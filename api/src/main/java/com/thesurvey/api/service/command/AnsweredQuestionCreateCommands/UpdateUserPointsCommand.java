package com.thesurvey.api.service.command.AnsweredQuestionCreateCommands;

import com.thesurvey.api.domain.User;
import com.thesurvey.api.repository.UserRepository;
import com.thesurvey.api.service.command.Command;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateUserPointsCommand implements Command {

    private final UserRepository userRepository;
    private final User user;
    private final int rewardPoints;

    @Override
    public void execute() {
        user.updatePoint(user.getPoint() + rewardPoints);
        userRepository.save(user);
    }
}

