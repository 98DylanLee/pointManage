package com.point.api.point.repository;

import com.point.api.point.entity.PointLedger;
import com.point.api.point.entity.PointTxType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointLedgerRepository extends JpaRepository<PointLedger, Long> {

    Optional<PointLedger> findByPointKey(String pointKey);

    @Query("""
            select p
            from PointLedger p
            where p.userId = :userId
              and p.txType in :lotTypes
              and p.remainedAmountWon > 0
              and p.expiredAt > :at
            order by case when p.txType = com.point.api.point.entity.PointTxType.ADMIN_GIFT then 0 else 1 end,
                     p.expiredAt asc,
                     p.pointId asc
            """)
    List<PointLedger> findUsableLots(@Param("userId") Long userId,
                                     @Param("lotTypes") List<PointTxType> lotTypes,
                                     @Param("at") LocalDateTime at);

    List<PointLedger> findByRelatedPoint_PointIdAndTxType(Long relatedPointId, PointTxType txType);

    @Query("""
            select coalesce(sum(p.amountWon), 0)
            from PointLedger p
            where p.relatedPoint.pointId = :relatedPointId
              and p.txType = :txType
            """)
    long sumAmountByRelatedPointIdAndType(@Param("relatedPointId") Long relatedPointId,
                                          @Param("txType") PointTxType txType);

    @Query("""
            select p
            from PointLedger p
            where p.userId = :userId
              and p.txType in :lotTypes
              and p.remainedAmountWon > 0
              and p.expiredAt <= :at
            order by p.expiredAt asc, p.pointId asc
            """)
    List<PointLedger> findExpiredLots(@Param("userId") Long userId,
                                      @Param("lotTypes") List<PointTxType> lotTypes,
                                      @Param("at") LocalDateTime at);

    @Query("""
            select coalesce(sum(p.remainedAmountWon), 0)
            from PointLedger p
            where p.userId = :userId
              and p.txType in :lotTypes
              and p.remainedAmountWon > 0
              and p.expiredAt > :at
            """)
    long sumUsableBalance(@Param("userId") Long userId,
                          @Param("lotTypes") List<PointTxType> lotTypes,
                          @Param("at") LocalDateTime at);

    @Query("""
            select p
            from PointLedger p
            left join fetch p.order
            left join fetch p.relatedPoint
            where p.userId = :userId
              and p.createdAt >= :startDateTime
              and p.createdAt < :endExclusive
            order by p.createdAt asc, p.pointId asc
            """)
    List<PointLedger> findHistoryByUserIdAndCreatedAtBetween(@Param("userId") Long userId,
                                                             @Param("startDateTime") LocalDateTime startDateTime,
                                                             @Param("endExclusive") LocalDateTime endExclusive);
}
