package com.point.api.point.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@Getter
@Entity
@Table(name = "point_ledger")
@Check(constraints = "amount_won > 0")
@Check(constraints = "remained_amount_won >= 0")
@Check(constraints = "remained_amount_won <= amount_won")
@Check(constraints = "tx_type in ('EARN','ADMIN_GIFT','EARN_CANCEL','USE','USE_CANCEL','CANCEL_RETURN','EXPIRE')")
@Check(constraints = "((tx_type in ('EARN','ADMIN_GIFT','CANCEL_RETURN') and expired_at is not null) or (tx_type in ('EARN_CANCEL','USE','USE_CANCEL','EXPIRE')))")
@Check(constraints = "((tx_type in ('EARN','ADMIN_GIFT','CANCEL_RETURN') and remained_amount_won between 0 and amount_won) or (tx_type in ('EARN_CANCEL','USE','USE_CANCEL','EXPIRE') and remained_amount_won = 0))")
@Check(constraints = "((tx_type = 'USE' and order_id is not null) or (tx_type <> 'USE'))")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Long pointId;

    /** 멱등키 — 클라이언트가 직접 발급하는 업무 키 (예: "A", "B", "order-123-earn") */
    @Column(name = "point_key", nullable = false, unique = true, length = 64)
    private String pointKey;

    /** 동일 pointKey + 다른 페이로드 감지용 요청 해시 (SHA-256) */
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_type", nullable = false, length = 20)
    private PointTxType txType;

    /** 거래 금액 (항상 양수) */
    @Column(name = "amount_won", nullable = false)
    private long amountWon;

    /**
     * 현재 남은 사용 가능 금액.
     * EARN / ADMIN_GIFT / CANCEL_RETURN 에서만 의미가 있으며,
     * 나머지 tx_type 에서는 0.
     */
    @Column(name = "remained_amount_won", nullable = false)
    private long remainedAmountWon;

    /** 만료일. EARN / ADMIN_GIFT / CANCEL_RETURN 에서만 필수. */
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    /** 주문 연계. USE tx 에서만 필수. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    /**
     * 관련 원본 포인트 참조.
     * EARN_CANCEL → 취소 대상 EARN/ADMIN_GIFT
     * USE_CANCEL  → 취소 대상 USE
     * EXPIRE      → 만료 대상 EARN/ADMIN_GIFT
     * CANCEL_RETURN → 원인이 된 USE_CANCEL
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_point_id")
    private PointLedger relatedPoint;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private PointLedger(
            String pointKey,
            String requestHash,
            Long userId,
            PointTxType txType,
            long amountWon,
            long remainedAmountWon,
            LocalDateTime expiredAt,
            Order order,
            PointLedger relatedPoint
    ) {
        this.pointKey = pointKey;
        this.requestHash = requestHash;
        this.userId = userId;
        this.txType = txType;
        this.amountWon = amountWon;
        this.remainedAmountWon = remainedAmountWon;
        this.expiredAt = expiredAt;
        this.order = order;
        this.relatedPoint = relatedPoint;
        this.createdAt = LocalDateTime.now();
    }

    /** 사용 또는 취소 후 남은 잔액 업데이트 */
    public void deductRemained(long amount) {
        this.remainedAmountWon -= amount;
    }

    public void restoreRemained(long amount) {
        this.remainedAmountWon += amount;
    }

    public boolean isUsableLotAt(LocalDateTime at) {
        return (txType == PointTxType.EARN || txType == PointTxType.ADMIN_GIFT || txType == PointTxType.CANCEL_RETURN)
                && remainedAmountWon > 0
                && expiredAt != null
                && expiredAt.isAfter(at);
    }

    public boolean isExpiredAt(LocalDateTime at) {
        return expiredAt != null && !expiredAt.isAfter(at);
    }

    public void expireAt(LocalDateTime at) {
        this.expiredAt = at;
    }
}
