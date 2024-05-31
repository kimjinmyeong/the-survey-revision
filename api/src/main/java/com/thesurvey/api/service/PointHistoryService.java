package com.thesurvey.api.service;

import com.thesurvey.api.domain.PointHistory;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public void savePointHistory(User user, int operandPoint) {
        int userTotalPoint = getUserTotalPoint(user.getUserId());
        pointHistoryRepository.save(
            PointHistory.builder()
                .user(user)
                .transactionDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .point(userTotalPoint + operandPoint)
                .build()
        );
    }

    @Transactional
    public Integer getUserTotalPoint(Long userId) {
        List<Integer> totalPoint = pointHistoryRepository.findPointByUserId(userId);
        return totalPoint.get(0);
    }

}
