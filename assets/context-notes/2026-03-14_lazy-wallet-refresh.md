---
title: Lazy wallet refresh before point commands
date: 2026-03-14
project: point-api
decision_type: implementation
status: active
tags:
  - wallet
  - expiration
  - point
  - assignment
---

## What

모든 포인트 command API 진입 시 `PointWallet` 과 만료된 적립 lot 를 먼저 정리한다.
구체적으로는 사용 가능한 적립 lot 중 만료 시각이 지난 row 를 찾아 `EXPIRE` 원장을 생성하고, 이후 사용 가능한 lot 합계로 `PointWallet.balanceWon` 을 재계산한다.

## Why

배치가 없는 과제 환경에서도 지갑 잔액과 실제 사용 가능한 lot 상태가 어긋나면 안 된다.
필터에서 요청 바디를 읽어 `userId` 를 추출하는 방식은 Spring MVC 에서 복잡성과 부작용이 크기 때문에, 이미 `userId` 를 알고 있는 서비스 레이어에서 지연 정산하는 방식이 더 단순하고 안전하다.

## Where

- `src/main/java/com/point/api/point/service/PointCommandServiceImpl.java`
- `src/main/java/com/point/api/point/repository/PointLedgerRepository.java`
- `src/test/java/com/point/api/point/service/PointCommandServiceIntegrationTest.java`
