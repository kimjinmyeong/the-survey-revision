package com.thesurvey.api.domain;

import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Set;

@Entity
@Table(name = "question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @EmbeddedId
    private QuestionId questionId;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Set<AnsweredQuestion> answeredQuestion;

    @NotNull
    @Positive
    @Column(name = "question_no", nullable = false)
    private Integer questionNo;

    @NotNull
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    @Builder
    public Question(QuestionBank questionBank, Survey survey, Integer questionNo, Boolean isRequired) {
        this.questionNo = questionNo;
        this.isRequired = isRequired;
        this.questionId = QuestionId.builder()
                .survey(survey)
                .questionBank(questionBank)
                .build();
    }

    public void changeQuestionNo(Integer questionNo) {
        if (questionNo <= 0) {
            throw new BadRequestExceptionMapper(ErrorMessage.POSITIVE_VALUE_REQUIRED, "질문번호");
        }
        this.questionNo = questionNo;
    }

    public void changeIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }
}
