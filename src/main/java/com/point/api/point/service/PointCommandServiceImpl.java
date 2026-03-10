package com.point.api.point.service;

import com.point.api.point.dto.PointAccrualRequest;
import com.point.api.point.dto.PointTransactionResponse;
import com.point.api.point.entity.PointTransaction;
import com.point.api.point.entity.PointTransactionType;
import com.point.api.point.repository.PointTransactionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointCommandServiceImpl implements PointCommandService {

    private final PointTransactionRepository pointTransactionRepository;

    @Override
    public PointTransactionResponse accrue(PointAccrualRequest request) {
        PointTransaction transaction = pointTransactionRepository.save(PointTransaction.builder()
                .memberId(request.memberId())
                .amount(request.amount())
                .transactionType(PointTransactionType.ACCRUAL)
                .orderId(request.orderId())
                .description(request.description())
                .occurredAt(LocalDateTime.now())
                .build());

        return new PointTransactionResponse(
                transaction.getId(),
                transaction.getMemberId(),
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getOrderId(),
                transaction.getOccurredAt()
        );
    }
}
