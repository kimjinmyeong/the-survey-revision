package com.thesurvey.api.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

    @EmbeddedId
    private PointHistoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")  // This is the name of the attribute in PointHistoryId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "operand_point")
    private Integer operandPoint;

    @Builder
    public PointHistory(User user, LocalDateTime transactionDate, Integer operandPoint) {
        this.id = new PointHistoryId(transactionDate, user.getUserId());
        this.user = user;
        this.operandPoint = operandPoint;
    }

}
