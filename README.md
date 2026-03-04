# 🧩 koreanit-server-spring

Spring Boot 기반 커뮤니티 서버 프로젝트입니다.
User / Post / Comment 중심으로 인증·인가와 게시판 흐름을 구현했습니다.

---

## 📌 프로젝트 목적
- 계층형 백엔드 아키텍처(Controller/Service/Repository) 실습
- 세션 기반 인증/인가 흐름 이해
- 게시글/댓글 도메인 중심 API 구현

## 🧱 기술 스택
- Java 17
- Spring Boot 3.x
- Spring Security
- JDBC/JdbcTemplate
- MySQL
- Gradle

## ✨ 핵심 기능
- 사용자 인증 및 권한 기반 접근 제어
- 게시글/댓글 CRUD
- 관리자 권한 기능

## ⚡ 실행 방법
```bash
./gradlew bootRun
```
(Windows: `gradlew.bat bootRun`)

## 📁 디렉토리 구조
- `src/main/java` : 서버 소스 코드
- `src/main/resources` : 설정 파일
- `.api-test/` : API 테스트 관련 자료

## ✅ 상태
- 로컬 개발 환경 기준 주요 기능 동작 확인
