package com.point.api.point.dto;

import com.point.api.point.entity.PointTransactionType;
import java.time.LocalDateTime;

public record PointTransactionResponse(
        Long transactionId,
        String memberId,
        long amount,
        PointTransactionType transactionType,
        String orderId,
        LocalDateTime occurredAt
) {
}
