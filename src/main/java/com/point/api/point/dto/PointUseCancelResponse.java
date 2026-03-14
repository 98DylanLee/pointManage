package com.point.api.point.dto;

import com.point.api.point.entity.PointTxType;
import java.time.LocalDateTime;
import java.util.List;

public record PointUseCancelResponse(
        Long pointId,
        String pointKey,
        String originalUsePointKey,
        Long userId,
        PointTxType txType,
        long amount,
        long balanceAfter,
        long remainingCancelableAmount,
        LocalDateTime createdAt,
        List<PointUsageDetailResponse> usageDetails
) {
}
