package com.thesurvey.api.repository;

import com.thesurvey.api.domain.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT ph.point FROM PointHistory ph WHERE ph.user.userId = :userId ORDER BY "
        + "ph.transactionDate DESC")
    List<Integer> findPointByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT ph FROM PointHistory ph WHERE ph.user.userId = :userId ORDER BY "
            + "ph.transactionDate DESC")
    List<PointHistory> findPointHistoryByUserId(@Param("userId") Long userId);
}
