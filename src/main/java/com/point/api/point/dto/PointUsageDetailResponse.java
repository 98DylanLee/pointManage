package com.point.api.point.dto;

import com.point.api.point.entity.PointUsageDetailType;

public record PointUsageDetailResponse(
        PointUsageDetailType detailType,
        String sourcePointKey,
        String targetPointKey,
        long amount
) {
}
