package com.thesurvey.api.service.command.AnsweredQuestionCreateCommands;

import com.thesurvey.api.domain.User;
import com.thesurvey.api.service.PointHistoryService;
import com.thesurvey.api.service.command.Command;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SavePointHistoryCommand implements Command {

    private final User user;
    private final PointHistoryService pointHistoryService;
    private final int rewardPoints;

    @Override
    public void execute() {
        pointHistoryService.savePointHistory(user, rewardPoints);
    }
}
