package com.point.api.point.service;

import com.point.api.point.dto.PointHistoryItemResponse;
import com.point.api.point.dto.PointHistoryResponse;
import com.point.api.point.entity.PointLedger;
import com.point.api.point.repository.PointLedgerRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointQueryServiceImpl implements PointQueryService {

    private final PointLedgerRepository pointLedgerRepository;

    @Override
    public PointHistoryResponse getHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate 는 startDate 보다 빠를 수 없습니다.");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

        List<PointHistoryItemResponse> histories = pointLedgerRepository
                .findHistoryByUserIdAndCreatedAtBetween(userId, startDateTime, endExclusive)
                .stream()
                .map(this::toHistoryItem)
                .toList();

        return new PointHistoryResponse(userId, startDate, endDate, histories);
    }

    private PointHistoryItemResponse toHistoryItem(PointLedger ledger) {
        return new PointHistoryItemResponse(
                ledger.getPointId(),
                ledger.getPointKey(),
                ledger.getTxType(),
                ledger.getAmountWon(),
                ledger.getRemainedAmountWon(),
                ledger.getOrder() != null ? ledger.getOrder().getOrderNo() : null,
                ledger.getRelatedPoint() != null ? ledger.getRelatedPoint().getPointKey() : null,
                ledger.getExpiredAt(),
                ledger.getCreatedAt()
        );
    }
}
