# point-api

Java 21, Spring Boot 3.x, H2 기반의 포인트 백엔드 기본 프로젝트입니다.

현재 포함된 범위:

- 포인트 적립 엔드포인트 1개
- JPA/H2 기본 설정
- `MockMvc` 기반 컨트롤러 테스트
- VS Code `Test Runner for Java` 사용을 위한 기본 설정

## Test Runner

VS Code에서 바로 테스트를 실행하려면 다음 확장을 사용하면 됩니다.

- `Extension Pack for Java`
- `Test Runner for Java`

프로젝트를 열면 `.vscode` 설정으로 Gradle import가 자동 반영됩니다.
테스트는 테스트 파일 상단의 Run Test / Debug Test 버튼이나 `Gradle Test` task로 실행할 수 있습니다.

## Gradle test

```bash
./gradlew test
```
