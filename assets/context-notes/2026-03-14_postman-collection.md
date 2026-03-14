---
title: Postman import assets
date: 2026-03-14
project: point-api
decision_type: implementation
status: active
tags:
  - postman
  - tooling
  - assignment
---

## What

Postman에서 바로 임포트할 수 있는 collection과 local environment 파일을 추가했다.
구현된 command/query API 전체와 과제 예시 흐름 A-B-C-D를 한 번에 실행할 수 있는 요청 세트를 포함한다.

## Why

과제 검토자가 프로젝트를 실행한 뒤 API를 손쉽게 재현할 수 있어야 한다.
Swagger만으로도 호출은 가능하지만, 순차 시나리오와 변수화된 예시 body는 Postman collection 쪽이 더 빠르다.

## Where

- `postman/point-api.postman_collection.json`
- `postman/point-api.local.postman_environment.json`
