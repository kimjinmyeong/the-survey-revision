package com.thesurvey.api.domain;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answered_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnsweredQuestion {

    @EmbeddedId
    private AnsweredQuestionId answeredQuestionId;

    @MapsId("surveyId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    public Survey survey;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;

    @MapsId("questionBankId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_id")
    public QuestionBank questionBank;

    @Column(name = "single_choice", nullable = true)
    private Integer singleChoice;

    @Column(name = "multiple_choices", nullable = true)
    private Integer multipleChoice;

    @Column(name = "short_answer", nullable = true)
    private String shortAnswer;

    @Column(name = "long_answer", nullable = true)
    private String longAnswer;

    @Builder
    public AnsweredQuestion(Integer singleChoice, Integer multipleChoice, String shortAnswer,
        String longAnswer, Survey survey, User user, QuestionBank questionBank) {
        this.user = user;
        this.survey = survey;
        this.questionBank = questionBank;
        this.shortAnswer = shortAnswer;
        this.longAnswer = longAnswer;
        this.singleChoice = singleChoice;
        this.multipleChoice = multipleChoice;
        this.answeredQuestionId = AnsweredQuestionId.builder()
            .surveyId(survey.getSurveyId())
            .userId(user.getUserId())
            .questionBankId(questionBank.getQuestionBankId())
            .build();
    }
}
