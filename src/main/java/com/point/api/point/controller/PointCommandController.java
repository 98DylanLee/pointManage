package com.point.api.point.controller;

import com.point.api.point.dto.PointAccrualRequest;
import com.point.api.point.dto.PointTransactionResponse;
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

    @PostMapping("/accruals")
    @ResponseStatus(HttpStatus.CREATED)
    public PointTransactionResponse accrue(@Valid @RequestBody PointAccrualRequest request) {
        return pointCommandService.accrue(request);
    }
}
