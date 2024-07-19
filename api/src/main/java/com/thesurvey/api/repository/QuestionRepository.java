package com.thesurvey.api.repository;

import com.thesurvey.api.domain.Question;
import com.thesurvey.api.domain.QuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, QuestionId> {

    @Query("SELECT q FROM Question q WHERE q.questionId.survey.surveyId = :surveyId AND q.questionId.questionBank.questionBankId = :questionBankId" )
    Optional<Question> findBySurveyIdAndQuestionBankId(Long surveyId, Long questionBankId);

    @Query("SELECT q FROM Question q WHERE q.questionId.survey.surveyId = :surveyId ORDER BY q.questionNo ASC")
    List<Question> findAllBySurveyId(Long surveyId);

    @Query("SELECT q.questionNo FROM Question q WHERE q.questionId.questionBank.questionBankId = :questionBankId")
    Optional<Integer> findQuestionNoByQuestionBankId(Long questionBankId);

    @Query("SELECT q.isRequired FROM Question q WHERE q.questionId.questionBank.questionBankId = :questionBankId")
    Optional<Boolean> findIsRequiredByQuestionBankId(Long questionBankId);

    @Modifying
    @Query("DELETE FROM Question q WHERE q.questionId.survey.surveyId = :surveyId")
    void deleteBySurveyId(Long surveyId);
}
