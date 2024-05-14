package com.thesurvey.api.domain;

import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;
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
public class UserCertificationId implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /*
     * the value of id is set to the index of EnumTypeEntity.CertificationType.
     * the value of id : CertificationType
     * 0: NONE, 1: KAKAO, 2: NAVER, 3: GOOGLE, 4: WEBMAIL, 5: DRIVER_LICENSE, 6: IDENTITY_CARD
     */
    private CertificationType certificationType;

    @Builder
    public UserCertificationId(User user, CertificationType certificationType) {
        this.user = user;
        this.certificationType = certificationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserCertificationId that = (UserCertificationId) o;
        return Objects.equals(user, that.user) && Objects.equals(certificationType,
                that.certificationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, certificationType);
    }

}