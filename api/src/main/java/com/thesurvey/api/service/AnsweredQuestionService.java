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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    @Transactional
    public List<AnsweredQuestion> getAnswerQuestionByQuestionBankId(Long questionBankId) {
        log.info("Fetching answered questions for question bank ID: {}", questionBankId);
        return answeredQuestionRepository.findAllByQuestionBankId(questionBankId);
    }

    @Transactional(readOnly = true)
    public List<Long[]> getSingleChoiceResult(Long questionBankId) {
        log.info("Fetching single choice results for question bank ID: {}", questionBankId);
        return answeredQuestionRepository.countSingleChoiceByQuestionBankId(questionBankId);
    }

    @Transactional(readOnly = true)
    public List<Long[]> getMultipleChoiceResult(Long questionBankId) {
        log.info("Fetching multiple choice results for question bank ID: {}", questionBankId);
        return answeredQuestionRepository.countMultipleChoiceByQuestionBankId(questionBankId);
    }

    @Transactional
    public AnsweredQuestionRewardPointDto createAnswer(AnsweredQuestionRequestDto answeredQuestionRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = UserUtil.getUserFromAuthentication(authentication);
        log.info("Creating answer for user: {} and survey ID: {}", user.getUserId(), answeredQuestionRequestDto.getSurveyId());

        Survey survey = surveyRepository.findBySurveyId(answeredQuestionRequestDto.getSurveyId())
                .orElseThrow(() -> {
                    log.error("Survey not found with ID: {}", answeredQuestionRequestDto.getSurveyId());
                    return new NotFoundExceptionMapper(ErrorMessage.SURVEY_NOT_FOUND);
                });

        List<Integer> surveyCertificationList = surveyRepository.findCertificationTypeBySurveyIdAndAuthorId(
                survey.getSurveyId(), survey.getAuthorId());
        validateUserCompletedCertification(surveyCertificationList, user.getUserId());
        validateCreateAnswerRequest(user, survey);

        int rewardPoints = 0;
        boolean isAnswered = false;
        for (AnsweredQuestionDto answeredQuestionDto : answeredQuestionRequestDto.getAnswers()) {
            if (answeredQuestionDto.getIsRequired() && validateEmptyAnswer(answeredQuestionDto)) {
                log.warn("Required question not answered by user: {}", user.getUserId());
                throw new BadRequestExceptionMapper(ErrorMessage.NOT_ANSWER_TO_REQUIRED_QUESTION);
            }
            if (!isAnswered && !validateEmptyAnswer(answeredQuestionDto)) {
                isAnswered = true;
            }

            QuestionBank questionBank = questionBankRepository.findByQuestionBankId(
                    answeredQuestionDto.getQuestionBankId()).orElseThrow(
                    () -> {
                        log.error("Question bank not found with ID: {}", answeredQuestionDto.getQuestionBankId());
                        return new NotFoundExceptionMapper(ErrorMessage.QUESTION_BANK_NOT_FOUND);
                    });

            // check if the question is included in the survey.
            Optional<Question> question = questionRepository.findBySurveyIdAndQuestionBankId(survey.getSurveyId(), questionBank.getQuestionBankId());
            if (question.isEmpty()) {
                log.error("Question with ID: {} not part of survey with ID: {}", questionBank.getQuestionBankId(), survey.getSurveyId());
                throw new BadRequestExceptionMapper(ErrorMessage.NOT_SURVEY_QUESTION);
            }

            // In case it's not multiple choice question
            if (answeredQuestionDto.getMultipleChoices() == null
                    || answeredQuestionDto.getMultipleChoices().isEmpty()) {
                answeredQuestionRepository.save(
                        answeredQuestionMapper.toAnsweredQuestion(answeredQuestionDto, user, question.get()));
            } else {
                // In case it's multiple choice question
                List<AnsweredQuestion> answeredQuestionList = answeredQuestionDto.getMultipleChoices()
                        .stream()
                        .map(choice -> answeredQuestionMapper.toAnsweredQuestionWithMultipleChoices(
                                user, question.get(), choice))
                        .collect(Collectors.toList());

                answeredQuestionRepository.saveAll(answeredQuestionList);
            }
            rewardPoints += getQuestionBankRewardPoints(answeredQuestionDto);

        }
        if (!isAnswered) {
            log.warn("User: {} did not answer at least one question for survey: {}", user.getUserId(), survey.getSurveyId());
            throw new BadRequestExceptionMapper(ErrorMessage.ANSWER_AT_LEAST_ONE_QUESTION);
        }

        List<CertificationType> certificationTypeList =
                getCertificationTypeList(surveyCertificationList);
        participationService.createParticipation(user, certificationTypeList, survey);
        pointHistoryService.savePointHistory(user, rewardPoints);
        user.updatePoint(user.getPoint() + rewardPoints);
        userRepository.save(user);
        log.info("Answers saved and reward points updated for user: {}", user.getUserId());
        return AnsweredQuestionRewardPointDto.builder().rewardPoints(rewardPoints).build();
    }

    @Transactional
    public void deleteAnswer(Long surveyId) {
        log.info("Deleting answers for survey ID: {}", surveyId);
        List<AnsweredQuestion> answeredQuestionList = answeredQuestionRepository.findAllBySurveyId(surveyId);
        answeredQuestionRepository.deleteAll(answeredQuestionList);
        log.info("Answers deleted for survey ID: {}", surveyId);
    }

    private void validateCreateAnswerRequest(User user, Survey survey) {
        // validate if a user has already responded to the survey
        if (answeredQuestionRepository.existsByUserIdAndSurveyId(user.getUserId(),
                survey.getSurveyId())) {
            log.warn("User: {} has already submitted answers for survey: {}", user.getUserId(), survey.getSurveyId());
            throw new ForbiddenRequestExceptionMapper(ErrorMessage.ANSWER_ALREADY_SUBMITTED);
        }

        // validate if the survey creator is attempting to respond to their own survey
        if (user.getUserId().equals(survey.getAuthorId())) {
            log.warn("Survey creator: {} attempting to answer their own survey: {}", user.getUserId(), survey.getSurveyId());
            throw new ForbiddenRequestExceptionMapper(ErrorMessage.CREATOR_CANNOT_ANSWER);
        }

        // validate if the survey has not yet started
        if (LocalDateTime.now(ZoneId.of("Asia/Seoul")).isBefore(survey.getStartedDate())) {
            log.warn("Survey: {} has not started yet", survey.getSurveyId());
            throw new ForbiddenRequestExceptionMapper(ErrorMessage.SURVEY_NOT_STARTED);
        }

        // validate if the survey has already ended
        if (LocalDateTime.now(ZoneId.of("Asia/Seoul")).isAfter(survey.getEndedDate())) {
            log.warn("Survey: {} has already ended", survey.getSurveyId());
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
            log.warn("User: {} has not completed necessary certifications", userId);
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
