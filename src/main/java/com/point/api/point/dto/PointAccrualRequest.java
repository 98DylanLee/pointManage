package com.point.api.point.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PointAccrualRequest(
        @NotBlank String memberId,
        @Min(1) long amount,
        @NotBlank String orderId,
        String description
) {
}
