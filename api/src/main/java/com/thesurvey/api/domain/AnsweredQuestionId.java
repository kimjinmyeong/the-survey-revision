package com.thesurvey.api.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnsweredQuestionId implements Serializable {

    private UUID answerId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", columnDefinition = "uuid")
    public Survey survey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_id")
    public QuestionBank questionBank;

    @Builder
    public AnsweredQuestionId(User user, Survey survey, QuestionBank questionBank) {
        this.user = user;
        this.survey = survey;
        this.questionBank = questionBank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnsweredQuestionId that = (AnsweredQuestionId) o;
        return Objects.equals(user, that.user) && Objects.equals(survey, that.survey)
            && Objects.equals(questionBank, that.questionBank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, survey, questionBank);
    }

}
