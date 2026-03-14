---
title: Point transaction flow strategy
date: 2026-03-14
project: point-api
decision_type: implementation
status: active
tags:
  - point
  - transactions
  - ledger
  - assignment
---

## What

남은 과제 기능인 적립취소, 사용, 사용취소를 `point_ledger` 와 `point_usage_detail` 조합으로 구현한다.
`point_ledger` 는 거래 이벤트와 사용 가능한 lot 를 함께 표현하고, `point_usage_detail` 은 실제 사용과 사용취소의 출처/복구 경로를 기록한다.

## Why

과제 요구사항의 핵심은 "어느 적립건이 어떤 주문에서 1원 단위로 사용되었는지" 와 "사용취소 시 원래 lot 복구 또는 재적립" 이다.
원장만으로는 부분 사용취소의 출처와 재적립 대상을 명확히 표현하기 어렵기 때문에 상세 테이블을 분리했다.
이 구조는 적립취소, 사용, 사용취소, 이후 만료 처리까지 같은 모델 위에서 이어갈 수 있다.

## Where

- `src/main/java/com/point/api/point/entity/PointLedger.java`
- `src/main/java/com/point/api/point/entity/PointUsageDetail.java`
- `src/main/java/com/point/api/point/service/PointCommandServiceImpl.java`
- `src/test/java/com/point/api/point/service/PointCommandServiceIntegrationTest.java`
