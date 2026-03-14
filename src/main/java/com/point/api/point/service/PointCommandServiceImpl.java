package com.point.api.point.service;

import com.point.api.point.dto.PointEarnCancelRequest;
import com.point.api.point.dto.PointEarnCancelResponse;
import com.point.api.point.dto.PointEarnRequest;
import com.point.api.point.dto.PointEarnResponse;
import com.point.api.point.dto.PointUsageDetailResponse;
import com.point.api.point.dto.PointUseCancelRequest;
import com.point.api.point.dto.PointUseCancelResponse;
import com.point.api.point.dto.PointUseRequest;
import com.point.api.point.dto.PointUseResponse;
import com.point.api.point.entity.Order;
import com.point.api.point.entity.PointLedger;
import com.point.api.point.entity.PointPolicy;
import com.point.api.point.entity.PointTxType;
import com.point.api.point.entity.PointUsageDetail;
import com.point.api.point.entity.PointUsageDetailType;
import com.point.api.point.entity.PointWallet;
import com.point.api.point.repository.OrderRepository;
import com.point.api.point.repository.PointLedgerRepository;
import com.point.api.point.repository.PointPolicyRepository;
import com.point.api.point.repository.PointUsageDetailRepository;
import com.point.api.point.repository.PointWalletRepository;
import java.time.Clock;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointCommandServiceImpl implements PointCommandService {

    private static final String POLICY_CODE = "POINT";
    private static final List<PointTxType> USABLE_LOT_TYPES = List.of(
            PointTxType.ADMIN_GIFT,
            PointTxType.EARN,
            PointTxType.CANCEL_RETURN
    );

    private final PointPolicyRepository pointPolicyRepository;
    private final PointWalletRepository pointWalletRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;
    private final OrderRepository orderRepository;
    private final Clock clock;

    @Override
    public PointEarnResponse earn(PointEarnRequest request) {
        PointPolicy policy = getPolicy();
        String requestHash = computeHash(
                request.userId(), request.amount(), request.pointKey(), request.adminGift(), request.expireDays(), request.description()
        );
        Optional<PointLedger> existing = findExistingOrValidateHash(request.pointKey(), requestHash);
        if (existing.isPresent()) {
            return toEarnResponse(existing.get());
        }
        PointWallet wallet = refreshWallet(request.userId());

        if (request.amount() > policy.getMaxEarnPerTxWon()) {
            throw new IllegalArgumentException(
                    "1회 최대 적립 한도를 초과했습니다. 한도: " + policy.getMaxEarnPerTxWon());
        }

        int expireDays = request.expireDays() != null
                ? request.expireDays()
                : policy.getDefaultExpireDays();
        if (expireDays < policy.getMinExpireDays() || expireDays > policy.getMaxExpireDays()) {
            throw new IllegalArgumentException(String.format(
                    "만료일은 %d일 이상 %d일 이하여야 합니다.",
                    policy.getMinExpireDays(), policy.getMaxExpireDays()));
        }

        if (wallet.getBalanceWon() + request.amount() > policy.getMaxBalanceWon()) {
            throw new IllegalArgumentException(
                    "최대 보유 한도를 초과합니다. 한도: " + policy.getMaxBalanceWon());
        }

        PointTxType txType = request.adminGift() ? PointTxType.ADMIN_GIFT : PointTxType.EARN;
        LocalDateTime expiredAt = now().plusDays(expireDays);

        PointLedger ledger = PointLedger.builder()
                .pointKey(request.pointKey())
                .requestHash(requestHash)
                .userId(request.userId())
                .txType(txType)
                .amountWon(request.amount())
                .remainedAmountWon(request.amount())
                .expiredAt(expiredAt)
                .build();

        pointLedgerRepository.save(ledger);
        wallet.add(request.amount());
        pointWalletRepository.save(wallet);

        return toEarnResponse(ledger);
    }

    @Override
    public PointEarnCancelResponse cancelEarn(PointEarnCancelRequest request) {
        String requestHash = computeHash(
                request.userId(), request.pointKey(), request.originalPointKey(), request.description()
        );
        Optional<PointLedger> existing = findExistingOrValidateHash(request.pointKey(), requestHash);
        if (existing.isPresent()) {
            return toEarnCancelResponse(existing.get());
        }
        PointWallet wallet = refreshWallet(request.userId());

        PointLedger original = getRequiredLedger(request.originalPointKey());
        if (!isEarnLot(original)) {
            throw new IllegalArgumentException("적립 취소 대상은 적립성 포인트여야 합니다.");
        }
        if (!original.getUserId().equals(request.userId())) {
            throw new IllegalArgumentException("원본 적립의 사용자와 요청 사용자가 다릅니다.");
        }
        if (original.getRemainedAmountWon() != original.getAmountWon()) {
            throw new IllegalArgumentException("일부라도 사용된 적립은 취소할 수 없습니다.");
        }
        if (!pointLedgerRepository.findByRelatedPoint_PointIdAndTxType(original.getPointId(), PointTxType.EARN_CANCEL).isEmpty()) {
            throw new IllegalArgumentException("이미 취소된 적립입니다.");
        }

        wallet.subtract(original.getAmountWon());
        original.deductRemained(original.getAmountWon());

        PointLedger cancel = PointLedger.builder()
                .pointKey(request.pointKey())
                .requestHash(requestHash)
                .userId(request.userId())
                .txType(PointTxType.EARN_CANCEL)
                .amountWon(original.getAmountWon())
                .remainedAmountWon(0L)
                .relatedPoint(original)
                .build();
        pointLedgerRepository.save(cancel);
        pointWalletRepository.save(wallet);

        return toEarnCancelResponse(cancel);
    }

    @Override
    public PointUseResponse use(PointUseRequest request) {
        String requestHash = computeHash(
                request.userId(), request.amount(), request.pointKey(), request.orderNo(),
                request.orderRequestKey(), request.totalAmount(), request.description()
        );
        Optional<PointLedger> existing = findExistingOrValidateHash(request.pointKey(), requestHash);
        if (existing.isPresent()) {
            List<PointUsageDetail> details = pointUsageDetailRepository.findByPoint_PointIdOrderByUsageDetailIdAsc(existing.get().getPointId());
            PointWallet wallet = getOrCreateWallet(request.userId());
            return toUseResponse(existing.get(), wallet.getBalanceWon(), details);
        }
        PointWallet wallet = refreshWallet(request.userId());

        if (request.totalAmount() < request.amount()) {
            throw new IllegalArgumentException("주문 금액보다 많은 포인트를 사용할 수 없습니다.");
        }

        if (wallet.getBalanceWon() < request.amount()) {
            throw new IllegalArgumentException("보유 포인트가 부족합니다.");
        }

        if (orderRepository.findByOrderNo(request.orderNo()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 주문번호입니다.");
        }

        List<PointLedger> usableLots = pointLedgerRepository.findUsableLots(request.userId(), USABLE_LOT_TYPES, now());
        long remaining = request.amount();
        List<PointUsageDetail> usageDetails = new ArrayList<>();

        Order order = Order.builder()
                .orderNo(request.orderNo())
                .userId(request.userId())
                .requestKey(request.orderRequestKey())
                .totalAmountWon(request.totalAmount())
                .pointUsageAmountWon(request.amount())
                .paymentAmountWon(request.totalAmount() - request.amount())
                .build();
        orderRepository.save(order);

        PointLedger useLedger = PointLedger.builder()
                .pointKey(request.pointKey())
                .requestHash(requestHash)
                .userId(request.userId())
                .txType(PointTxType.USE)
                .amountWon(request.amount())
                .remainedAmountWon(0L)
                .order(order)
                .build();
        pointLedgerRepository.save(useLedger);

        for (PointLedger lot : usableLots) {
            if (remaining == 0) {
                break;
            }
            long deductAmount = Math.min(lot.getRemainedAmountWon(), remaining);
            lot.deductRemained(deductAmount);
            remaining -= deductAmount;

            usageDetails.add(pointUsageDetailRepository.save(PointUsageDetail.builder()
                    .point(useLedger)
                    .detailType(PointUsageDetailType.USE)
                    .order(order)
                    .sourcePoint(lot)
                    .amountWon(deductAmount)
                    .build()));
        }

        if (remaining > 0) {
            throw new IllegalArgumentException("사용 가능한 포인트 lot 이 부족합니다.");
        }

        wallet.subtract(request.amount());
        pointWalletRepository.save(wallet);

        return toUseResponse(useLedger, wallet.getBalanceWon(), usageDetails);
    }

    @Override
    public PointUseCancelResponse cancelUse(PointUseCancelRequest request) {
        PointPolicy policy = getPolicy();
        String requestHash = computeHash(
                request.userId(), request.amount(), request.pointKey(), request.originalUsePointKey(), request.description()
        );
        Optional<PointLedger> existing = findExistingOrValidateHash(request.pointKey(), requestHash);
        if (existing.isPresent()) {
            PointLedger originalUse = getRequiredLedger(request.originalUsePointKey());
            long remainingCancelable = originalUse.getAmountWon()
                    - pointLedgerRepository.sumAmountByRelatedPointIdAndType(originalUse.getPointId(), PointTxType.USE_CANCEL);
            List<PointUsageDetail> details = pointUsageDetailRepository.findByPoint_PointIdOrderByUsageDetailIdAsc(existing.get().getPointId());
            PointWallet wallet = getOrCreateWallet(request.userId());
            return toUseCancelResponse(existing.get(), wallet.getBalanceWon(), remainingCancelable, details);
        }
        PointWallet wallet = refreshWallet(request.userId());

        PointLedger originalUse = getRequiredLedger(request.originalUsePointKey());
        if (originalUse.getTxType() != PointTxType.USE) {
            throw new IllegalArgumentException("사용 취소 대상은 USE 포인트여야 합니다.");
        }
        if (!originalUse.getUserId().equals(request.userId())) {
            throw new IllegalArgumentException("원본 사용의 사용자와 요청 사용자가 다릅니다.");
        }

        long canceledAmount = pointLedgerRepository.sumAmountByRelatedPointIdAndType(originalUse.getPointId(), PointTxType.USE_CANCEL);
        long remainingCancelable = originalUse.getAmountWon() - canceledAmount;
        if (request.amount() > remainingCancelable) {
            throw new IllegalArgumentException("취소 가능 금액을 초과했습니다.");
        }

        PointLedger cancelLedger = PointLedger.builder()
                .pointKey(request.pointKey())
                .requestHash(requestHash)
                .userId(request.userId())
                .txType(PointTxType.USE_CANCEL)
                .amountWon(request.amount())
                .remainedAmountWon(0L)
                .order(originalUse.getOrder())
                .relatedPoint(originalUse)
                .build();
        pointLedgerRepository.save(cancelLedger);

        List<PointUsageDetail> originalUseDetails =
                pointUsageDetailRepository.findByPoint_PointIdOrderByUsageDetailIdAsc(originalUse.getPointId());
        List<PointUsageDetail> cancelDetails = new ArrayList<>();
        long restoreRemaining = request.amount();

        for (PointUsageDetail useDetail : originalUseDetails) {
            if (restoreRemaining == 0) {
                break;
            }
            long alreadyCanceledForSource = sumCanceledAmountForSource(originalUse, useDetail.getSourcePoint().getPointId());
            long sourceCancelable = useDetail.getAmountWon() - alreadyCanceledForSource;
            if (sourceCancelable <= 0) {
                continue;
            }

            long restoreAmount = Math.min(sourceCancelable, restoreRemaining);
            PointLedger sourcePoint = useDetail.getSourcePoint();
            if (sourcePoint.isExpiredAt(now())) {
                PointLedger regrant = PointLedger.builder()
                        .pointKey(buildCancelReturnPointKey(request.pointKey(), sourcePoint.getPointId()))
                        .requestHash(computeHash(request.pointKey(), sourcePoint.getPointId(), restoreAmount))
                        .userId(request.userId())
                        .txType(PointTxType.CANCEL_RETURN)
                        .amountWon(restoreAmount)
                        .remainedAmountWon(restoreAmount)
                        .expiredAt(now().plusDays(policy.getDefaultExpireDays()))
                        .relatedPoint(cancelLedger)
                        .build();
                pointLedgerRepository.save(regrant);
                cancelDetails.add(pointUsageDetailRepository.save(PointUsageDetail.builder()
                        .point(cancelLedger)
                        .detailType(PointUsageDetailType.USE_CANCEL_REGRANT)
                        .order(originalUse.getOrder())
                        .sourcePoint(sourcePoint)
                        .targetPoint(regrant)
                        .amountWon(restoreAmount)
                        .build()));
            } else {
                sourcePoint.restoreRemained(restoreAmount);
                cancelDetails.add(pointUsageDetailRepository.save(PointUsageDetail.builder()
                        .point(cancelLedger)
                        .detailType(PointUsageDetailType.USE_CANCEL_RESTORE)
                        .order(originalUse.getOrder())
                        .sourcePoint(sourcePoint)
                        .amountWon(restoreAmount)
                        .build()));
            }
            restoreRemaining -= restoreAmount;
        }

        if (restoreRemaining > 0) {
            throw new IllegalArgumentException("사용 취소 복구 처리 중 잔여 금액이 남았습니다.");
        }

        wallet.add(request.amount());
        pointWalletRepository.save(wallet);

        long updatedRemainingCancelable = originalUse.getAmountWon()
                - pointLedgerRepository.sumAmountByRelatedPointIdAndType(originalUse.getPointId(), PointTxType.USE_CANCEL);

        return toUseCancelResponse(cancelLedger, wallet.getBalanceWon(), updatedRemainingCancelable, cancelDetails);
    }

    /**
     * 요청 필드를 조합한 SHA-256 해시.
     * 동일 pointKey + 다른 페이로드 감지에 사용한다.
     */
    private String computeHash(Object... values) {
        StringBuilder raw = new StringBuilder();
        for (Object value : values) {
            if (raw.length() > 0) {
                raw.append('|');
            }
            raw.append(value);
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    private PointPolicy getPolicy() {
        return pointPolicyRepository.findById(POLICY_CODE)
                .orElseThrow(() -> new IllegalStateException("포인트 정책이 존재하지 않습니다."));
    }

    private PointWallet getOrCreateWallet(Long userId) {
        return pointWalletRepository.findById(userId)
                .orElseGet(() -> PointWallet.create(userId));
    }

    private PointWallet refreshWallet(Long userId) {
        PointWallet wallet = getOrCreateWallet(userId);
        LocalDateTime current = now();

        List<PointLedger> expiredLots = pointLedgerRepository.findExpiredLots(userId, USABLE_LOT_TYPES, current);
        for (PointLedger expiredLot : expiredLots) {
            long expiredAmount = expiredLot.getRemainedAmountWon();
            if (expiredAmount <= 0) {
                continue;
            }

            PointLedger expireLedger = PointLedger.builder()
                    .pointKey(buildExpirePointKey(expiredLot.getPointId()))
                    .requestHash(computeHash("expire", expiredLot.getPointId(), expiredAmount, expiredLot.getExpiredAt()))
                    .userId(userId)
                    .txType(PointTxType.EXPIRE)
                    .amountWon(expiredAmount)
                    .remainedAmountWon(0L)
                    .relatedPoint(expiredLot)
                    .build();
            pointLedgerRepository.save(expireLedger);
            expiredLot.deductRemained(expiredAmount);
        }

        long recalculatedBalance = pointLedgerRepository.sumUsableBalance(userId, USABLE_LOT_TYPES, current);
        wallet.replaceBalance(recalculatedBalance);
        return pointWalletRepository.save(wallet);
    }

    private Optional<PointLedger> findExistingOrValidateHash(String pointKey, String requestHash) {
        Optional<PointLedger> existing = pointLedgerRepository.findByPointKey(pointKey);
        if (existing.isPresent() && !existing.get().getRequestHash().equals(requestHash)) {
            throw new IllegalArgumentException("동일한 pointKey에 다른 요청 내용이 존재합니다: " + pointKey);
        }
        return existing;
    }

    private PointLedger getRequiredLedger(String pointKey) {
        return pointLedgerRepository.findByPointKey(pointKey)
                .orElseThrow(() -> new IllegalArgumentException("해당 pointKey 가 존재하지 않습니다: " + pointKey));
    }

    private boolean isEarnLot(PointLedger ledger) {
        return ledger.getTxType() == PointTxType.EARN
                || ledger.getTxType() == PointTxType.ADMIN_GIFT
                || ledger.getTxType() == PointTxType.CANCEL_RETURN;
    }

    private long sumCanceledAmountForSource(PointLedger originalUse, Long sourcePointId) {
        long total = 0L;
        List<PointLedger> cancelLedgers =
                pointLedgerRepository.findByRelatedPoint_PointIdAndTxType(originalUse.getPointId(), PointTxType.USE_CANCEL);
        for (PointLedger cancelLedger : cancelLedgers) {
            List<PointUsageDetail> details =
                    pointUsageDetailRepository.findByPoint_PointIdOrderByUsageDetailIdAsc(cancelLedger.getPointId());
            for (PointUsageDetail detail : details) {
                if (detail.getSourcePoint().getPointId().equals(sourcePointId)) {
                    total += detail.getAmountWon();
                }
            }
        }
        return total;
    }

    private String buildCancelReturnPointKey(String useCancelPointKey, Long sourcePointId) {
        return useCancelPointKey + "-return-" + sourcePointId;
    }

    private String buildExpirePointKey(Long sourcePointId) {
        return "expire-" + sourcePointId;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private PointEarnResponse toEarnResponse(PointLedger ledger) {
        return new PointEarnResponse(
                ledger.getPointId(),
                ledger.getPointKey(),
                ledger.getUserId(),
                ledger.getTxType(),
                ledger.getAmountWon(),
                ledger.getRemainedAmountWon(),
                ledger.getExpiredAt(),
                ledger.getCreatedAt()
        );
    }

    private PointEarnCancelResponse toEarnCancelResponse(PointLedger ledger) {
        return new PointEarnCancelResponse(
                ledger.getPointId(),
                ledger.getPointKey(),
                ledger.getRelatedPoint().getPointKey(),
                ledger.getUserId(),
                ledger.getTxType(),
                ledger.getAmountWon(),
                ledger.getCreatedAt()
        );
    }

    private PointUseResponse toUseResponse(PointLedger ledger, long balanceAfter, List<PointUsageDetail> details) {
        return new PointUseResponse(
                ledger.getPointId(),
                ledger.getPointKey(),
                ledger.getUserId(),
                ledger.getOrder().getOrderNo(),
                ledger.getTxType(),
                ledger.getAmountWon(),
                balanceAfter,
                ledger.getCreatedAt(),
                mapUsageDetails(details)
        );
    }

    private PointUseCancelResponse toUseCancelResponse(
            PointLedger ledger,
            long balanceAfter,
            long remainingCancelableAmount,
            List<PointUsageDetail> details
    ) {
        return new PointUseCancelResponse(
                ledger.getPointId(),
                ledger.getPointKey(),
                ledger.getRelatedPoint().getPointKey(),
                ledger.getUserId(),
                ledger.getTxType(),
                ledger.getAmountWon(),
                balanceAfter,
                remainingCancelableAmount,
                ledger.getCreatedAt(),
                mapUsageDetails(details)
        );
    }

    private List<PointUsageDetailResponse> mapUsageDetails(List<PointUsageDetail> details) {
        return details.stream()
                .map(detail -> new PointUsageDetailResponse(
                        detail.getDetailType(),
                        detail.getSourcePoint().getPointKey(),
                        detail.getTargetPoint() != null ? detail.getTargetPoint().getPointKey() : null,
                        detail.getAmountWon()
                ))
                .toList();
    }
}
