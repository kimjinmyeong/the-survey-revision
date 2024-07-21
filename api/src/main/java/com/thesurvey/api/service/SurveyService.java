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
import com.thesurvey.api.repository.SurveyRepository;
import com.thesurvey.api.repository.UserRepository;
import com.thesurvey.api.service.command.Command;
import com.thesurvey.api.service.command.CommandExecutor;
import com.thesurvey.api.service.command.SurveyCreateCommands.*;
import com.thesurvey.api.service.mapper.QuestionBankMapper;
import com.thesurvey.api.service.mapper.QuestionOptionMapper;
import com.thesurvey.api.service.mapper.SurveyMapper;
import com.thesurvey.api.util.PointUtil;
import com.thesurvey.api.util.StringUtil;
import com.thesurvey.api.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
    private final AnsweredQuestionRepository answeredQuestionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SurveyListPageDto getAllSurvey(int page) {
        log.info("Fetching all surveys, page: {}", page);
        if (page < 1) {
            log.error("Invalid page number: {}", page);
            throw new BadRequestExceptionMapper(ErrorMessage.INVALID_REQUEST);
        }

        Page<Survey> surveyPage = surveyRepository.findAllInDescendingOrder(PageRequest.of(page - 1, 8));
        if (surveyPage.getTotalElements() != 0 && surveyPage.getTotalPages() < page) {
            log.error("Page not found, page: {}", page);
            throw new NotFoundExceptionMapper(ErrorMessage.PAGE_NOT_FOUND);
        }

        List<SurveyPageDto> surveyPageDtoList = surveyPage.getContent().stream()
                .map(surveyMapper::toSurveyPageDto).collect(Collectors.toList());
        return surveyMapper.toSurveyListPageDto(surveyPageDtoList, surveyPage);
    }

    @Transactional(readOnly = true)
    public SurveyResponseDto getSurveyBySurveyIdWithRelatedQuestion(Long surveyId) {
        log.info("Fetching survey by ID: {}", surveyId);
        Survey survey = getSurveyFromSurveyId(surveyId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Integer> surveyCertificationList = surveyRepository.findCertificationTypeBySurveyIdAndAuthorId(surveyId, survey.getAuthorId());
        Long userId = UserUtil.getUserIdFromAuthentication(authentication);

        if (!survey.getAuthorId().equals(userId)) {
            answeredQuestionService.validateUserCompletedCertification(surveyCertificationList, userId);
        }

        if (answeredQuestionRepository.existsByUserIdAndSurveyId(userId, survey.getSurveyId())) {
            log.warn("User ID {} has already submitted answers for survey ID {}", userId, surveyId);
            throw new ForbiddenRequestExceptionMapper(ErrorMessage.ANSWER_ALREADY_SUBMITTED);
        }

        return surveyMapper.toSurveyResponseDto(survey, survey.getAuthorId());
    }

    @Transactional(readOnly = true)
    public List<UserSurveyTitleDto> getUserCreatedSurveys(Authentication authentication) {
        log.info("Fetching user created surveys");
        return surveyRepository.findUserCreatedSurveysByAuthorID(UserUtil.getUserIdFromAuthentication(authentication));
    }

    @Transactional(readOnly = true)
    public UserSurveyResultDto getUserCreatedSurveyResult(Long surveyId) {
        log.info("Fetching user created survey result for survey ID: {}", surveyId);
        Survey survey = getSurveyFromSurveyId(surveyId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validateSurveyAuthor(UserUtil.getUserIdFromAuthentication(authentication), survey.getAuthorId());

        if (survey.getStartedDate().isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul")))) {
            log.warn("Survey ID {} has not started yet", surveyId);
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_NOT_STARTED);
        }

        List<QuestionBank> questionBanks = questionService.getAllQuestionBankBySurveyId(surveyId);
        List<QuestionBankAnswerDto> questionBankAnswerDtoList = getQuestionBankAnswerDtoList(questionBanks);
        return surveyMapper.toUserSurveyResultDto(survey, questionBankAnswerDtoList);
    }

    @Transactional
    public SurveyResponseDto createSurvey(SurveyRequestDto surveyRequestDto) {
        log.info("Creating new survey");

        validateSurveyCreateDate(surveyRequestDto);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = UserUtil.getUserFromAuthentication(authentication);
        List<CertificationType> certificationTypes = surveyRequestDto.getCertificationTypes().isEmpty() ? List.of(CertificationType.NONE) : surveyRequestDto.getCertificationTypes();

        // Initialize commands
        SaveSurveyCommand saveSurveyCommand = new SaveSurveyCommand(surveyRepository, surveyMapper, surveyRequestDto, user);
        saveSurveyCommand.execute(); // Execute immediately to get the survey object

        Survey survey = saveSurveyCommand.getSurvey();

        List<QuestionBank> questionBankList = questionService.getAllQuestionBankBySurveyId(survey.getSurveyId());
        int surveyCreatePoints = PointUtil.calculateSurveyCreatePoints(questionBankList);

        // Add commands to the executor
        List<Command> commands = List.of(
                new SaveQuestionsCommand(questionService, surveyRequestDto, survey),
                new UpdateUserPointsCommand(userRepository, user, surveyCreatePoints),
                new SaveParticipationCommand(participationService, user, certificationTypes, survey),
                new SavePointHistoryCommand(pointHistoryService, user, surveyCreatePoints)
        );

        CommandExecutor executor = new CommandExecutor(commands);
        executor.executeCommands();

        return surveyMapper.toSurveyResponseDto(survey, user.getUserId());
    }

    @Transactional
    public void deleteSurvey(Authentication authentication, Long surveyId) {
        log.info("Deleting survey ID: {}", surveyId);
        User user = UserUtil.getUserFromAuthentication(authentication);
        Survey survey = getSurveyFromSurveyId(surveyId);
        validateSurveyAuthor(user.getUserId(), survey.getAuthorId());

        if (survey.getStartedDate().isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul"))) && LocalDateTime.now(ZoneId.of("Asia/Seoul")).isBefore(survey.getEndedDate())) {
            log.error("Cannot delete survey ID {} because it has already started", surveyId);
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_ALREADY_STARTED);
        }
        List<QuestionBank> questionBankList = questionService.getAllQuestionBanksBySurveyId(surveyId);
        int surveyCreatePoints = PointUtil.calculateSurveyCreatePoints(questionBankList);
        user.updatePoint(user.getPoint() + surveyCreatePoints);
        userRepository.save(user);
        pointHistoryService.savePointHistory(user, surveyCreatePoints);
        participationService.deleteParticipation(surveyId);
        questionService.deleteQuestion(surveyId);
        surveyRepository.delete(survey);
    }

    @Transactional
    public SurveyResponseDto updateSurvey(SurveyUpdateRequestDto surveyUpdateRequestDto) {
        log.info("Updating survey ID: {}", surveyUpdateRequestDto.getSurveyId());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = UserUtil.getUserIdFromAuthentication(authentication);
        Survey survey = getSurveyFromSurveyId(surveyUpdateRequestDto.getSurveyId());
        validateSurveyAuthor(userId, survey.getAuthorId());
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

    private void validateSurveyCreateDate(SurveyRequestDto surveyRequestDto) {
        if (surveyRequestDto.getStartedDate().isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusSeconds(5))) {
            log.error("Invalid started date: {}", surveyRequestDto.getStartedDate());
            throw new BadRequestExceptionMapper(ErrorMessage.STARTEDDATE_ISBEFORE_CURRENTDATE);
        }

        if (surveyRequestDto.getStartedDate().isAfter(surveyRequestDto.getEndedDate())) {
            log.error("Started date is after ended date: startedDate={}, endedDate={}", surveyRequestDto.getStartedDate(), surveyRequestDto.getEndedDate());
            throw new BadRequestExceptionMapper(ErrorMessage.STARTEDDATE_ISAFTER_ENDEDDATE);
        }
    }

    public void validateUpdateSurvey(Survey survey, SurveyUpdateRequestDto surveyUpdateRequestDto) {
        if (survey.getEndedDate().isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")))) {
            log.warn("Cannot update survey ID {} because it has already ended", survey.getSurveyId());
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_ALREADY_ENDED);
        }

        if (survey.getStartedDate().isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusSeconds(5))) {
            log.warn("Cannot update survey ID {} because it has already started", survey.getSurveyId());
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_ALREADY_STARTED);
        }

        if (surveyUpdateRequestDto.getEndedDate().isBefore(surveyUpdateRequestDto.getStartedDate())) {
            log.error("Update validation failed: started date is after ended date for survey ID {}", survey.getSurveyId());
            throw new BadRequestExceptionMapper(ErrorMessage.STARTEDDATE_ISAFTER_ENDEDDATE);
        }

        if (surveyUpdateRequestDto.getStartedDate().isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusSeconds(5))) {
            log.error("Update validation failed: started date is before current date for survey ID {}", survey.getSurveyId());
            throw new BadRequestExceptionMapper(ErrorMessage.STARTEDDATE_ISBEFORE_CURRENTDATE);
        }
    }

    private void validateSurveyAuthor(Long userId, Long authorId) {
        if (!userId.equals(authorId)) {
            log.warn("User ID {} is not the author of survey ID {}", userId, authorId);
            throw new ForbiddenRequestExceptionMapper(ErrorMessage.AUTHOR_NOT_MATCHING);
        }
    }

    private Survey getSurveyFromSurveyId(Long surveyId) {
        return surveyRepository.findBySurveyId(surveyId)
                .orElseThrow(() -> {
                    log.error("Survey ID {} not found", surveyId);
                    return new NotFoundExceptionMapper(ErrorMessage.SURVEY_NOT_FOUND);
                });
    }

    private List<QuestionBankAnswerDto> getQuestionBankAnswerDtoList(List<QuestionBank> questionBankList) {
        List<QuestionBankAnswerDto> questionBankAnswerDtoList = new ArrayList<>();
        for (QuestionBank questionBank : questionBankList) {
            QuestionType questionType = questionBank.getQuestionType();
            Long questionBankId = questionBank.getQuestionBankId();
            Integer questionNo = questionService.getQuestionNoByQuestionBankId(questionBank.getQuestionBankId());
            List<AnsweredQuestion> answeredQuestionList = answeredQuestionService.getAnswerQuestionByQuestionBankId(questionBank.getQuestionBankId());

            List<QuestionOptionAnswerDto> questionOptionAnswerDtoList = new ArrayList<>();
            List<String> shortLongAnswerList = new ArrayList<>();

            if (questionType == QuestionType.SINGLE_CHOICE || questionType == QuestionType.MULTIPLE_CHOICES) {
                questionOptionAnswerDtoList = getQuestionOptionAnswerDtoList(questionBankId, questionType);
            } else if (questionType == QuestionType.SHORT_ANSWER || questionType == QuestionType.LONG_ANSWER) {
                shortLongAnswerList = getShortLongAnswerList(questionType, answeredQuestionList);
            }

            questionBankAnswerDtoList.add(questionBankMapper.toQuestionBankAnswerDto(questionBank, questionNo, shortLongAnswerList, questionOptionAnswerDtoList));
        }

        return questionBankAnswerDtoList;
    }

    private List<QuestionOptionAnswerDto> getQuestionOptionAnswerDtoList(Long questionBankId, QuestionType questionType) {
        List<Long[]> answeredChoiceList = new ArrayList<>();
        if (questionType == QuestionType.SINGLE_CHOICE) {
            answeredChoiceList = answeredQuestionService.getSingleChoiceResult(questionBankId);
        } else if (questionType == QuestionType.MULTIPLE_CHOICES) {
            answeredChoiceList = answeredQuestionService.getMultipleChoiceResult(questionBankId);
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

    private List<String> getShortLongAnswerList(QuestionType questionType, List<AnsweredQuestion> answeredQuestionList) {
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