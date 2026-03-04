# PARK COMMUNITY

## 1) 프로젝트 개요

PARK COMMUNITY는 **Spring Boot 기반 커뮤니티 웹 서비스**입니다.  
User / Post / Comment 도메인을 중심으로, 인증·인가가 포함된 서버 구조와 실제 서비스형 화면 구성을 구현했습니다.

### 프로젝트 목적
- 계층형 백엔드 아키텍처(Controller → Service → Repository) 설계 역량 학습
- 세션 기반 인증/인가(Spring Security) 흐름 이해
- 게시글/댓글 중심 커뮤니티 기능을 실제 서비스 형태로 통합 구현
- 정적 프론트(HTML/CSS/JS)와 REST API 연동 경험 확보

### 해결하려는 문제
단순 CRUD 예제를 넘어서,
- **비로그인/로그인/관리자** 권한 차이를 반영하고
- 게시글/댓글 작성 및 조회, 관리자 공지 관리 등
- 실제 운영형 커뮤니티에서 필요한 핵심 흐름을 구현하는 것을 목표로 했습니다.

### 프로젝트 유형
- **개인 프로젝트** (1인 개발)

---

## 2) 기술 스택 (선택 이유 포함)

- **Java 17**  
  LTS 버전으로 안정성이 높고, 최신 문법/런타임 최적화를 사용할 수 있어 선택했습니다.

- **Spring Boot 3.x**  
  설정 단순화와 빠른 개발 생산성을 위해 선택했습니다. REST API 서버를 표준 방식으로 구성하기 좋습니다.

- **Spring Security (세션 기반 인증)**  
  로그인 상태를 세션으로 관리하여 웹 서비스에 적합한 인증 흐름을 학습/구현하기 위해 사용했습니다.

- **JDBC / JdbcTemplate**  
  ORM에 의존하기 전에 SQL과 데이터 접근 흐름을 명확히 이해하기 위해 선택했습니다.

- **MySQL**  
  범용성이 높고 학습/운영 모두에서 검증된 관계형 DB라 선택했습니다.

- **Gradle**  
  빌드 속도와 유연한 태스크 관리가 가능해 프로젝트 빌드 도구로 사용했습니다.

- *(선택)* **Redis (세션 스토어 확장 가능)**  
  현재는 기본 세션 방식이지만, 확장 시 Redis 기반 세션 저장 구조로 전환 가능한 형태를 고려했습니다.

---

## 3) 프로젝트 구조

### 패키지 구조

```text
com.koreanit.spring
├─ user
├─ post
├─ comment
├─ security
└─ common
```

### 계층 분리 원칙

- **Controller**  
  HTTP 요청/응답 처리, 파라미터 검증, API 입출력 책임

- **Service**  
  비즈니스 로직과 권한 규칙 처리  
  (예: 게시글 삭제 시 관리자 또는 작성자 본인만 허용)

- **Repository**  
  DB 접근 계층. SQL 실행 및 영속성 처리 담당

### 데이터 모델/전달 구조

- **Entity**: DB 테이블과 매핑되는 영속 데이터 구조
- **Domain**: 비즈니스 로직에 사용되는 핵심 모델
- **DTO(Request/Response)**: API 입출력 전용 객체

핵심 흐름:
- `Controller → Service → Repository`
- `Entity ↔ Domain ↔ DTO`로 책임을 분리해 유지보수성을 높였습니다.

---

## 4) 주요 기능

### [User]
- 회원가입
- 로그인 / 로그아웃
- 세션 기반 인증
- 내 정보 조회(`/api/me`)
- 권한 조회(`/api/me/permissions`)

### [Post]
- 게시글 목록 조회 (비로그인 허용)
- 게시글 상세 조회 (비로그인 허용)
- 게시글 작성 (로그인 필요)
- 게시글 삭제 (**관리자 또는 작성자 본인만 가능**)
- 관리자 공지 등록(공지 태그 처리 및 상단 노출)

### [Comment]
- 댓글 작성 (로그인 필요)
- 게시글별 댓글 조회 (비로그인 허용)
- 댓글 작성자 닉네임 표시

### [UI/운영 기능]
- 메인(index)에서 게시글 목록/댓글 수/새로고침 제공
- 로그인 상태에 따른 상단 메뉴/버튼 동적 처리
- 다크모드(Black & Red) 테마 적용

---

## 5) 실행 방법

### 사전 요구사항
- Java 17
- MySQL 실행 중
- (필요 시) DB 스키마/테이블 준비

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd koreanit-server/spring
```

### 2. 설정 확인
- `src/main/resources/application-*.yml`에서 DB 접속 정보 확인
  - URL
  - username
  - password

### 3. 실행 방법 A (개발 모드)
```bash
./gradlew bootRun
```

### 4. 실행 방법 B (JAR 빌드 후 실행)
```bash
./gradlew clean bootJar -x test
java -Xms128m -Xmx512m -jar build/libs/spring-0.0.1-SNAPSHOT.jar
```

### 5. 접속
- 메인: `http://localhost:8080/index.html`
- 상세: `http://localhost:8080/post.html?id={postId}`
- 로그인: `http://localhost:8080/auth.html`
- 회원가입: `http://localhost:8080/signup.html`
- 마이페이지: `http://localhost:8080/mypage.html`
