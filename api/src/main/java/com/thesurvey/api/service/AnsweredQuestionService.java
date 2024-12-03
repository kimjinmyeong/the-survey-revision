package com.thesurvey.api.service;

import com.thesurvey.api.domain.*;
import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionDto;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionRequestDto;
import com.thesurvey.api.dto.response.answeredQuestion.AnsweredQuestionRewardPointDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import com.thesurvey.api.exception.mapper.NotFoundExceptionMapper;
import com.thesurvey.api.exception.mapper.UnauthorizedRequestExceptionMapper;
import com.thesurvey.api.repository.*;
import com.thesurvey.api.service.command.AnsweredQuestionCreateCommands.SaveParticipationCommand;
import com.thesurvey.api.service.command.AnsweredQuestionCreateCommands.SavePointHistoryCommand;
import com.thesurvey.api.service.command.Command;
import com.thesurvey.api.service.command.CommandExecutor;
import com.thesurvey.api.service.command.SurveyCreateCommands.UpdateUserPointsCommand;
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
    private final ParticipationService participationService;
    private final UserCertificationRepository userCertificationRepository;
    private final PointHistoryService pointHistoryService;
    private final UserRepository userRepository;
    private final AnsweredQuestionMapper answeredQuestionMapper;
    private final QuestionRepository questionRepository;
    private final QuestionBankRepository questionBankRepository;

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

        // Execute validation and fetch survey command
        Survey survey = surveyRepository.findBySurveyId(answeredQuestionRequestDto.getSurveyId())
                .orElseThrow(() -> new NotFoundExceptionMapper(ErrorMessage.SURVEY_NOT_FOUND));
        List<Integer> surveyCertificationList = surveyRepository.findCertificationTypeBySurveyIdAndAuthorId(survey.getSurveyId(), survey.getAuthorId());

        // Execute validation and fetch rewardPoints
        int rewardPoints = saveAnsweredQuestion(answeredQuestionRequestDto, survey, user);

        // Initialize and execute other commands
        List<Command> commands = List.of(
                new SaveParticipationCommand(participationService, user, survey, surveyCertificationList),
                new SavePointHistoryCommand(user, pointHistoryService, rewardPoints),
                new UpdateUserPointsCommand(userRepository, user, rewardPoints)
        );

        CommandExecutor executor = new CommandExecutor(commands);
        executor.executeCommands();

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

    private int saveAnsweredQuestion(AnsweredQuestionRequestDto answeredQuestionRequestDto, Survey survey, User user) {
        boolean isAnswered = false;
        int rewardPoints = 0;
        for (AnsweredQuestionDto answeredQuestionDto : answeredQuestionRequestDto.getAnswers()) {

            // Check if the question is required and has an empty answer.
            if (answeredQuestionDto.getIsRequired() && validateEmptyAnswer(answeredQuestionDto)) {
                throw new BadRequestExceptionMapper(ErrorMessage.NOT_ANSWER_TO_REQUIRED_QUESTION);
            }

            // Set isAnswered to true if there is at least one non-empty answer.
            if (!isAnswered && !validateEmptyAnswer(answeredQuestionDto)) {
                isAnswered = true;
            }

            QuestionBank questionBank = questionBankRepository.findByQuestionBankId(answeredQuestionDto.getQuestionBankId())
                    .orElseThrow(() -> new NotFoundExceptionMapper(ErrorMessage.QUESTION_BANK_NOT_FOUND));
            Optional<Question> question = questionRepository.findBySurveyIdAndQuestionBankId(survey.getSurveyId(), questionBank.getQuestionBankId());
            if (question.isEmpty()) {
                throw new BadRequestExceptionMapper(ErrorMessage.NOT_SURVEY_QUESTION);
            }

            // If there are no multiple choices, save the answered question
            if (answeredQuestionDto.getMultipleChoices() == null || answeredQuestionDto.getMultipleChoices().isEmpty()) {
                answeredQuestionRepository.save(answeredQuestionMapper.toAnsweredQuestion(answeredQuestionDto, user, question.get()));
            } else {
                // If there are multiple choices, map each choice to an answered question and save them all
                List<AnsweredQuestion> answeredQuestionList = answeredQuestionDto.getMultipleChoices()
                        .stream()
                        .map(choice -> answeredQuestionMapper.toAnsweredQuestionWithMultipleChoices(user, question.get(), choice))
                        .collect(Collectors.toList());
                answeredQuestionRepository.saveAll(answeredQuestionList);
            }
            // Accumulate reward points based on the answered question
            rewardPoints += getQuestionBankRewardPoints(answeredQuestionDto);
        }
        // Throw an exception if no question was answered
        if (!isAnswered) {
            throw new BadRequestExceptionMapper(ErrorMessage.ANSWER_AT_LEAST_ONE_QUESTION);
        }
        return rewardPoints;
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

    private int getQuestionBankRewardPoints(AnsweredQuestionDto answeredQuestionDto) {
        if (!validateEmptyAnswer(answeredQuestionDto)) {
            return PointUtil.calculateSurveyMaxRewardPoints(answeredQuestionDto.getQuestionType());
        }
        return 0;
    }
}
