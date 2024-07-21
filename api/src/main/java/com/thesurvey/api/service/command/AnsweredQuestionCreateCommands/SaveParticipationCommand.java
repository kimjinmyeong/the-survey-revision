package com.thesurvey.api.service.command.AnsweredQuestionCreateCommands;

import com.thesurvey.api.domain.EnumTypeEntity;
import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.service.ParticipationService;
import com.thesurvey.api.service.command.Command;
import com.thesurvey.api.service.converter.CertificationTypeConverter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SaveParticipationCommand implements Command {

    private final ParticipationService participationService;
    private final User user;
    private final Survey survey;
    private final List<Integer> surveyCertificationList;

    @Override
    public void execute() {
        List<EnumTypeEntity.CertificationType> certificationTypeList = getCertificationTypeList(surveyCertificationList);
        participationService.createParticipation(user, certificationTypeList, survey);
    }

    private List<EnumTypeEntity.CertificationType> getCertificationTypeList(List<Integer> surveyCertificationList) {
        if (surveyCertificationList.contains(EnumTypeEntity.CertificationType.NONE.getCertificationTypeId())) {
            return List.of(EnumTypeEntity.CertificationType.NONE);
        }
        return CertificationTypeConverter.toCertificationTypeList(surveyCertificationList);
    }
}
