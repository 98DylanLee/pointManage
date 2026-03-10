package com.point.api.point.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String memberId;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PointTransactionType transactionType;

    @Column(nullable = false, length = 100)
    private String orderId;

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Builder
    private PointTransaction(
            String memberId,
            long amount,
            PointTransactionType transactionType,
            String orderId,
            String description,
            LocalDateTime occurredAt
    ) {
        this.memberId = memberId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.orderId = orderId;
        this.description = description;
        this.occurredAt = occurredAt;
    }
}
