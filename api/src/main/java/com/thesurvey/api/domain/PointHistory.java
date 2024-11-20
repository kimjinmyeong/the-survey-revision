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
    @AttributeOverride(name = "transactionDate", column = @Column(name = "transaction_date"))
    private PointHistoryId pointHistoryId;

    @Column(name = "operand_point")
    private Integer operandPoint;

    @Builder
    public PointHistory(User user, LocalDateTime transactionDate, Integer operandPoint) {
        this.pointHistoryId = new PointHistoryId(transactionDate, user);
        this.operandPoint = operandPoint;
    }

}
