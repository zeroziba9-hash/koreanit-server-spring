# PARK COMMUNITY Runbook

## Quick Health Check
1. `/actuator/health` (or `/health`) 확인
2. DB 연결 상태 확인
3. 로그인/게시글 목록 API 스모크 테스트

## Incident: DB connection failure
### Symptoms
- 로그인, 게시글/댓글 API에서 5xx 증가

### Actions
1. DB 프로세스/접속 계정/네트워크 확인
2. 애플리케이션 DB URL 및 credential 재확인
3. 재기동 전 DB 정상화 확인

### Recovery validation
- 회원가입/로그인 정상
- 게시글 목록 + 댓글 조회 정상

## Incident: Authorization bug
### Symptoms
- 비인가 사용자가 수정/삭제 성공

### Actions
1. 서비스 권한 검증 코드 점검
2. 임시 차단(해당 mutation endpoint)
3. 회귀 테스트 추가 후 재배포

### Recovery validation
- 작성자/관리자 외 삭제 시 403 반환
