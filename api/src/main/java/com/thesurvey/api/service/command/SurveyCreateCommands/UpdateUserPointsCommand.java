package com.thesurvey.api.service.command.SurveyCreateCommands;

import com.thesurvey.api.domain.User;
import com.thesurvey.api.repository.UserRepository;
import com.thesurvey.api.service.command.Command;
import com.thesurvey.api.util.PointUtil;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateUserPointsCommand implements Command {

    private final UserRepository userRepository;
    private final User user;
    private final int surveyCreatePoints;

    @Override
    public void execute() {
        user.updatePoint(user.getPoint() - surveyCreatePoints);
        userRepository.save(user);
        PointUtil.validateUserPoint(surveyCreatePoints, user.getPoint());
    }
}
