package com.thesurvey.api.service;

import com.thesurvey.api.domain.EnumTypeEntity;
import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.dto.request.survey.SurveyRequestDto;
import com.thesurvey.api.dto.response.survey.SurveyResponseDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import com.thesurvey.api.repository.SurveyRepository;
import com.thesurvey.api.repository.UserRepository;
import com.thesurvey.api.service.mapper.SurveyMapper;
import com.thesurvey.api.util.PointUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyTransactionService {

    private final PointHistoryService pointHistoryService;
    private final SurveyRepository surveyRepository;
    private final QuestionService questionService;
    private final SurveyMapper surveyMapper;
    private final ParticipationService participationService;
    private final UserRepository userRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(value = "surveyListCache", allEntries = true)
    public SurveyResponseDto createSurveyTransactional(SurveyRequestDto surveyRequestDto, User user,
                                                       List<EnumTypeEntity.CertificationType> certificationTypes) {
        validateCreateSurvey(surveyRequestDto, user);

        int surveyCreatePoints = surveyRequestDto.getQuestions().stream()
                .mapToInt(questionRequestDto -> PointUtil.calculateSurveyCreatePoints(questionRequestDto.getQuestionType()))
                .sum();
        pointHistoryService.savePointHistory(user, -surveyCreatePoints);
        Survey survey = surveyRepository.save(surveyMapper.toSurvey(surveyRequestDto, user.getUserId()));
        questionService.createQuestion(surveyRequestDto.getQuestions(), survey);
        participationService.createParticipation(user, certificationTypes, survey);
        return surveyMapper.toSurveyResponseDto(survey, user.getUserId());
    }

    private void validateCreateSurvey(SurveyRequestDto surveyRequestDto, User user) {
        validateSurveyDates(surveyRequestDto);
        int surveyCreatePoints = surveyRequestDto.getQuestions().stream()
                .mapToInt(questionRequestDto -> PointUtil.calculateSurveyCreatePoints(questionRequestDto.getQuestionType()))
                .sum();
        validateUserPoint(user, surveyCreatePoints);
        validateRecentSurveyCreation(user);
    }

    public void validateUserPoint(User user, Integer surveyCreatePoints) {
        if (user.getPoint() - surveyCreatePoints < 0) {
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_CREATE_POINT_NOT_ENOUGH);
        }
        user.updatePoint(user.getPoint() - surveyCreatePoints);
        userRepository.saveAndFlush(user);
    }



    private void validateSurveyDates(SurveyRequestDto surveyRequestDto) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // Validate that the survey's start date is not more than 5 seconds in the past.
        if (surveyRequestDto.getStartedDate().isBefore(now.minusSeconds(5))) {
            throw new BadRequestExceptionMapper(ErrorMessage.STARTEDDATE_ISBEFORE_CURRENTDATE);
        }

        // Validate that the survey's start date is not after its end date.
        if (surveyRequestDto.getStartedDate().isAfter(surveyRequestDto.getEndedDate())) {
            throw new BadRequestExceptionMapper(ErrorMessage.STARTEDDATE_ISAFTER_ENDEDDATE);
        }
    }

    private void validateRecentSurveyCreation(User user) {
        List<Survey> surveys = surveyRepository.findUserCreatedSurveysByAuthorID(user.getUserId());
        if (surveys.isEmpty()) {
            return;
        }
        LocalDateTime userRecentCreateTime = surveys.get(surveys.size() - 1).getCreatedDate();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        Duration duration = Duration.between(userRecentCreateTime, now);
        if (duration.getSeconds() < 30) {
            throw new BadRequestExceptionMapper(ErrorMessage.USER_CREATE_SURVEY_RECENT);
        }
    }

}
