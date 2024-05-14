package com.thesurvey.api.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistoryId implements Serializable {

    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public PointHistoryId(LocalDateTime transactionDate, User user) {
        this.transactionDate = transactionDate;
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
        PointHistoryId that = (PointHistoryId) o;
        return Objects.equals(transactionDate, that.transactionDate) && Objects.equals(user,
            that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionDate, user);
    }
}
