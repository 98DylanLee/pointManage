package com.point.api.point.dto;

import com.point.api.point.entity.PointTxType;
import java.time.LocalDateTime;

public record PointEarnCancelResponse(
        Long pointId,
        String pointKey,
        String originalPointKey,
        Long userId,
        PointTxType txType,
        long amount,
        LocalDateTime createdAt
) {
}
