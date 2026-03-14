---
title: Assignment baseline aligned with albus-web
date: 2026-03-14
project: point-api
decision_type: implementation
status: active
tags:
  - assignment
  - structure
  - spring-boot
  - aws
---

## What

`point-api`를 과제용 최소 범위로 유지하면서 `albus-web`의 구성 방식을 부분 반영했다.
구체적으로는 공통 응답/예외를 `core` 패키지로 이동하고, `config` 패키지와 OpenAPI 설정을 추가했으며, 공통/로컬 설정 파일을 분리했다.
또한 AWS 기반 배포를 가정한 아키텍처 이미지를 `src/main/resources/architecture/aws-backend-architecture.svg`로 포함했다.

## Why

과제 제출물은 기능 구현뿐 아니라 프로젝트가 어떻게 확장될지 보여주는 기본 구조가 중요하다.
`albus-web` 전체를 그대로 복제하면 과제 범위를 벗어나므로, 도메인 중심 패키지 구조와 공통 설정 계층만 추려서 반영했다.
AWS 아키텍처 이미지는 과제 옵션 요구사항을 충족하면서 이후 적립 외 기능 확장 방향도 설명해 준다.

## Where

- `src/main/java/com/point/api/core/api`
- `src/main/java/com/point/api/core/exception`
- `src/main/java/com/point/api/config`
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`
- `src/main/resources/architecture/aws-backend-architecture.svg`
- `README.md`
