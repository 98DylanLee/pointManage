package com.point.api.point.controller;

import com.point.api.core.api.ApiResponse;
import com.point.api.point.dto.PointEarnCancelRequest;
import com.point.api.point.dto.PointEarnCancelResponse;
import com.point.api.point.dto.PointEarnRequest;
import com.point.api.point.dto.PointEarnResponse;
import com.point.api.point.dto.PointUseCancelRequest;
import com.point.api.point.dto.PointUseCancelResponse;
import com.point.api.point.dto.PointUseRequest;
import com.point.api.point.dto.PointUseResponse;
import com.point.api.point.service.PointCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointCommandController {

    private final PointCommandService pointCommandService;

    @PostMapping("/earn")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PointEarnResponse> earn(@Valid @RequestBody PointEarnRequest request) {
        return ApiResponse.ok(pointCommandService.earn(request));
    }

    @PostMapping("/earn-cancel")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PointEarnCancelResponse> cancelEarn(@Valid @RequestBody PointEarnCancelRequest request) {
        return ApiResponse.ok(pointCommandService.cancelEarn(request));
    }

    @PostMapping("/use")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PointUseResponse> use(@Valid @RequestBody PointUseRequest request) {
        return ApiResponse.ok(pointCommandService.use(request));
    }

    @PostMapping("/use-cancel")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PointUseCancelResponse> cancelUse(@Valid @RequestBody PointUseCancelRequest request) {
        return ApiResponse.ok(pointCommandService.cancelUse(request));
    }
}
