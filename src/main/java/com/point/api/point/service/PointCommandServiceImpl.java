package com.point.api.point.service;

import com.point.api.point.dto.PointEarnRequest;
import com.point.api.point.dto.PointEarnResponse;
import com.point.api.point.entity.PointLedger;
import com.point.api.point.entity.PointPolicy;
import com.point.api.point.entity.PointTxType;
import com.point.api.point.entity.PointWallet;
import com.point.api.point.repository.PointLedgerRepository;
import com.point.api.point.repository.PointPolicyRepository;
import com.point.api.point.repository.PointWalletRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointCommandServiceImpl implements PointCommandService {

    private static final String POLICY_CODE = "POINT";

    private final PointPolicyRepository pointPolicyRepository;
    private final PointWalletRepository pointWalletRepository;
    private final PointLedgerRepository pointLedgerRepository;

    @Override
    public PointEarnResponse earn(PointEarnRequest request) {
        PointPolicy policy = pointPolicyRepository.findById(POLICY_CODE)
                .orElseThrow(() -> new IllegalStateException("포인트 정책이 존재하지 않습니다."));

        // 멱등성 처리: 동일 pointKey 재요청 시 기존 결과 반환
        String requestHash = computeHash(request);
        Optional<PointLedger> existing = pointLedgerRepository.findByPointKey(request.pointKey());
        if (existing.isPresent()) {
            if (!existing.get().getRequestHash().equals(requestHash)) {
                throw new IllegalArgumentException(
                        "동일한 pointKey에 다른 요청 내용이 존재합니다: " + request.pointKey());
            }
            return toResponse(existing.get());
        }

        // 1회 적립 한도 검증
        if (request.amount() > policy.getMaxEarnPerTxWon()) {
            throw new IllegalArgumentException(
                    "1회 최대 적립 한도를 초과했습니다. 한도: " + policy.getMaxEarnPerTxWon());
        }

        // 만료일 검증
        int expireDays = request.expireDays() != null
                ? request.expireDays()
                : policy.getDefaultExpireDays();
        if (expireDays < policy.getMinExpireDays() || expireDays > policy.getMaxExpireDays()) {
            throw new IllegalArgumentException(String.format(
                    "만료일은 %d일 이상 %d일 이하여야 합니다.",
                    policy.getMinExpireDays(), policy.getMaxExpireDays()));
        }

        // 지갑 조회 또는 신규 생성 + 최대 보유 한도 검증
        PointWallet wallet = pointWalletRepository.findById(request.userId())
                .orElseGet(() -> PointWallet.create(request.userId()));
        if (wallet.getBalanceWon() + request.amount() > policy.getMaxBalanceWon()) {
            throw new IllegalArgumentException(
                    "최대 보유 한도를 초과합니다. 한도: " + policy.getMaxBalanceWon());
        }

        // 원장 저장
        PointTxType txType = request.adminGift() ? PointTxType.ADMIN_GIFT : PointTxType.EARN;
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(expireDays);

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

        // 지갑 잔액 갱신
        wallet.add(request.amount());
        pointWalletRepository.save(wallet);

        return toResponse(ledger);
    }

    /**
     * 요청 필드를 조합한 SHA-256 해시.
     * 동일 pointKey + 다른 페이로드 감지에 사용한다.
     */
    private String computeHash(PointEarnRequest request) {
        String raw = request.userId() + "|" + request.amount() + "|"
                + request.adminGift() + "|" + request.expireDays() + "|" + request.description();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    private PointEarnResponse toResponse(PointLedger ledger) {
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
}
