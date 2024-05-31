package com.thesurvey.api.util;

import com.thesurvey.api.domain.EnumTypeEntity.QuestionType;
import com.thesurvey.api.domain.QuestionBank;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import com.thesurvey.api.repository.QuestionBankRepository;
import com.thesurvey.api.service.PointHistoryService;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.thesurvey.api.domain.EnumTypeEntity.PointTransactionType.*;

@Component
public class PointUtil {

    private final QuestionBankRepository questionBankRepository;

    private final PointHistoryService pointHistoryService;

    public PointUtil(QuestionBankRepository questionBankRepository, PointHistoryService pointHistoryService) {
        this.questionBankRepository = questionBankRepository;
        this.pointHistoryService = pointHistoryService;
    }

    public int calculateSurveyCreatePoints(QuestionType questionType) {
        switch (questionType) {
            case SINGLE_CHOICE:
                return SINGLE_CHOICE_CONSUME.getTransactionPoint();

            case MULTIPLE_CHOICES:
                return MULTIPLE_CHOICES_CONSUME.getTransactionPoint();

            case SHORT_ANSWER:
                return SHORT_ANSWER_CONSUME.getTransactionPoint();

            case LONG_ANSWER:
                return LONG_ANSWER_CONSUME.getTransactionPoint();

            default:
                throw new BadRequestExceptionMapper(ErrorMessage.INVALID_QUESTION_TYPE);
        }
    }

    public int calculateSurveyMaxRewardPoints(QuestionType questionType) {
        switch (questionType) {
            case SINGLE_CHOICE:
                return SINGLE_CHOICE_REWARD.getTransactionPoint();

            case MULTIPLE_CHOICES:
                return MULTIPLE_CHOICES_REWARD.getTransactionPoint();

            case SHORT_ANSWER:
                return SHORT_ANSWER_REWARD.getTransactionPoint();

            case LONG_ANSWER:
                return LONG_ANSWER_REWARD.getTransactionPoint();

            default:
                throw new BadRequestExceptionMapper(ErrorMessage.INVALID_QUESTION_TYPE);
        }
    }

    /**
     * {@code maxRewardPoints} is the amount of points a user get when they answer all the
     * questions in the survey.
     */
    public int getSurveyMaxRewardPoints(Long surveyId) {
        List<QuestionBank> questionBankList = questionBankRepository.findAllBySurveyId(surveyId);
        int maxRewardPoints = questionBankList.stream()
                .mapToInt(questionBank -> calculateSurveyMaxRewardPoints(questionBank.getQuestionType()))
                .sum();
        return maxRewardPoints;
    }

    public void validateUserPoint(int surveyCreatePoint, Long userId) {
        int userTotalPoint = pointHistoryService.getUserTotalPoint(userId);
        if (userTotalPoint - surveyCreatePoint < 0) {
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_CREATE_POINT_NOT_ENOUGH);
        }
    }

}
