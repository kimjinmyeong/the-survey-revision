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

    @Id
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "operand_point")
    private Integer operandPoint;

    @Builder
    public PointHistory(User user, LocalDateTime transactionDate, Integer operandPoint) {
        this.transactionDate = transactionDate;
        this.user = user;
        this.operandPoint = operandPoint;
    }

}
