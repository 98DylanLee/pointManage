# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Purpose

무료 포인트 시스템 API. 적립 / 적립취소 / 사용 / 사용취소 기능을 제공한다.

제출 요건:
- 소스코드 GitHub 업로드 (필수)
- ERD를 `resource/` 하위에 PDF 또는 이미지로 포함 (필수)
- AWS 아키텍처 구성도를 `resource/` 하위에 PDF 또는 이미지로 포함 (옵션)
- 빌드 방법 및 과제 설명을 `README.md`에 작성 (필수)
- 과제를 판단하는 관리자가 테스트 하기 용이해야 함. mock @Test 등 적극적 사용

## Business Rules

### 포인트 정책 (`point_policy` 테이블로 관리 — 하드코딩 금지)

| 항목 | 제약 |
|---|---|
| 1회 최대 적립 | 1 이상 100,000 이하 |
| 개인 최대 보유 | 정책 테이블로 제어 |
| 만료일 범위 | 최소 1일 이상, 최대 5년 미만(1825일 미만) |
| 기본 만료일 | 365일 |

### 적립 (EARN / ADMIN_GIFT)

- 관리자 수기 지급(`ADMIN_GIFT`)은 일반 적립(`EARN`)과 구분 식별.
- 모든 적립건에는 만료일 필수.

### 적립 취소 (EARN_CANCEL)

- 해당 적립건의 전액이 미사용 상태일 때만 취소 가능 (`remained_amount_won == amount_won`).

### 사용 (USE)

- 주문 시에만 사용 가능 (`order_id` 필수).
- 사용 우선순위: `ADMIN_GIFT` 먼저 → 만료일이 짧은 순.
- `point_usage_detail`에 어느 적립건에서 얼마를 썼는지 1원 단위로 기록.

### 사용 취소 (USE_CANCEL)

- 전액 또는 일부 취소 가능.
- 취소 시 원천 적립건이 **아직 유효**하면 `remained_amount_won` 복구 (`USE_CANCEL_RESTORE`).
- 취소 시 원천 적립건이 **이미 만료**되었으면 해당 금액만큼 신규 `CANCEL_RETURN` 적립 (`USE_CANCEL_REGRANT`).

## ERD (테이블 요약)

### `point_policy`
포인트 정책 테이블. 1회 최대 적립, 최대 보유, 만료일 범위 등 하드코딩 금지 항목을 관리.

### `point_wallet`
사용자별 현재 보유 포인트 잔액. 낙관적 잠금(`version`) 적용.

### `point_ledger`
포인트 원장. 모든 포인트 이벤트를 단일 테이블로 관리.

`tx_type` 값:

| 값 | 설명 |
|---|---|
| `EARN` | 일반 적립 |
| `ADMIN_GIFT` | 관리자 수기 지급 |
| `EARN_CANCEL` | 적립 취소 |
| `USE` | 주문 사용 |
| `USE_CANCEL` | 사용 취소 |
| `CANCEL_RETURN` | 사용취소 시 만료된 적립 재발급 |
| `EXPIRE` | 만료 처리 |

- `EARN` / `ADMIN_GIFT` / `CANCEL_RETURN` : 적립 lot 역할. `remained_amount_won`(잔여 사용 가능액)과 `expired_at` 보유.
- 나머지 tx_type : `remained_amount_won = 0`, `expired_at` 불필요.
- `point_key` : 멱등키 (업무 키). `related_point_id` : 원인이 된 원장 행 참조.

### `point_usage_detail`
사용/사용취소 시 원천 적립건별 금액 추적.

`detail_type` 값:

| 값 | 설명 |
|---|---|
| `USE` | 사용 시 적립건별 차감 상세 |
| `USE_CANCEL_RESTORE` | 사용취소 시 미만료 적립건 복구 |
| `USE_CANCEL_REGRANT` | 사용취소 시 만료된 적립건 신규 재발급 |

### `orders`
주문. `order_no`(외부 비즈니스 키), `point_usage_amount_won`(사용 포인트) 포함.

### `product`
상품. 낙관적 잠금(`version`) 적용.

## Data Initialization

H2 인메모리 DB를 사용하므로 서비스 기동 시 초기 데이터 삽입이 필요하다. `src/main/resources/data.sql` 등을 통해 아래 정책 데이터를 자동 삽입해야 한다.

```sql
INSERT INTO point_policy (
    policy_code,
    max_earn_per_tx_won,
    max_balance_won,
    default_expire_days,
    min_expire_days,
    max_expire_days
) VALUES (
    'POINT',
    100000,
    5000000,
    365,
    1,
    1824
);
```

## Commands

```bash
# Build
./gradlew build

# Run application
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.point.api.point.controller.PointCommandControllerTest"

# Run a single test method
./gradlew test --tests "com.point.api.point.controller.PointCommandControllerTest.적립요청을_생성한다"
```

H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:pointdb`).

## Architecture

Java 21 + Spring Boot 3.3.6 + H2 (in-memory, PostgreSQL compatibility mode). No persistence across restarts (`ddl-auto: create-drop`).

### Package structure

All application code lives under `com.point.api.point`, split by layer:

- `controller/` — REST controllers (`@WebMvcTest` slice in tests)
- `service/` — Service interfaces + `*Impl` implementations (`@Transactional` on impl class)
- `repository/` — Spring Data JPA repositories
- `entity/` — JPA entities and enums (entities use `@Builder` with protected no-arg constructor)
- `dto/` — Java records for request/response

### Naming conventions

- CQRS-style split: Command operations use `PointCommand*` naming. Query operations should use `PointQuery*`.
- Service layer: interface (`PointCommandService`) + `@Service` impl (`PointCommandServiceImpl`).
- DTOs are Java records with Bean Validation annotations on request records.

### Current API

`POST /api/v1/points/accruals` — creates a point accrual transaction.

`PointTransactionType` enum defines all supported transaction kinds: `ACCRUAL`, `ACCRUAL_CANCEL`, `REDEEM`, `REDEEM_CANCEL`.

### Testing conventions

- Controller tests use `@WebMvcTest` + `@MockBean` for the service.
- Test method names are written in Korean.
- No separate test fixtures classes — inline `private record RequestFixture` used inside test classes.
