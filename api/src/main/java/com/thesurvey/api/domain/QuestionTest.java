package com.thesurvey.api.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "question_test")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", columnDefinition = "BINARY(20)")
    public Survey survey;

    @Column(name = "question_no")
    private Integer questionNo;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Builder
    public QuestionTest(Integer questionNo, Boolean isRequired, Survey survey) {
        this.questionNo = questionNo;
        this.isRequired = isRequired;
        this.survey = survey;
    }
}
