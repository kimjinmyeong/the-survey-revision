package com.thesurvey.api.util;

import com.thesurvey.api.domain.EnumTypeEntity.QuestionType;
import com.thesurvey.api.domain.QuestionBank;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import com.thesurvey.api.repository.QuestionBankRepository;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.thesurvey.api.domain.EnumTypeEntity.PointTransactionType.*;

@Component
public class PointUtil {

    private final QuestionBankRepository questionBankRepository;

    public PointUtil(QuestionBankRepository questionBankRepository) {
        this.questionBankRepository = questionBankRepository;
    }

    public int calculateSurveyCreatePoints(Long surveyId) {
        List<QuestionBank> questionBankList = questionBankRepository.findAllBySurveyId(surveyId);
        int createPoints = 0;
        for (QuestionBank questionBank : questionBankList) {
            switch (questionBank.getQuestionType()) {
                case SINGLE_CHOICE:
                    createPoints += SINGLE_CHOICE_CONSUME.getTransactionPoint();
                    break;

                case MULTIPLE_CHOICES:
                    createPoints += MULTIPLE_CHOICES_CONSUME.getTransactionPoint();
                    break;

                case SHORT_ANSWER:
                    createPoints += SHORT_ANSWER_CONSUME.getTransactionPoint();
                    break;

                case LONG_ANSWER:
                    createPoints += LONG_ANSWER_CONSUME.getTransactionPoint();
                    break;

                default:
                    throw new BadRequestExceptionMapper(ErrorMessage.INVALID_QUESTION_TYPE);
            }
        }
        return createPoints;
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

    public void validateUserPoint(int surveyCreatePoint, int userTotalPoint) {
        if (userTotalPoint - surveyCreatePoint < 0) {
            throw new BadRequestExceptionMapper(ErrorMessage.SURVEY_CREATE_POINT_NOT_ENOUGH);
        }
    }

}
