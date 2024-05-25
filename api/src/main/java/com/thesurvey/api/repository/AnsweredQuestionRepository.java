package com.thesurvey.api.repository;

import com.thesurvey.api.domain.AnsweredQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnsweredQuestionRepository extends
    JpaRepository<AnsweredQuestion, Long> {
    @Query("SELECT aq FROM AnsweredQuestion aq WHERE aq.question.questionId.survey.surveyId = :surveyId")
    List<AnsweredQuestion> findAllBySurveyId(Long surveyId);

    @Query("SELECT aq FROM AnsweredQuestion aq WHERE aq.question.questionId.questionBank.questionBankId = :questionBankId")
    List<AnsweredQuestion> findAllByQuestionBankId(Long questionBankId);

    @Query("SELECT CASE WHEN COUNT(aq) > 0 THEN true ELSE false END FROM AnsweredQuestion aq WHERE aq.user.userId = :userId AND aq.question.questionId.survey.surveyId = :surveyId")
    boolean existsByUserIdAndSurveyId(Long userId, Long surveyId);

    @Query("SELECT aq.singleChoice, COUNT(aq) FROM AnsweredQuestion aq WHERE aq.question.questionId.questionBank.questionBankId = :questionBankId GROUP BY aq.singleChoice")
    List<Long[]> countSingleChoiceByQuestionBankId(Long questionBankId);

    @Query("SELECT aq.multipleChoice, COUNT(aq) FROM AnsweredQuestion aq WHERE aq.question.questionId.questionBank.questionBankId = :questionBankId GROUP BY aq.multipleChoice")
    List<Long[]> countMultipleChoiceByQuestionBankId(Long questionBankId);

}
