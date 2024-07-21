package com.thesurvey.api.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandExecutor {

    private final List<Command> commands;

    public void executeCommands() {
        for (Command command : commands) {
            command.execute();
        }
    }
}

