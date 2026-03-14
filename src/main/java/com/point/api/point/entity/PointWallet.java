package com.point.api.point.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point_wallet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointWallet {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "balance_won", nullable = false)
    private long balanceWon;

    @Version
    private Long version;

    public static PointWallet create(Long userId) {
        PointWallet wallet = new PointWallet();
        wallet.userId = userId;
        wallet.balanceWon = 0L;
        return wallet;
    }

    public void add(long amount) {
        this.balanceWon += amount;
    }
}
