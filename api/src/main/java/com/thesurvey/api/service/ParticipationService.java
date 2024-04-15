package com.thesurvey.api.service;

import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;
import com.thesurvey.api.domain.Participation;
import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.repository.ParticipationRepository;
import com.thesurvey.api.service.mapper.ParticipationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;

    private final ParticipationMapper participationMapper;

    @Transactional
    public void createParticipation(User user, List<CertificationType> certificationTypes,
        Survey survey) {
        certificationTypes.stream()
            .map((certificationType) -> participationMapper.toParticipation(user, survey,
                certificationType))
            .forEach(participationRepository::save);
    }

    @Transactional
    public void deleteParticipation(UUID surveyId) {
        List<Participation> participationList = participationRepository.findAllBySurveyId(surveyId);
        participationRepository.deleteAll(participationList);
    }
}
