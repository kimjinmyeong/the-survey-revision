package com.thesurvey.api.repository;

import com.thesurvey.api.domain.Survey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @Query("SELECT s FROM Survey s WHERE s.endedDate > CURRENT_TIMESTAMP ORDER BY s.createdDate DESC")
    Page<Survey> findAllInDescendingOrder(Pageable pageable);

    @Query("SELECT p.participationId.certificationType FROM Participation p WHERE p.participationId.survey.surveyId = :surveyId AND p.participationId.user.userId = :authorId")
    List<Integer> findCertificationTypeBySurveyIdAndAuthorId(@Param("surveyId") Long surveyId, @Param("authorId") Long authorId);

    Optional<Survey> findBySurveyId(Long surveyId);

    @Query("SELECT s FROM Survey s WHERE s.authorId = :authorId ORDER BY s.createdDate DESC")
    List<Survey> findUserCreatedSurveysByAuthorID(@Param("authorId") Long authorId);

}
