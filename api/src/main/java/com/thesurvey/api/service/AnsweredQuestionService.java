package com.thesurvey.api.service;

import com.thesurvey.api.domain.AnsweredQuestion;
import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;
import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionRequestDto;
import com.thesurvey.api.dto.response.answeredQuestion.AnsweredQuestionRewardPointDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.UnauthorizedRequestExceptionMapper;
import com.thesurvey.api.repository.*;
import com.thesurvey.api.service.command.AnsweredQuestionCreateCommands.*;
import com.thesurvey.api.service.command.Command;
import com.thesurvey.api.service.command.CommandExecutor;
import com.thesurvey.api.service.command.SurveyCreateCommands.UpdateUserPointsCommand;
import com.thesurvey.api.service.mapper.AnsweredQuestionMapper;
import com.thesurvey.api.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

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
    private final PointHistoryService pointHistoryService;
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

        // Execute validation and fetch survey command
        FetchSurveyCommand validateAndFetchSurveyCommand = new FetchSurveyCommand(surveyRepository, answeredQuestionRequestDto);
        validateAndFetchSurveyCommand.execute();
        Survey survey = validateAndFetchSurveyCommand.getSurvey();
        List<Integer> surveyCertificationList = surveyRepository.findCertificationTypeBySurveyIdAndAuthorId(survey.getSurveyId(), survey.getAuthorId());

        // Execute validation and fetch rewardPoints
        new ValidateUserCertificationCommand(userCertificationRepository, answeredQuestionRepository, surveyCertificationList, user, survey).execute();
        SaveAnswerCommand saveAnswerCommand = new SaveAnswerCommand(answeredQuestionRepository, answeredQuestionMapper, questionRepository, questionBankRepository, answeredQuestionRequestDto, user, survey);
        saveAnswerCommand.execute();
        int rewardPoints = saveAnswerCommand.getRewardPoints();

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
}
