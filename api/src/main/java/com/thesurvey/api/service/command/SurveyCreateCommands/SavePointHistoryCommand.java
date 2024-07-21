package com.thesurvey.api.service.command.SurveyCreateCommands;

import com.thesurvey.api.domain.User;
import com.thesurvey.api.service.PointHistoryService;
import com.thesurvey.api.service.command.Command;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SavePointHistoryCommand implements Command {

    private final PointHistoryService pointHistoryService;
    private final User user;
    private final int surveyCreatePoints;

    @Override
    public void execute() {
        pointHistoryService.savePointHistory(user, -surveyCreatePoints);
    }
}

