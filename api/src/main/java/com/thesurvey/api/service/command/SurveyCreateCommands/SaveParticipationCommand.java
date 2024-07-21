package com.thesurvey.api.service.command.SurveyCreateCommands;

import com.thesurvey.api.domain.EnumTypeEntity;
import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.service.ParticipationService;
import com.thesurvey.api.service.command.Command;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SaveParticipationCommand implements Command {

    private final ParticipationService participationService;
    private final User user;
    private final List<EnumTypeEntity.CertificationType> certificationTypes;
    private final Survey survey;

    @Override
    public void execute() {
        participationService.createParticipation(user, certificationTypes, survey);
    }
}
