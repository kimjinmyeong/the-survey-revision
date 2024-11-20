package com.thesurvey.api.domain;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class PointHistoryId implements Serializable {

    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;

    public PointHistoryId() {}

    public PointHistoryId(LocalDateTime transactionDate, User user) {
        this.transactionDate = transactionDate;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointHistoryId that = (PointHistoryId) o;
        return transactionDate.equals(that.transactionDate) && user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionDate, user);
    }
}
