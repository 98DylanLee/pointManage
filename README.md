# point-api

29CM 백엔드 과제를 위한 Java 21 / Spring Boot 3 기반 포인트 서비스입니다.
현재는 적립, 적립취소, 사용, 사용취소 기능을 구현했으며, 이후 만료 배치까지 확장할 수 있도록 기본 골격을 `albus-web` 스타일에 맞춰 정리했습니다.

## Current Scope

- 포인트 적립 API `POST /api/v1/points/earn`
- 포인트 적립취소 API `POST /api/v1/points/earn-cancel`
- 포인트 사용 API `POST /api/v1/points/use`
- 포인트 사용취소 API `POST /api/v1/points/use-cancel`
- 포인트 히스토리 조회 API `GET /api/v1/points/history?userId={id}&startDate=yyyy-MM-dd&endDate=yyyy-MM-dd`
- 멱등키(`pointKey`) 기반 중복 요청 방지
- 정책 테이블 기반 적립 한도 / 만료일 / 최대 보유 한도 검증
- 관리자 수기지급 우선 사용, 만료일 임박 순 차감
- 사용 상세 추적과 사용취소 시 원 lot 복구 또는 `CANCEL_RETURN` 재적립
- H2 인메모리 DB와 초기 정책 데이터 로딩
- `MockMvc` 및 SpringBootTest 기반 테스트
- Swagger UI, Actuator health/metrics 노출

## Package Structure

```text
com.point.api
├── config
├── core
│   ├── api
│   └── exception
└── point
    ├── controller
    ├── dto
    ├── entity
    ├── repository
    └── service
```

`albus-web`처럼 공통 관심사는 `config`, `core`로 올리고, 실제 비즈니스는 도메인 패키지 아래에 두는 구성을 따랐습니다.

## Run Locally

기본 프로필은 `local`이며 H2 메모리 DB를 사용합니다.

```bash
./gradlew bootRun
```

주요 경로:

- API base: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/api-test`
- OpenAPI docs: `http://localhost:8080/swagger-ui/api-docs`
- H2 console: `http://localhost:8080/h2-console` (`JDBC URL: jdbc:h2:mem:pointdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`)
- Health: `http://localhost:8080/actuator/health`

## Test

```bash
./gradlew test
```

VS Code에서는 `Extension Pack for Java`, `Test Runner for Java` 확장을 사용하면 바로 테스트를 실행할 수 있습니다.

## Postman

Postman import 파일을 함께 제공합니다.

- Collection: `postman/point-api.postman_collection.json`
- Environment: `postman/point-api.local.postman_environment.json`

가져온 뒤 local environment를 선택하면 구현된 API와 예시 시나리오를 바로 실행할 수 있습니다.

## Assignment Architecture

AWS 기반으로 서비스한다고 가정한 제출용 아키텍처 이미지를 리소스에 포함했습니다.

- `src/main/resources/architecture/aws-backend-architecture.svg`

구성 요약:

- CloudFront / Route 53
- ALB + ECS Fargate 위 Spring Boot API
- RDS PostgreSQL
- Secrets Manager / Parameter Store
- CloudWatch / X-Ray
- SQS / EventBridge 기반 후속 비동기 처리 확장
