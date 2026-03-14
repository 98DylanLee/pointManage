package com.point.api.point.dto;

import com.point.api.point.entity.PointTxType;
import java.time.LocalDateTime;

public record PointEarnResponse(
        Long pointId,
        String pointKey,
        Long userId,
        PointTxType txType,
        long amount,
        long remainedAmount,
        LocalDateTime expiredAt,
        LocalDateTime createdAt
) {
}
