package com.thesurvey.api.service.mapper;

import com.thesurvey.api.domain.*;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionDto;
import com.thesurvey.api.util.StringUtil;

import org.springframework.stereotype.Component;

@Component
public class AnsweredQuestionMapper {

    public AnsweredQuestion toAnsweredQuestion(AnsweredQuestionDto answeredQuestionRequestDto,
                                               User user, Question question) {
        return AnsweredQuestion
                .builder()
                .user(user)
                .question(question)
                .singleChoice(answeredQuestionRequestDto.getSingleChoice())
                .shortAnswer(StringUtil.trimShortLongAnswer(answeredQuestionRequestDto.getShortAnswer(),
                        answeredQuestionRequestDto.getIsRequired()))
                .longAnswer(StringUtil.trimShortLongAnswer(answeredQuestionRequestDto.getLongAnswer(),
                        answeredQuestionRequestDto.getIsRequired()))
                .build();
    }

    public AnsweredQuestion toAnsweredQuestionWithMultipleChoices(User user, Question question, Long multipleChoice) {
        return AnsweredQuestion
                .builder()
                .user(user)
                .question(question)
                .multipleChoice(multipleChoice)
                .build();
    }

}
