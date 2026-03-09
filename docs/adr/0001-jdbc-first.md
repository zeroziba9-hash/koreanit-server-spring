# ADR-0001: JDBC/JdbcTemplate first, JPA later

## Status
Accepted

## Context
이 프로젝트의 1차 목표는 SQL, 트랜잭션 경계, 권한 기반 비즈니스 규칙을 직접 제어하는 백엔드 학습/검증입니다.

## Decision
초기 구현은 JDBC/JdbcTemplate 중심으로 유지합니다.

## Rationale
- SQL 실행 경로가 명확해 디버깅이 쉬움
- 쿼리 튜닝 포인트를 직접 확인 가능
- 도메인 규칙과 DB 접근 책임을 분리해서 학습 가능

## Trade-offs
- 반복 코드가 늘어남
- 생산성은 JPA 대비 낮을 수 있음

## Follow-up
도메인이 안정화되면 일부 조회/복잡 연관관계부터 JPA 도입 여부를 재평가합니다.
