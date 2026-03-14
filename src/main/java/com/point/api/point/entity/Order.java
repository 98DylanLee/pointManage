package com.point.api.point.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Check;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@Check(constraints = "total_amount_won >= 0")
@Check(constraints = "point_usage_amount_won >= 0")
@Check(constraints = "payment_amount_won >= 0")
@Check(constraints = "payment_amount_won = total_amount_won - point_usage_amount_won")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "request_key", nullable = false, unique = true, length = 64)
    private String requestKey;

    @Column(name = "total_amount_won", nullable = false)
    private long totalAmountWon;

    @Column(name = "point_usage_amount_won", nullable = false)
    private long pointUsageAmountWon;

    @Column(name = "payment_amount_won", nullable = false)
    private long paymentAmountWon;

    @Column(name = "ordered_at", nullable = false, updatable = false)
    private LocalDateTime orderedAt;

    @Builder
    private Order(
            String orderNo,
            Long userId,
            String requestKey,
            long totalAmountWon,
            long pointUsageAmountWon,
            long paymentAmountWon
    ) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.requestKey = requestKey;
        this.totalAmountWon = totalAmountWon;
        this.pointUsageAmountWon = pointUsageAmountWon;
        this.paymentAmountWon = paymentAmountWon;
        this.orderedAt = LocalDateTime.now();
    }
}
