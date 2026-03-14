package com.point.api.point.dto;

import java.time.LocalDate;
import java.util.List;

public record PointHistoryResponse(
        Long userId,
        LocalDate startDate,
        LocalDate endDate,
        List<PointHistoryItemResponse> histories
) {
}
