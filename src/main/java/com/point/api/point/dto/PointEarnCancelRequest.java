package com.point.api.point.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PointEarnCancelRequest(
        @NotNull Long userId,
        @NotBlank String pointKey,
        @NotBlank String originalPointKey,
        String description
) {
}
