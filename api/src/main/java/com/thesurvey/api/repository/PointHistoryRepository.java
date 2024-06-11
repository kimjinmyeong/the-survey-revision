package com.thesurvey.api.repository;

import com.thesurvey.api.domain.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, LocalDateTime> {

}
