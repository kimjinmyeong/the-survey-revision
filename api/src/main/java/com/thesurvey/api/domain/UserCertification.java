package com.thesurvey.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;

import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_certification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCertification {

    @EmbeddedId
    @AttributeOverride(name = "certificationType", column = @Column(name = "certification_type", insertable = false, updatable = false))
    private UserCertificationId userCertificationId;

    @Column(name = "certification_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime certificationDate;

    @Column(name = "expiration_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expirationDate;

    @Builder
    public UserCertification(User user, CertificationType certificationType,
        LocalDateTime certificationDate, LocalDateTime expirationDate) {
        this.certificationDate = certificationDate;
        this.userCertificationId = UserCertificationId.builder()
            .user(user)
            .certificationType(certificationType)
            .build();
        this.expirationDate = expirationDate;
    }

}