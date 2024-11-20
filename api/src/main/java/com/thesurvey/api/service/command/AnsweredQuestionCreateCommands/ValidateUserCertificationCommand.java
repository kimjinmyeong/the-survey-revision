package com.thesurvey.api.service.command.AnsweredQuestionCreateCommands;

import com.thesurvey.api.domain.EnumTypeEntity;
import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.ForbiddenRequestExceptionMapper;
import com.thesurvey.api.exception.mapper.UnauthorizedRequestExceptionMapper;
import com.thesurvey.api.repository.AnsweredQuestionRepository;
import com.thesurvey.api.repository.UserCertificationRepository;
import com.thesurvey.api.service.command.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ValidateUserCertificationCommand implements Command {

    private final UserCertificationRepository userCertificationRepository;
    private final AnsweredQuestionRepository answeredQuestionRepository;
    private final List<Integer> surveyCertificationList;
    private final User user;
    private final Survey survey;

    @Override
    public void execute() {
        validateUserCompletedCertification(surveyCertificationList, user.getUserId());
        validateCreateAnswerRequest(user, survey);
    }

    // validate if the user has completed the necessary certifications for the survey
    public void validateUserCompletedCertification(List<Integer> surveyCertificationList,
                                                   Long userId) {
        if (surveyCertificationList.contains(EnumTypeEntity.CertificationType.NONE.getCertificationTypeId())) {
            return;
        }
        List<Integer> userCertificationList = userCertificationRepository.findUserCertificationTypeByUserId(
                userId);
        if (!new HashSet<>(userCertificationList).containsAll(surveyCertificationList)) {
            log.warn("User: {} has not completed necessary certifications", userId);
            throw new UnauthorizedRequestExceptionMapper(ErrorMessage.CERTIFICATION_NOT_COMPLETED);
        }
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
}
