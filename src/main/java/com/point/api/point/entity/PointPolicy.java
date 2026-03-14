package com.point.api.point.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointPolicy {

    @Id
    @Column(name = "policy_code", length = 30)
    private String policyCode;

    @Column(name = "max_earn_per_tx_won", nullable = false)
    private long maxEarnPerTxWon;

    @Column(name = "max_balance_won", nullable = false)
    private long maxBalanceWon;

    @Column(name = "default_expire_days", nullable = false)
    private int defaultExpireDays;

    @Column(name = "min_expire_days", nullable = false)
    private int minExpireDays;

    @Column(name = "max_expire_days", nullable = false)
    private int maxExpireDays;
}
