package com.thesurvey.api.service.command.SurveyCreateCommands;

import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.dto.request.survey.SurveyRequestDto;
import com.thesurvey.api.service.QuestionService;
import com.thesurvey.api.service.command.Command;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveQuestionsCommand implements Command {
    private final QuestionService questionService;
    private final SurveyRequestDto surveyRequestDto;
    private final Survey survey;

    @Override
    public void execute() {
        questionService.createQuestion(surveyRequestDto.getQuestions(), survey);
    }
}
