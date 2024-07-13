package com.thesurvey.api.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class PointHistoryId implements Serializable {

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "user_id")
    private Long userId;

    public PointHistoryId() {}

    public PointHistoryId(LocalDateTime transactionDate, Long userId) {
        this.transactionDate = transactionDate;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointHistoryId that = (PointHistoryId) o;
        return transactionDate.equals(that.transactionDate) && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionDate, userId);
    }
}
