package com.point.api.point.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PointEarnRequest(
        @NotNull Long userId,

        /** 적립 금액. 정책의 max_earn_per_tx_won 이하여야 한다. */
        @Min(1) long amount,

        /** 멱등키. 클라이언트가 직접 발급. */
        @NotBlank String pointKey,

        /** true 이면 관리자 수기 지급 (ADMIN_GIFT), false 이면 일반 적립 (EARN) */
        boolean adminGift,

        /** 만료일(일 단위). null 이면 정책 기본값(default_expire_days) 적용. */
        Integer expireDays,

        String description
) {
}
