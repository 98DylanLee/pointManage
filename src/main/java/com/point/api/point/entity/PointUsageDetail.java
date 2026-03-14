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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@Getter
@Entity
@Table(
        name = "point_usage_detail",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_point_usage_detail_point_type_source",
                columnNames = {"point_id", "detail_type", "source_point_id"}
        )
)
@Check(constraints = "amount_won > 0")
@Check(constraints = "detail_type in ('USE','USE_CANCEL_RESTORE','USE_CANCEL_REGRANT')")
@Check(constraints = "((detail_type in ('USE','USE_CANCEL_RESTORE') and target_point_id is null) or (detail_type = 'USE_CANCEL_REGRANT' and target_point_id is not null))")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointUsageDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_detail_id")
    private Long usageDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id", nullable = false)
    private PointLedger point;

    @Enumerated(EnumType.STRING)
    @Column(name = "detail_type", nullable = false, length = 30)
    private PointUsageDetailType detailType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_point_id", nullable = false)
    private PointLedger sourcePoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_point_id")
    private PointLedger targetPoint;

    @Column(name = "amount_won", nullable = false)
    private long amountWon;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private PointUsageDetail(
            PointLedger point,
            PointUsageDetailType detailType,
            Order order,
            PointLedger sourcePoint,
            PointLedger targetPoint,
            long amountWon
    ) {
        this.point = point;
        this.detailType = detailType;
        this.order = order;
        this.sourcePoint = sourcePoint;
        this.targetPoint = targetPoint;
        this.amountWon = amountWon;
        this.createdAt = LocalDateTime.now();
    }
}
