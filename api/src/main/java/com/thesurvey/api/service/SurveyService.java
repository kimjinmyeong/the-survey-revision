package com.thesurvey.api.service;

import com.thesurvey.api.domain.AnsweredQuestion;
import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;
import com.thesurvey.api.domain.EnumTypeEntity.QuestionType;
import com.thesurvey.api.domain.QuestionBank;
import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.dto.request.survey.SurveyRequestDto;
import com.thesurvey.api.dto.request.survey.SurveyUpdateRequestDto;
import com.thesurvey.api.dto.response.question.QuestionBankAnswerDto;
import com.thesurvey.api.dto.response.question.QuestionOptionAnswerDto;
import com.thesurvey.api.dto.response.survey.SurveyListPageDto;
import com.thesurvey.api.dto.response.survey.SurveyPageDto;
import com.thesurvey.api.dto.response.survey.SurveyResponseDto;
import com.thesurvey.api.dto.response.user.UserSurveyResultDto;
import com.thesurvey.api.dto.response.user.UserSurveyTitleDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import com.thesurvey.api.exception.mapper.ForbiddenRequestExceptionMapper;
import com.thesurvey.api.exception.mapper.NotFoundExceptionMapper;
import com.thesurvey.api.repository.AnsweredQuestionRepository;
import com.thesurvey.api.repository.QuestionBankRepository;
import com.thesurvey.api.repository.SurveyRepository;
import com.thesurvey.api.repository.UserRepository;
import com.thesurvey.api.service.mapper.QuestionBankMapper;
import com.thesurvey.api.service.mapper.QuestionOptionMapper;
import com.thesurvey.api.service.mapper.SurveyMapper;
import com.thesurvey.api.util.PointUtil;
import com.thesurvey.api.util.StringUtil;
import com.thesurvey.api.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;

    private final SurveyMapper surveyMapper;

    private final QuestionService questionService;

    private final QuestionOptionService questionOptionService;

    private final ParticipationService participationService;

    private final AnsweredQuestionService answeredQuestionService;

    private final QuestionOptionMapper questionOptionMapper;

    private final QuestionBankMapper questionBankMapper;

    private final PointHistoryService pointHistoryService;

    private final PointUtil pointUtil;

    private final AnsweredQuestionRepository answeredQuestionRepository;

    private final UserRepository userRepository;

    private final QuestionBankRepository questionBankRepository;

    private final RedissonClient redissonClient;

    private final long TIMEOUT_SECONDS = 5;

    @Transactional(readOnly = true)
    public SurveyListPageDto getAllSurvey(int page) {
        // page starts from 1
        if (page < 1) {
            throw new BadRequestExceptionMapper(ErrorMessage.INVALID_REQUEST);
        }

        Page<Survey> surveyPage = surveyRepository.findAllInDescendingOrder(
                PageRequest.of(page - 1, 8));
        if (surveyPage.getTotalElements() != 0 && surveyPage.getTotalPages() < page) {
            throw new NotFoundExceptionMapper(ErrorMessage.PAGE_NOT_FOUND);
        }

        List<SurveyPageDto> surveyPageDtoList = surveyPage.getContent().stream()
                .map(surveyMapper::toSurveyPageDto).collect(Collectors.toList());
        return surveyMapper.toSurveyListPageDto(surveyPageDtoList, surveyPage);
    }

    @Transactional(readOnly = true)
    public SurveyResponseDto getSurveyBySurveyIdWithRelatedQuestion(Long surveyId) {
        Survey survey = getSurveyFromSurveyId(surveyId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Integer> surveyCertificationList =
                surveyRepository.findCertificationTypeBySurveyIdAndAuthorId(surveyId, survey.getAuthorId());
        Long userId = UserUtil.getUserIdFromAuthentication(authentication);

        // validate if the user has completed the necessary certifications for the survey
        if(!survey.getAuthorId().equals(userId)){
            answeredQuestionService.validateUserCompletedCertification(surveyCertificationList, userId);
        }

        // validate if a user has already responded to the survey
        if (answeredQuestionRepository.existsByUserIdAndSurveyId(userId, survey.getSurveyId())) {
            throw new ForbiddenRequestExceptionMapper(ErrorMessage.ANSWER_ALREADY_SUBMITTED);
        }

        return surveyMapper.toSurveyResponseDto(survey, survey.getAuthorId());
    }

    @Transactional(readOnly = true)
    public List<UserSurveyTitleDto> getUserCreatedSurveys(Authentication authentication) {
        Long authorId = UserUtil.getUserIdFromAuthentication(authentication);
        List<Survey> surveys = surveyRepository.findUserCreatedSurveysByAuthorID(authorId);
        return surveys.stream()
                .map(survey -> new UserSurveyTitleDto(survey.getSurveyId(), survey.getTitle()))
                .collect(Collectors.toList());
    }

    /**
     * Returns survey results and the answers created by the user.
     *
     * @param surveyId the ID of survey to get result
     * @return {@link UserSurveyResultDto}
     */
    @Transactional(readOnly = true)
    public UserSurveyResultDto getUserCreatedSurveyResult(Long surveyId) {
        Survey survey = getSurveyFromSurveyId(surveyId);

        // validate survey author from current user

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validateSurveyAuthor(UserUtil.getUserIdFromAuthentication(authentication),
                survey.getAuthorId());

        // validate if the survey has not yet started
        if (survey.getStartedDate().isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul")))) {
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_NOT_STARTED);
        }

        List<QuestionBank> questionBanks = questionService.getAllQuestionBankBySurveyId(surveyId);

        List<QuestionBankAnswerDto> questionBankAnswerDtoList = getQuestionBankAnswerDtoList(questionBanks);

        return surveyMapper.toUserSurveyResultDto(survey, questionBankAnswerDtoList);
    }

    @Transactional
    public SurveyResponseDto createSurvey(SurveyRequestDto surveyRequestDto) {

        // calculate survey need point
        int surveyCreatePoints = surveyRequestDto.getQuestions().stream()
                .mapToInt(questionRequestDto -> pointUtil.calculateSurveyCreatePoints(questionRequestDto.getQuestionType()))
                .sum();

        // fetch need certification
        List<CertificationType> certificationTypes =
                surveyRequestDto.getCertificationTypes().isEmpty()
                        ? List.of(CertificationType.NONE) : surveyRequestDto.getCertificationTypes();

        RLock lock = redissonClient.getLock("createSurveyLock");
        boolean isLocked = false;
        User user;
        try {
            isLocked = lock.tryLock(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new LockTimeoutException("지금은 설문조사를 생성할 수 없습니다. 잠시 후 다시 시도해 주세요.");
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            user = UserUtil.getUserFromAuthentication(authentication);
            validateCreateSurvey(surveyRequestDto, user);
        } catch (InterruptedException e) {
            throw new RuntimeException("스레드가 중단되었습니다.", e);
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
        pointHistoryService.savePointHistory(user, -surveyCreatePoints);
        Survey survey = surveyRepository.save(surveyMapper.toSurvey(surveyRequestDto, user.getUserId()));
        questionService.createQuestion(surveyRequestDto.getQuestions(), survey);
        participationService.createParticipation(user, certificationTypes, survey);
        return surveyMapper.toSurveyResponseDto(survey, user.getUserId());
    }

    @Transactional
    public void deleteSurvey(Authentication authentication, Long surveyId) {
        User user = UserUtil.getUserFromAuthentication(authentication);
        Survey survey = getSurveyFromSurveyId(surveyId);

        // validate survey author from current user
        validateSurveyAuthor(user.getUserId(), survey.getAuthorId());

        // validate for attempts to delete a started survey.
        if (survey.getStartedDate().isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                && LocalDateTime.now(ZoneId.of("Asia/Seoul")).isBefore(survey.getEndedDate())) {
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_ALREADY_STARTED);
        }

        List<QuestionBank> questionBankList = questionBankRepository.findAllBySurveyId(surveyId);
        int surveyCreatePoints = questionBankList.stream()
                .mapToInt(questionBank -> pointUtil.calculateSurveyCreatePoints(questionBank.getQuestionType()))
                .sum();
        user.updatePoint(user.getPoint() + surveyCreatePoints);
        userRepository.save(user);

        pointHistoryService.savePointHistory(user, surveyCreatePoints);
        participationService.deleteParticipation(surveyId);
        questionService.deleteQuestion(surveyId);
        surveyRepository.delete(survey);

    }

    @Transactional
    public SurveyResponseDto updateSurvey(SurveyUpdateRequestDto surveyUpdateRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = UserUtil.getUserIdFromAuthentication(authentication);
        Survey survey = getSurveyFromSurveyId(surveyUpdateRequestDto.getSurveyId());

        // validate survey author from current user
        validateSurveyAuthor(userId, survey.getAuthorId());

        // validate survey request dto
        validateUpdateSurvey(survey, surveyUpdateRequestDto);

        survey.changeTitle(StringUtil.trim(surveyUpdateRequestDto.getTitle()));
        survey.changeDescription(StringUtil.trim(surveyUpdateRequestDto.getDescription()));

        if (surveyUpdateRequestDto.getStartedDate() != null) {
            survey.changeStartedDate(surveyUpdateRequestDto.getStartedDate());
        }

        if (surveyUpdateRequestDto.getEndedDate() != null) {
            survey.changeEndedDate(surveyUpdateRequestDto.getEndedDate());
        }

        questionService.updateQuestion(survey.getSurveyId(), surveyUpdateRequestDto.getQuestions());
        return surveyMapper.toSurveyResponseDto(survey, userId);
    }

    public void validateUpdateSurvey(Survey survey, SurveyUpdateRequestDto surveyUpdateRequestDto) {
        // validate for attempts to modify ended survey.
        if (survey.getEndedDate().isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")))) {
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_ALREADY_ENDED);
        }

        // validate for when to modify a survey that has already been started.
        if (survey.getStartedDate()
                .isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusSeconds(5))) {
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_ALREADY_STARTED);
        }

        // validate for when the start time of the survey is set after the end time.
        if (surveyUpdateRequestDto.getEndedDate()
                .isBefore(surveyUpdateRequestDto.getStartedDate())) {
            throw new BadRequestExceptionMapper(ErrorMessage.STARTEDDATE_ISAFTER_ENDEDDATE);
        }

        // `startedDate` is only allowed to be within 5 seconds from now or later.
        if (surveyUpdateRequestDto.getStartedDate()
                .isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusSeconds(5))) {
            throw new BadRequestExceptionMapper(ErrorMessage.STARTEDDATE_ISBEFORE_CURRENTDATE);
        }

    }

    public void validateUserPoint(User user, Integer surveyCreatePoints) {
        if (user.getPoint() - surveyCreatePoints < 0) {
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_CREATE_POINT_NOT_ENOUGH);
        }
        user.updatePoint(user.getPoint() - surveyCreatePoints);
        userRepository.saveAndFlush(user);
    }

    private void validateCreateSurvey(SurveyRequestDto surveyRequestDto, User user) {
        // validate that the survey's start date is not more than 5 seconds in the past.
        if (surveyRequestDto.getStartedDate()
                .isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusSeconds(5))) {
            throw new BadRequestExceptionMapper(ErrorMessage.STARTEDDATE_ISBEFORE_CURRENTDATE);
        }

        // Validate that the survey's start date is not after its end date.
        if (surveyRequestDto.getStartedDate().isAfter(surveyRequestDto.getEndedDate())) {
            throw new BadRequestExceptionMapper(ErrorMessage.STARTEDDATE_ISAFTER_ENDEDDATE);
        }

        // Validate that the user has enough points to create the survey.
        int surveyCreatePoints = surveyRequestDto.getQuestions().stream()
                .mapToInt(questionRequestDto -> pointUtil.calculateSurveyCreatePoints(questionRequestDto.getQuestionType()))
                .sum();
        validateUserPoint(user, surveyCreatePoints);

        // Validate that at least 30 seconds have passed since the user's last survey creation.
        List<Survey> surveys = surveyRepository.findUserCreatedSurveysByAuthorID(user.getUserId());
        if (surveys.isEmpty()) {
            return;
        }
        LocalDateTime userRecentCreateTime = surveys.get(surveys.size() - 1).getCreatedDate();
        Duration duration = Duration.between(userRecentCreateTime, LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        long secondsDifference = Math.abs(duration.getSeconds());
        if (secondsDifference < 30) {
            throw new BadRequestExceptionMapper(ErrorMessage.USER_CREATE_SURVEY_RECENT);
        }
    }

    private void validateSurveyAuthor(Long userId, Long authorId) {
        if (!userId.equals(authorId)) {
            throw new ForbiddenRequestExceptionMapper(ErrorMessage.AUTHOR_NOT_MATCHING);
        }
    }

    private Survey getSurveyFromSurveyId(Long surveyId) {
        return surveyRepository.findBySurveyId(surveyId)
                .orElseThrow(() -> new NotFoundExceptionMapper(ErrorMessage.SURVEY_NOT_FOUND));
    }

    private List<QuestionBankAnswerDto> getQuestionBankAnswerDtoList(List<QuestionBank> questionBankList) {
        List<QuestionBankAnswerDto> questionBankAnswerDtoList = new ArrayList<>();
        for (QuestionBank questionBank : questionBankList) {
            QuestionType questionType = questionBank.getQuestionType();
            Long questionBankId = questionBank.getQuestionBankId();
            Integer questionNo = questionService.getQuestionNoByQuestionBankId(
                    questionBank.getQuestionBankId());
            List<AnsweredQuestion> answeredQuestionList = answeredQuestionService.getAnswerQuestionByQuestionBankId(
                    questionBank.getQuestionBankId());

            List<QuestionOptionAnswerDto> questionOptionAnswerDtoList = new ArrayList<>();
            List<String> shortLongAnswerList = new ArrayList<>();

            if (questionType == QuestionType.SINGLE_CHOICE || questionType == QuestionType.MULTIPLE_CHOICES) {
                questionOptionAnswerDtoList = getQuestionOptionAnswerDtoList(questionBankId,
                        questionType);
            } else if (questionType == QuestionType.SHORT_ANSWER || questionType == QuestionType.LONG_ANSWER) {
                shortLongAnswerList = getShortLongAnswerList(questionType, answeredQuestionList);
            }

            questionBankAnswerDtoList.add(questionBankMapper.toQuestionBankAnswerDto(questionBank,
                    questionNo, shortLongAnswerList, questionOptionAnswerDtoList));
        }

        return questionBankAnswerDtoList;
    }

    /**
     * Returns a list of {@link QuestionOptionAnswerDto} containing the answer data
     * for a given {@code Long questionBankId} and {@code QuestionType questionType}.
     *
     * @param questionBankId the ID of the question bank for which to retrieve answer data
     * @param questionType  the type of question for which to retrieve answer data,
     * either {@code QuestionType.SINGLE_CHOICE} or {@code QuestionType.MULTIPLE_CHOICES}
     * @return {@code List<QuestionOptionAnswerDto>}
     */
    private List<QuestionOptionAnswerDto> getQuestionOptionAnswerDtoList(Long questionBankId,
                                                                         QuestionType questionType) {
        List<Long[]> answeredChoiceList = new ArrayList<>();
        if (questionType == QuestionType.SINGLE_CHOICE) {
            answeredChoiceList = answeredQuestionService.getSingleChoiceResult(
                    questionBankId);
        } else if (questionType == QuestionType.MULTIPLE_CHOICES) {
            answeredChoiceList = answeredQuestionService.getMultipleChoiceResult(
                    questionBankId);
        }

        return answeredChoiceList.stream()
                .map(answeredChoiceResult -> {
                    Long questionOptionId = answeredChoiceResult[0];
                    Long totalResponseCount = answeredChoiceResult[1];
                    String option = questionOptionService.getOptionByQuestionOptionId(questionOptionId);
                    return questionOptionMapper.toQuestionOptionAnswerDto(questionOptionId, option, totalResponseCount);
                })
                .collect(Collectors.toList());

    }

    /**
     * Returns a list of short or long answer strings
     * based on the provided {@code QuestionType questionType} and
     * {@code List<AnsweredQuestion>answeredQuestionList}.
     *
     * @param questionType the type of question for which to retrieve answer data,
     * either {@code QuestionType.SHORT_ANSWER} or {@code QuestionType.LONG_ANSWER}
     * @param answeredQuestionList a list of {@code AnsweredQuestion}
     * @return {@code List<String>}
     */
    private List<String> getShortLongAnswerList(QuestionType questionType,
                                                List<AnsweredQuestion> answeredQuestionList) {

        List<String> shortLongAnswerList = new ArrayList<>();
        if (questionType == QuestionType.SHORT_ANSWER) {
            shortLongAnswerList = getShortAnswerList(answeredQuestionList);
        } else if (questionType == QuestionType.LONG_ANSWER) {
            shortLongAnswerList = getLongAnswerList(answeredQuestionList);
        }

        return shortLongAnswerList;
    }

    private List<String> getShortAnswerList(List<AnsweredQuestion> answeredQuestionList) {
        return answeredQuestionList.stream()
                .map(AnsweredQuestion::getShortAnswer)
                .collect(Collectors.toList());
    }

    private List<String> getLongAnswerList(List<AnsweredQuestion> answeredQuestionList) {
        return answeredQuestionList.stream()
                .map(AnsweredQuestion::getLongAnswer)
                .collect(Collectors.toList());
    }

}
