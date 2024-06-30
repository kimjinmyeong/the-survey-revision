package com.thesurvey.api.service;

import com.thesurvey.api.domain.*;
import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionDto;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionRequestDto;
import com.thesurvey.api.dto.response.answeredQuestion.AnsweredQuestionRewardPointDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import com.thesurvey.api.exception.mapper.ForbiddenRequestExceptionMapper;
import com.thesurvey.api.exception.mapper.NotFoundExceptionMapper;
import com.thesurvey.api.exception.mapper.UnauthorizedRequestExceptionMapper;
import com.thesurvey.api.repository.*;
import com.thesurvey.api.service.converter.CertificationTypeConverter;
import com.thesurvey.api.service.mapper.AnsweredQuestionMapper;
import com.thesurvey.api.util.PointUtil;
import com.thesurvey.api.util.StringUtil;
import com.thesurvey.api.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockTimeoutException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AnsweredQuestionService {

    private final SurveyRepository surveyRepository;

    private final AnsweredQuestionRepository answeredQuestionRepository;

    private final QuestionBankRepository questionBankRepository;

    private final AnsweredQuestionMapper answeredQuestionMapper;

    private final QuestionRepository questionRepository;

    private final ParticipationService participationService;

    private final UserCertificationRepository userCertificationRepository;

    private final CertificationTypeConverter certificationTypeConverter;

    private final PointHistoryService pointHistoryService;

    private final PointUtil pointUtil;

    private final UserRepository userRepository;

    private final RedissonClient redissonClient;

    private final long TIMEOUT_SECONDS = 5;

    @Transactional
    public List<AnsweredQuestion> getAnswerQuestionByQuestionBankId(Long questionBankId) {
        return answeredQuestionRepository.findAllByQuestionBankId(questionBankId);
    }

    @Transactional(readOnly = true)
    public List<Long[]> getSingleChoiceResult(Long questionBankId) {
        return answeredQuestionRepository.countSingleChoiceByQuestionBankId(questionBankId);
    }

    @Transactional(readOnly = true)
    public List<Long[]> getMultipleChoiceResult(Long questionBankId) {
        return answeredQuestionRepository.countMultipleChoiceByQuestionBankId(questionBankId);
    }

    @Transactional
    public AnsweredQuestionRewardPointDto createAnswer(AnsweredQuestionRequestDto answeredQuestionRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = UserUtil.getUserFromAuthentication(authentication);
        RLock lock = redissonClient.getLock("createAnswerLock: " + user.getEmail());
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new LockTimeoutException("지금은 답변을 제출할 수 없습니다. 잠시 후 다시 시도해 주세요.");
            }

            Survey survey = surveyRepository.findBySurveyId(answeredQuestionRequestDto.getSurveyId())
                    .orElseThrow(() -> new NotFoundExceptionMapper(ErrorMessage.SURVEY_NOT_FOUND));
            List<Integer> surveyCertificationList = surveyRepository.findCertificationTypeBySurveyIdAndAuthorId(
                    survey.getSurveyId(), survey.getAuthorId());
            List<CertificationType> convertedCertificationTypeList =
                    getCertificationTypeList(surveyCertificationList);

            validateUserCompletedCertification(surveyCertificationList, user.getUserId());
            validateCreateAnswerRequest(user, survey);

            int rewardPoints = getRewardPoints(answeredQuestionRequestDto);
            saveAnswers(answeredQuestionRequestDto, survey, user);
            updateUserPoint(user, rewardPoints);
            participationService.createParticipation(user, convertedCertificationTypeList, survey);
            return AnsweredQuestionRewardPointDto.builder().rewardPoints(rewardPoints).build();
        } catch (InterruptedException e) {
            throw new RuntimeException("스레드가 중단되었습니다.");
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
    }

    private void updateUserPoint(User user, int rewardPoints) {
        pointHistoryService.savePointHistory(user, rewardPoints);
        user.updatePoint(user.getPoint() + rewardPoints);
        userRepository.save(user);
    }

    private int getRewardPoints(AnsweredQuestionRequestDto answeredQuestionRequestDto) {
        int rewardPoints = 0;
        boolean isAnswered = false;
        for (AnsweredQuestionDto answeredQuestionDto : answeredQuestionRequestDto.getAnswers()) {
            if (answeredQuestionDto.getIsRequired() && validateEmptyAnswer(answeredQuestionDto)) {
                throw new BadRequestExceptionMapper(ErrorMessage.NOT_ANSWER_TO_REQUIRED_QUESTION);
            }
            if (!isAnswered && !validateEmptyAnswer(answeredQuestionDto)) {
                isAnswered = true;
            }
            rewardPoints += getQuestionBankRewardPoints(answeredQuestionDto);
        }
        if (!isAnswered) {
            throw new BadRequestExceptionMapper(ErrorMessage.ANSWER_AT_LEAST_ONE_QUESTION);
        }
        return rewardPoints;
    }

    private void saveAnswers(AnsweredQuestionRequestDto answeredQuestionRequestDto, Survey survey, User user) {
        for (AnsweredQuestionDto answeredQuestionDto : answeredQuestionRequestDto.getAnswers()) {
            QuestionBank questionBank = questionBankRepository.findByQuestionBankId(
                    answeredQuestionDto.getQuestionBankId()).orElseThrow(
                    () -> new NotFoundExceptionMapper(ErrorMessage.QUESTION_BANK_NOT_FOUND));

            // check if the question is included in the survey.
            Optional<Question> question = questionRepository.findBySurveyIdAndQuestionBankId(survey.getSurveyId(), questionBank.getQuestionBankId());
            if (question.isEmpty()) {
                throw new BadRequestExceptionMapper(ErrorMessage.NOT_SURVEY_QUESTION);
            }

            // In case it's not multiple choice question
            if (answeredQuestionDto.getMultipleChoices() == null
                    || answeredQuestionDto.getMultipleChoices().isEmpty()) {
                answeredQuestionRepository.save(
                        answeredQuestionMapper.toAnsweredQuestion(answeredQuestionDto, user, question.get()));
            } else {
                // In case it's multiple choice question
                List<AnsweredQuestion> answeredQuestionList = new ArrayList<>();
                for (long choice : answeredQuestionDto.getMultipleChoices()) {
                    AnsweredQuestion answeredQuestion = answeredQuestionMapper.toAnsweredQuestionWithMultipleChoices(user, question.get(), choice);
                    answeredQuestionList.add(answeredQuestion);
                }
                answeredQuestionRepository.saveAll(answeredQuestionList);
            }
        }
    }

    @Transactional
    public void deleteAnswer(Long surveyId) {
        List<AnsweredQuestion> answeredQuestionList = answeredQuestionRepository.findAllBySurveyId(
                surveyId);
        answeredQuestionRepository.deleteAll(answeredQuestionList);
    }

    private void validateCreateAnswerRequest(User user, Survey survey) {
        // validate if a user has already responded to the survey
        if (answeredQuestionRepository.existsByUserIdAndSurveyId(user.getUserId(),
                survey.getSurveyId())) {
            throw new ForbiddenRequestExceptionMapper(ErrorMessage.ANSWER_ALREADY_SUBMITTED);
        }

        // validate if the survey creator is attempting to respond to their own survey
        if (user.getUserId().equals(survey.getAuthorId())) {
            throw new ForbiddenRequestExceptionMapper(ErrorMessage.CREATOR_CANNOT_ANSWER);
        }

        // validate if the survey has not yet started
        if (LocalDateTime.now(ZoneId.of("Asia/Seoul")).isBefore(survey.getStartedDate())) {
            throw new ForbiddenRequestExceptionMapper(ErrorMessage.SURVEY_NOT_STARTED);
        }

        // validate if the survey has already ended
        if (LocalDateTime.now(ZoneId.of("Asia/Seoul")).isAfter(survey.getEndedDate())) {
            throw new ForbiddenRequestExceptionMapper(ErrorMessage.SURVEY_ALREADY_ENDED);
        }
    }

    private boolean validateEmptyAnswer(AnsweredQuestionDto answeredQuestionDto) {
        return (answeredQuestionDto.getLongAnswer() == null
                || StringUtil.trimShortLongAnswer(answeredQuestionDto.getLongAnswer(),
                answeredQuestionDto.getIsRequired()).isEmpty())
                && (answeredQuestionDto.getShortAnswer() == null
                || StringUtil.trimShortLongAnswer(answeredQuestionDto.getShortAnswer(),
                answeredQuestionDto.getIsRequired()).isEmpty())
                && answeredQuestionDto.getSingleChoice() == null
                && (answeredQuestionDto.getMultipleChoices() == null || answeredQuestionDto.getMultipleChoices().isEmpty());
    }

    // validate if the user has completed the necessary certifications for the survey
    public void validateUserCompletedCertification(List<Integer> surveyCertificationList,
                                                   Long userId) {
        if (surveyCertificationList.contains(CertificationType.NONE.getCertificationTypeId())) {
            return;
        }
        List<Integer> userCertificationList = userCertificationRepository.findUserCertificationTypeByUserId(
                userId);
        if (!new HashSet<>(userCertificationList).containsAll(surveyCertificationList)) {
            throw new UnauthorizedRequestExceptionMapper(ErrorMessage.CERTIFICATION_NOT_COMPLETED);
        }
    }

    private List<CertificationType> getCertificationTypeList(List<Integer> surveyCertificationList) {
        if (surveyCertificationList.contains(CertificationType.NONE.getCertificationTypeId())) {
            return List.of(CertificationType.NONE);
        }
        return certificationTypeConverter.toCertificationTypeList(surveyCertificationList);
    }

    private int getQuestionBankRewardPoints(AnsweredQuestionDto answeredQuestionDto) {
        if (!validateEmptyAnswer(answeredQuestionDto)) {
            return pointUtil.calculateSurveyMaxRewardPoints(answeredQuestionDto.getQuestionType());
        }
        return 0;
    }

}
