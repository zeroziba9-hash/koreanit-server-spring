# PARK COMMUNITY (koreanit-server-spring)

Spring Boot 기반 **커뮤니티 백엔드 프로젝트**입니다.  
User / Post / Comment 도메인을 중심으로, 세션 인증/인가, 공통 응답·예외 처리, 운영 실행 가이드를 포함합니다.

---

## 1) 프로젝트 목적

- 계층형 백엔드 아키텍처(Controller → Service → Repository) 설계 역량 강화
- 세션 기반 인증/인가(Spring Security) 흐름 구현
- 단순 CRUD를 넘어 운영 관점(로깅, 배포 실행, Runbook)까지 반영

---

## 2) 기술 스택

- Java 17+
- Spring Boot 3.x
- Spring Web
- Spring Security (Session)
- JDBC / JdbcTemplate
- MySQL
- Gradle
- (옵션) Redis, Nginx

---

## 3) 핵심 기능

### User
- 회원가입
- 로그인 / 로그아웃
- 내 정보 조회

### Post
- 게시글 목록 / 상세 조회
- 게시글 작성
- 게시글 삭제 (관리자 또는 작성자)

### Comment
- 댓글 작성
- 게시글별 댓글 목록 조회

### Common
- `ApiResponse` 공통 응답 포맷
- `GlobalExceptionHandler` 공통 예외 처리
- 요청 로깅 필터

---

## 4) 구조

```text
spring/
├─ common/       # config, error, logging, response
├─ security/     # SecurityConfig, SessionAuthenticationFilter, AuthController
├─ user/         # UserController, UserService, UserRepository
├─ post/         # PostController, PostService, PostRepository
├─ comment/      # CommentController, CommentService, CommentRepository
└─ Application.java
```

### 계층 분리 원칙
- Controller: HTTP 입출력
- Service: 비즈니스 규칙/권한
- Repository: DB 접근(SQL)

---

## 5) 실행 방법

### 5-1. DB 생성

```sql
CREATE DATABASE koreanit_service
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
```

### 5-2. 스키마 적용

```bash
mysql -u USER -p koreanit_service < sql/schema.sql
```

### 5-3. 개발 실행

```bash
./gradlew bootRun
```

### 5-4. 빌드 실행

```bash
./gradlew clean bootJar -x test
java -jar build/libs/spring-0.0.1-SNAPSHOT.jar
```

---

## 6) 환경 변수 예시

```text
SPRING_PROFILES_ACTIVE=prod
PORT=8000

DB_URL=jdbc:mysql://localhost:3306/koreanit_service
DB_USER=USER
DB_PASSWORD=PASSWORD

# optional
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

## 7) 운영 실행 예시 (systemd)

`/etc/systemd/system/koreanit-api.service`

```ini
[Unit]
Description=Koreanit API Server
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/koreanit-api
ExecStart=/usr/bin/java -jar /opt/koreanit-api/app.jar
Restart=always
RestartSec=5
EnvironmentFile=/opt/koreanit-api/config/.env

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable koreanit-api
sudo systemctl restart koreanit-api
sudo systemctl status koreanit-api
```

---

## 8) Engineering Quality

- CI workflow (`.github/workflows/ci.yml`)
- ADR 문서 (`docs/adr/0001-jdbc-first.md`)
- Runbook (`docs/runbook.md`)
- Observability 엔드포인트 (`/actuator/health`, `/actuator/prometheus`)
- 부하 스모크 스크립트 (`perf/k6-smoke.js`)

---

## 9) 개선 예정

- API 명세(OpenAPI/Swagger) 보강
- 테스트 커버리지 확장 (Controller/Service)
- Redis 기반 세션 확장
- 배포 파이프라인 고도화
