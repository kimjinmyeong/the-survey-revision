package com.thesurvey.api.domain;

import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

    @EmbeddedId
    @AttributeOverride(name = "transactionDate", column = @Column(name = "transaction_date", insertable = false, updatable = false))
    private PointHistoryId pointHistoryId;

    @Column(name = "point")
    private Integer point;

    public static final int USER_INITIAL_POINT = 50;

    @Builder
    public PointHistory(User user, LocalDateTime transactionDate, Integer point) {
        this.point = point;
        this.pointHistoryId = PointHistoryId.builder()
            .transactionDate(transactionDate)
            .user(user)
            .build();
    }

}
