package com.thesurvey.api.util;

import com.thesurvey.api.domain.EnumTypeEntity.QuestionType;
import com.thesurvey.api.domain.QuestionBank;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.thesurvey.api.domain.EnumTypeEntity.PointTransactionType.*;

@Component
public class PointUtil {

    public static int calculateSurveyCreatePoints(QuestionType questionType) {
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

    public static int calculateSurveyMaxRewardPoints(QuestionType questionType) {
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
    public static int getSurveyMaxRewardPoints(List<QuestionBank> questionBankList) {
        return questionBankList.stream()
            .mapToInt(questionBank -> calculateSurveyMaxRewardPoints(questionBank.getQuestionType()))
            .sum();
    }

    public static void validateUserPoint(int surveyCreatePoint, int userTotalPoint) {
        if (userTotalPoint - surveyCreatePoint < 0) {
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_CREATE_POINT_NOT_ENOUGH);
        }
    }

}
