package com.thesurvey.api.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.*;

import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipationId implements Serializable {

    /*
     * the value of id is set to the index of EnumTypeEntity.CertificationType.
     * the value of id : CertificationType
     * 0: NONE, 1: KAKAO, 2: NAVER, 3: GOOGLE, 4: WEBMAIL, 5: DRIVER_LICENSE, 6: IDENTITY_CARD
     */
    private CertificationType certificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", columnDefinition = "uuid")
    public Survey survey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;

    @Builder
    public ParticipationId(CertificationType certificationType, Survey survey, User user) {
        this.certificationType = certificationType;
        this.survey = survey;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParticipationId that = (ParticipationId) o;
        return Objects.equals(survey, that.survey) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(survey, user);
    }
}
