package com.point.api.point.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.point.api.point.dto.PointEarnCancelRequest;
import com.point.api.point.dto.PointEarnRequest;
import com.point.api.point.dto.PointUseCancelRequest;
import com.point.api.point.dto.PointUseResponse;
import com.point.api.point.dto.PointUseRequest;
import com.point.api.point.entity.PointLedger;
import com.point.api.point.entity.PointTxType;
import com.point.api.point.entity.PointUsageDetailType;
import com.point.api.point.repository.PointLedgerRepository;
import com.point.api.point.repository.PointWalletRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PointCommandServiceIntegrationTest {

    @Autowired
    private PointCommandService pointCommandService;

    @Autowired
    private PointLedgerRepository pointLedgerRepository;

    @Autowired
    private PointWalletRepository pointWalletRepository;

    @Test
    void 포인트_사용시_관리자지급_우선_그리고_만료임박순으로_차감한다() {
        pointCommandService.earn(new PointEarnRequest(1L, 200L, "EARN-1", false, 5, null));
        pointCommandService.earn(new PointEarnRequest(1L, 300L, "ADMIN-1", true, 30, null));
        pointCommandService.earn(new PointEarnRequest(1L, 400L, "EARN-2", false, 20, null));

        PointUseResponse response = pointCommandService.use(
                new PointUseRequest(1L, 450L, "USE-1", "ORDER-1", "ORDER-REQ-1", 1000L, null)
        );

        assertThat(response.balanceAfter()).isEqualTo(450L);
        assertThat(response.usageDetails()).hasSize(2);
        assertThat(response.usageDetails().get(0).detailType()).isEqualTo(PointUsageDetailType.USE);
        assertThat(response.usageDetails().get(0).sourcePointKey()).isEqualTo("ADMIN-1");
        assertThat(response.usageDetails().get(0).amount()).isEqualTo(300L);
        assertThat(response.usageDetails().get(1).sourcePointKey()).isEqualTo("EARN-1");
        assertThat(response.usageDetails().get(1).amount()).isEqualTo(150L);

        assertThat(pointLedgerRepository.findByPointKey("ADMIN-1")).get().extracting(PointLedger::getRemainedAmountWon).isEqualTo(0L);
        assertThat(pointLedgerRepository.findByPointKey("EARN-1")).get().extracting(PointLedger::getRemainedAmountWon).isEqualTo(50L);
        assertThat(pointLedgerRepository.findByPointKey("EARN-2")).get().extracting(PointLedger::getRemainedAmountWon).isEqualTo(400L);
    }

    @Test
    void 일부라도_사용된_적립은_취소할수_없다() {
        pointCommandService.earn(new PointEarnRequest(2L, 1000L, "EARN-A", false, 30, null));
        pointCommandService.use(new PointUseRequest(2L, 300L, "USE-A", "ORDER-A", "ORDER-REQ-A", 2000L, null));

        assertThatThrownBy(() -> pointCommandService.cancelEarn(
                new PointEarnCancelRequest(2L, "CANCEL-A", "EARN-A", null)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("일부라도 사용된 적립은 취소할 수 없습니다.");
    }

    @Test
    void 사용취소시_원본_lot가_만료되었으면_재적립한다() {
        pointCommandService.earn(new PointEarnRequest(3L, 1000L, "A", false, 30, null));
        pointCommandService.earn(new PointEarnRequest(3L, 500L, "B", false, 30, null));
        pointCommandService.use(new PointUseRequest(3L, 1200L, "C", "ORDER-C", "ORDER-REQ-C", 3000L, null));

        PointLedger earnA = pointLedgerRepository.findByPointKey("A").orElseThrow();
        earnA.expireAt(LocalDateTime.now().minusDays(1));

        var response = pointCommandService.cancelUse(
                new PointUseCancelRequest(3L, 1100L, "D", "C", null)
        );

        assertThat(response.balanceAfter()).isEqualTo(1400L);
        assertThat(response.remainingCancelableAmount()).isEqualTo(100L);
        assertThat(response.usageDetails()).hasSize(2);
        assertThat(response.usageDetails().get(0).detailType()).isEqualTo(PointUsageDetailType.USE_CANCEL_REGRANT);
        assertThat(response.usageDetails().get(0).sourcePointKey()).isEqualTo("A");
        assertThat(response.usageDetails().get(0).targetPointKey()).startsWith("D-return-");
        assertThat(response.usageDetails().get(0).amount()).isEqualTo(1000L);
        assertThat(response.usageDetails().get(1).detailType()).isEqualTo(PointUsageDetailType.USE_CANCEL_RESTORE);
        assertThat(response.usageDetails().get(1).sourcePointKey()).isEqualTo("B");
        assertThat(response.usageDetails().get(1).amount()).isEqualTo(100L);

        PointLedger regrant = pointLedgerRepository.findByPointKey(response.usageDetails().get(0).targetPointKey()).orElseThrow();
        assertThat(regrant.getTxType()).isEqualTo(PointTxType.CANCEL_RETURN);
        assertThat(regrant.getAmountWon()).isEqualTo(1000L);
        assertThat(regrant.getRemainedAmountWon()).isEqualTo(1000L);

        assertThat(pointLedgerRepository.findByPointKey("B")).get().extracting(PointLedger::getRemainedAmountWon).isEqualTo(400L);
        assertThat(pointWalletRepository.findById(3L)).get().extracting(wallet -> wallet.getBalanceWon()).isEqualTo(1400L);
    }

    @Test
    void API_진입시_만료포인트를_EXPIRE로_정리하고_지갑을_재계산한다() {
        pointCommandService.earn(new PointEarnRequest(4L, 1000L, "EXPIRE-ME", false, 30, null));

        PointLedger earned = pointLedgerRepository.findByPointKey("EXPIRE-ME").orElseThrow();
        earned.expireAt(LocalDateTime.now().minusMinutes(1));

        assertThatThrownBy(() -> pointCommandService.use(
                new PointUseRequest(4L, 100L, "USE-EXPIRED", "ORDER-EXPIRED", "ORDER-REQ-EXPIRED", 1000L, null)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보유 포인트가 부족합니다.");

        PointLedger expireLedger = pointLedgerRepository.findByPointKey("expire-" + earned.getPointId()).orElseThrow();
        assertThat(expireLedger.getTxType()).isEqualTo(PointTxType.EXPIRE);
        assertThat(expireLedger.getAmountWon()).isEqualTo(1000L);
        assertThat(pointLedgerRepository.findByPointKey("EXPIRE-ME")).get().extracting(PointLedger::getRemainedAmountWon).isEqualTo(0L);
        assertThat(pointWalletRepository.findById(4L)).get().extracting(wallet -> wallet.getBalanceWon()).isEqualTo(0L);
    }
}
