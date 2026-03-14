package com.point.api.point.dto;

import com.point.api.point.entity.PointTxType;
import java.time.LocalDateTime;
import java.util.List;

public record PointUseResponse(
        Long pointId,
        String pointKey,
        Long userId,
        String orderNo,
        PointTxType txType,
        long amount,
        long balanceAfter,
        LocalDateTime createdAt,
        List<PointUsageDetailResponse> usageDetails
) {
}
