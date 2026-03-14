---
title: Point history query API
date: 2026-03-14
project: point-api
decision_type: implementation
status: active
tags:
  - point
  - query
  - history
  - assignment
---

## What

`userId`, `startDate`, `endDate`를 받아 기간 내 포인트 원장 히스토리를 조회하는 API를 추가했다.
응답은 `point_ledger` 기준으로 생성 시각 범위 내 이력을 정렬해서 반환하며, 주문번호와 관련 원본 포인트 키도 함께 내려준다.

## Why

요구사항에는 직접 명시되지 않았지만, 과제 검토자가 실제 포인트 흐름을 추적하기 쉽게 하려면 조회 API가 필요하다.
원장 자체가 포인트 이벤트의 기준 데이터이므로 별도 집계 테이블 없이 `point_ledger` 조회로 구현하는 편이 단순하고 정확하다.

## Where

- `src/main/java/com/point/api/point/controller/PointQueryController.java`
- `src/main/java/com/point/api/point/service/PointQueryServiceImpl.java`
- `src/main/java/com/point/api/point/repository/PointLedgerRepository.java`
- `src/test/java/com/point/api/point/controller/PointQueryControllerTest.java`
