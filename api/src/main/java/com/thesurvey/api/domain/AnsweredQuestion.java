package com.thesurvey.api.domain;

import javax.persistence.*;
import javax.validation.constraints.Size;

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
    @AttributeOverride(name = "answerId", column = @Column(name = "answer_id"))
    private AnsweredQuestionId answeredQuestionId;

    @Column(name = "single_choice", nullable = true)
    private Long singleChoice;

    @Column(name = "multiple_choices", nullable = true)
    private Long multipleChoice;

    @Size(max = 100)
    @Column(name = "short_answer", nullable = true)
    private String shortAnswer;

    @Size(max = 255)
    @Column(name = "long_answer", nullable = true)
    private String longAnswer;

    @Builder
    public AnsweredQuestion(Long singleChoice, Long multipleChoice, String shortAnswer,
        String longAnswer, Survey survey, User user, QuestionBank questionBank) {
        this.shortAnswer = shortAnswer;
        this.longAnswer = longAnswer;
        this.singleChoice = singleChoice;
        this.multipleChoice = multipleChoice;
        this.answeredQuestionId = AnsweredQuestionId.builder()
            .survey(survey)
            .user(user)
            .questionBank(questionBank)
            .build();
    }
}
