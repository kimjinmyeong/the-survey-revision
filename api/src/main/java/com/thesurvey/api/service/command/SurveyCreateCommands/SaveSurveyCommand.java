package com.thesurvey.api.service.command.SurveyCreateCommands;

import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.dto.request.survey.SurveyRequestDto;
import com.thesurvey.api.repository.SurveyRepository;
import com.thesurvey.api.service.command.Command;
import com.thesurvey.api.service.mapper.SurveyMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveSurveyCommand implements Command {

    private final SurveyRepository surveyRepository;
    private final SurveyMapper surveyMapper;
    private final SurveyRequestDto surveyRequestDto;
    private final User user;

    @Getter
    private Survey survey;

    @Override
    public void execute() {
        survey = surveyRepository.save(surveyMapper.toSurvey(surveyRequestDto, user.getUserId()));
    }

}

