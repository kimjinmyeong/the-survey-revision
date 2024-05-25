package com.thesurvey.api.repository;

import com.thesurvey.api.domain.Participation;
import com.thesurvey.api.domain.ParticipationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, ParticipationId> {

    @Query("SELECT p FROM Participation p WHERE p.participationId.survey.surveyId = :surveyId")
    List<Participation> findAllBySurveyId(Long surveyId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Participation p WHERE p.participationId.user.userId = :userId AND p.participationId.survey.surveyId = :surveyId")
    boolean existsByUserIdAndSurveyId(Long userId, Long surveyId);
}
