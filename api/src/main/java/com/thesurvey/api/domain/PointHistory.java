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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pointHistoryId;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "point")
    private Integer point;

    public static final int USER_INITIAL_POINT = 18;

    @Builder
    public PointHistory(User user, LocalDateTime transactionDate, Integer point) {
        this.point = point;
        this.transactionDate = transactionDate;
        this.user = user;
    }

}
