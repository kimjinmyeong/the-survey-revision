package com.thesurvey.api.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionId implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    public Survey survey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_id")
    public QuestionBank questionBank;

    @Builder
    public QuestionId(Survey survey, QuestionBank questionBank) {
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
        QuestionId that = (QuestionId) o;
        return Objects.equals(survey, that.survey) && Objects.equals(questionBank,
            that.questionBank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(survey, questionBank);
    }

}
