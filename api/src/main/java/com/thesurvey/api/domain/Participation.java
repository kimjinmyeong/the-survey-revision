package com.thesurvey.api.domain;

import java.time.LocalDateTime;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "participation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participation extends BaseTimeEntity {

    @EmbeddedId
    @AttributeOverride(name = "certificationType", column = @Column(name = "certification_type", insertable = false, updatable = false))
    private ParticipationId participationId;

    @Column(name = "participate_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime participateDate;

    @Column(name = "submitted_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedDate;

    @Builder
    public Participation(Survey survey, User user,
        LocalDateTime participateDate, LocalDateTime submittedDate,
        CertificationType certificationType) {
        this.participateDate = participateDate;
        this.submittedDate = submittedDate;
        this.participationId = ParticipationId.builder()
            .certificationType(certificationType)
            .survey(survey)
            .user(user)
            .build();
    }

}
