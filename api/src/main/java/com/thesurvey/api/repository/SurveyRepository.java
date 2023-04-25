package com.thesurvey.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.thesurvey.api.domain.Survey;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, UUID> {

    @Query("SELECT s FROM Survey s ORDER BY s.createdDate DESC")
    Page<Survey> findAllInDescendingOrder(Pageable pageable);

    @Query("SELECT p.certificationType FROM Participation p WHERE p.survey.surveyId = :survey_id")
    List<Integer> findCertificationTypeBySurveyId(@Param("survey_id") UUID surveyId);

    Optional<Survey> findBySurveyId(UUID surveyId);

}
