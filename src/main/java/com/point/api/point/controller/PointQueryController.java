package com.point.api.point.controller;

import com.point.api.core.api.ApiResponse;
import com.point.api.point.dto.PointHistoryResponse;
import com.point.api.point.service.PointQueryService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointQueryController {

    private final PointQueryService pointQueryService;

    @GetMapping("/history")
    public ApiResponse<PointHistoryResponse> history(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.ok(pointQueryService.getHistory(userId, startDate, endDate));
    }
}
