package com.thesurvey.api.domain;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;

@Embeddable
@Getter
public class QuestionId implements Serializable {

    @ManyToOne
    private Survey survey;

    @ManyToOne
    private QuestionBank questionBank;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionId that = (QuestionId) o;
        return Objects.equals(survey, that.survey) && Objects.equals(questionBank, that.questionBank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(survey, questionBank);
    }

}
