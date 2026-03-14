package com.point.api.point.dto;

import com.point.api.point.entity.PointTxType;
import java.time.LocalDateTime;

public record PointHistoryItemResponse(
        Long pointId,
        String pointKey,
        PointTxType txType,
        long amount,
        long remainedAmount,
        String orderNo,
        String relatedPointKey,
        LocalDateTime expiredAt,
        LocalDateTime createdAt
) {
}
