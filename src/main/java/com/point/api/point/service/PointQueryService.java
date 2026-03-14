package com.point.api.point.service;

import com.point.api.point.dto.PointHistoryResponse;
import java.time.LocalDate;

public interface PointQueryService {

    PointHistoryResponse getHistory(Long userId, LocalDate startDate, LocalDate endDate);
}
