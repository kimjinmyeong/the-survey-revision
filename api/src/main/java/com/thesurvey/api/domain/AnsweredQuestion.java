package com.thesurvey.api.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name = "answered_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnsweredQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "answered_question_id", updatable = false, nullable = false)
    private Long answeredQuestionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "survey_id", referencedColumnName = "survey_id"),
            @JoinColumn(name = "question_bank_id", referencedColumnName = "question_bank_id")
    })
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

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
                            String longAnswer, Question question, User user) {
        this.shortAnswer = shortAnswer;
        this.longAnswer = longAnswer;
        this.singleChoice = singleChoice;
        this.multipleChoice = multipleChoice;
        this.question = question;
        this.user = user;
    }
}
