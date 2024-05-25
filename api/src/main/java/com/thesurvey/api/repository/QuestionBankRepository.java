package com.thesurvey.api.repository;

import java.util.List;
import java.util.Optional;

import com.thesurvey.api.domain.QuestionBank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long> {

    Optional<QuestionBank> findByQuestionBankId(Long questionBankId);

    @Query("SELECT qb FROM QuestionBank qb JOIN FETCH qb.questions q JOIN q.questionId.survey s WHERE s.surveyId= :surveyId ORDER BY q.questionNo ASC")
    List<QuestionBank> findAllBySurveyId(Long surveyId);

    @Query("SELECT qb FROM QuestionBank qb JOIN FETCH qb.questions q JOIN q.questionId.survey s WHERE s.surveyId = :surveyId AND qb.title = :title")
    Optional<QuestionBank> findBySurveyIdAndTitle(Long surveyId, String title);

}
