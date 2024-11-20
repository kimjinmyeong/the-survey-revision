package com.thesurvey.api.service.command.AnsweredQuestionCreateCommands;

import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionRequestDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.NotFoundExceptionMapper;
import com.thesurvey.api.repository.SurveyRepository;
import com.thesurvey.api.service.command.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FetchSurveyCommand implements Command {
    private final SurveyRepository surveyRepository;
    private final AnsweredQuestionRequestDto answeredQuestionRequestDto;

    @Getter
    private Survey survey;

    @Override
    public void execute() {
        survey = surveyRepository.findBySurveyId(answeredQuestionRequestDto.getSurveyId())
                .orElseThrow(() -> new NotFoundExceptionMapper(ErrorMessage.SURVEY_NOT_FOUND));
    }

}
