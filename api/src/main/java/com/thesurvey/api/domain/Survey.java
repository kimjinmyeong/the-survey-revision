package com.thesurvey.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "survey")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Survey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "survey_id")
    private Long surveyId;

    @OneToMany(
        mappedBy = "participationId.survey",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Participation> participations;

    @Size(min = 1)
    @OneToMany(
        mappedBy = "questionId.survey",
        cascade = CascadeType.PERSIST,
        orphanRemoval = true
    )
    private List<Question> questions;

    @NotNull
    @Positive
    @Column(name = "author_id", updatable = false, nullable = false)
    private Long authorId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank
    @Size(max = 255)
    @Column(name = "description", nullable = true)
    private String description;

    @NotNull
    @Column(name = "started_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedDate;

    @NotNull
    @Future
    @Column(name = "ended_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endedDate;

    @Builder
    public Survey(Long authorId, String title, List<Question> questions,
        List<Participation> participations, String description, LocalDateTime startedDate,
        LocalDateTime endedDate) {
        this.authorId = authorId;
        this.title = title;
        this.participations = participations;
        this.questions = questions;
        this.description = description;
        this.startedDate = startedDate;
        this.endedDate = endedDate;
    }

    public void changeTitle(String title) {
        if (title.length() > 100) {
            throw new BadRequestExceptionMapper(ErrorMessage.MAX_SIZE_EXCEEDED, "제목", 100);
        }
        this.title = title;
    }

    public void changeDescription(String description) {
        if (description.length() > 255) {
            throw new BadRequestExceptionMapper(ErrorMessage.MAX_SIZE_EXCEEDED, "설명", 255);
        }
        this.description = description;
    }

    public void changeStartedDate(LocalDateTime startedDate) {
        this.startedDate = startedDate;
    }

    public void changeEndedDate(LocalDateTime endedDate) {
        this.endedDate = endedDate;
    }
}
