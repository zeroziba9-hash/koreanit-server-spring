package com.koreanit.spring.user;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.koreanit.spring.common.error.ApiException;
import com.koreanit.spring.common.error.ErrorCode;
import com.koreanit.spring.security.SecurityUtils;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;

@Service
public class UserService {

  // list 조회 시, 클라이언트가 지나치게 큰 limit을 넣어 서버가 느려지는 것을 방지
  private static final int MAX_LIMIT = 1000;

  // DB 접근 인터페이스(구현체: JdbcUserRepository 등)
  private final UserRepository userRepository;

  // 비밀번호 해시(암호화) / 검증을 위한 Spring Security 유틸
  private final PasswordEncoder passwordEncoder;

  // 생성자 주입: 테스트/교체 용이, 의존성 명확
  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  // ============================
  // 공통 유틸: limit 검증/보정
  // ============================
  private int normalizeLimit(int limit) {
    // 0 이하로 들어오면 요청 자체가 잘못된 것
    if (limit <= 0) {
      throw new IllegalArgumentException("limit 은 1 이상 입력해주세요");
    }

    // 너무 큰 값은 MAX_LIMIT로 잘라서 방어
    return Math.min(limit, MAX_LIMIT);
  }

  public boolean isSelf(Long userId) {
    Long currentUserId = SecurityUtils.currentUserId();
    return currentUserId != null && userId != null && currentUserId.equals(userId);
  }

  // ============================
  // 공통 유틸: 중복키 에러 메시지 사용자 친화적으로 변환
  // ============================
  private String toDuplicateMessage(DuplicateKeyException e) {
    // 메시지 null 방어
    String m = (e.getMessage() == null) ? "" : e.getMessage();

    // DB/드라이버마다 메시지 포맷이 조금씩 다르므로
    // "어떤 key에서 중복이 났는지" 키 이름 기반으로 판별
    if (m.contains("for key") && (m.contains("users.username") || m.contains("'username'") || m.contains("username"))) {
      return "이미 존재하는 username입니다";
    }
    if (m.contains("for key") && (m.contains("users.email") || m.contains("'email'") || m.contains("email"))) {
      return "이미 존재하는 email입니다";
    }

    // 그 외는 일반 중복 처리
    return "이미 존재하는 값입니다";
  }

  // ============================
  // 사용자 생성(회원가입)
  // ============================
  // 정상 흐름: 입력값 정규화 → 비밀번호 해시 → DB 저장 → PK 반환
  public Long create(String username, String password, String email, String nickname) {

    // username/nickname은 공백 제거 + 소문자 통일(정규화)
    // ※ 여기서 null 들어오면 NPE 가능 -> Controller에서 @Valid로 막거나 여기서 체크해도 됨
    username = username.trim().toLowerCase();
    nickname = nickname.trim().toLowerCase();

    // email은 optional일 수 있으니 null-safe로 정규화
    // (원하면 trim()도 같이 하는게 보통 더 안전함)
    String normalizedEmail = (email == null) ? null : email.toLowerCase();

    // 비밀번호는 반드시 해시로 저장해야 함(평문 저장 금지)
    String hash = passwordEncoder.encode(password);

    try {
      // Repository는 "DB에 저장"만 책임지고
      // Service는 "규칙/정책(정규화, 예외 변환)"을 책임진다
      return userRepository.save(username, hash, nickname, normalizedEmail);

    } catch (DuplicateKeyException e) {
      // DB unique 제약(중복) 발생 시 → API 예외로 변환하여 클라이언트에 의미있는 메시지 전달
      throw new ApiException(
          ErrorCode.DUPLICATE_RESOURCE,
          toDuplicateMessage(e));
    }
  }

  // ============================
  // 단일 사용자 조회
  // ============================
  @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#id)")
  public User get(Long id) {
    try {
      // DB에서 엔티티 조회(없으면 EmptyResultDataAccessException)
      UserEntity e = userRepository.findById(id);

      // Entity(DB모델) → Domain(User) 변환
      // ※ Service는 외부로 Entity를 직접 노출하지 않는 게 좋음
      return UserMapper.toDomain(e);

    } catch (EmptyResultDataAccessException e) {
      // 결과가 없으면 404 성격의 API 예외로 변환
      throw new ApiException(
          ErrorCode.NOT_FOUND_RESOURCE,
          "존재하지 않는 사용자입니다. id=" + id);
    }
  }

  // ============================
  // 사용자 목록 조회
  // ============================
  @PreAuthorize("hasRole('ADMIN')")
  public List<User> list(int limit) {
    // limit 검증/보정 → DB 조회 → Entity List를 Domain List로 변환해서 반환
    return UserMapper.toDomainList(
        userRepository.findAll(normalizeLimit(limit)));
  }

  // ============================
  // 닉네임 변경
  // ============================
  @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#id)")
  public void changeNickname(Long id, String nickname) {

    // nickname 정규화
    nickname = nickname.trim().toLowerCase();

    // 1) 사용자 존재 확인: 없으면 get()에서 NOT_FOUND_RESOURCE 예외 발생
    User user = get(id);

    // 2) 동일 값 변경 방지(불필요한 업데이트/이력 방지)
    // ※ user.getNickname()이 null일 가능성이 있다면 null-safe 비교로 바꾸면 더 튼튼함
    if (user.getNickname().equals(nickname)) {
      throw new ApiException(
          ErrorCode.INVALID_REQUEST,
          "기존 닉네임과 동일한 닉네임으로 변경할 수 없습니다.");
    }

    // 3) DB 업데이트 수행
    int updated = userRepository.updateNickname(id, nickname);

    // updated == 0 이면 실제로 업데이트된 row가 없음
    // (id가 없거나 where 조건 불일치 등)
    if (updated == 0) {
      throw new ApiException(
          ErrorCode.NOT_FOUND_RESOURCE,
          "닉네임 변경에 실패하였습니다. id=" + id);
    }
  }

  // ============================
  // 비밀번호 변경
  // ============================
  @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#id)")
  public void changePassword(Long id, String password) {

    // 1) 사용자 존재 확인 및 기존 비밀번호 해시 조회
    User user = get(id);

    // 2) 새 비밀번호가 기존과 같은지 검사
    // matches(평문, 해시) 형태로 비교한다
    boolean ok = passwordEncoder.matches(password, user.getPassword());
    if (ok) {
      throw new ApiException(ErrorCode.INVALID_REQUEST,
          "기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
    }

    // 3) 새 비밀번호를 해시로 만들어 DB 저장
    String passworedHash = passwordEncoder.encode(password);

    int updated = userRepository.updatePassword(id, passworedHash);

    // 변경 실패(업데이트된 row가 0)면 예외
    if (updated == 0) {
      throw new ApiException(
          ErrorCode.NOT_FOUND_RESOURCE,
          "존재하지 않는 사용자입니다. id=" + id);
    }
  }

  // ============================
  // 이메일 변경
  // ============================
  @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#id)")
  public void changeEmail(Long id, String email) {

    // 1) 이메일 정규화(null-safe)
    // - null 허용할지 정책 필요(현재 코드는 null도 update로 들어갈 수 있음)
    String normalizedEmail = (email == null)
        ? null
        : email.trim().toLowerCase();

    // 2) 사용자 존재 확인
    User user = get(id);

    // 3) 기존 이메일과 동일하면 변경 막기
    // ※ normalizedEmail이 null일 때 equalsIgnoreCase 호출이 안 되도록 user.getEmail() != null
    // 체크
    if (user.getEmail() != null &&
        user.getEmail().equalsIgnoreCase(normalizedEmail)) {

      throw new ApiException(
          ErrorCode.INVALID_REQUEST,
          "기존 이메일과 동일한 이메일로 변경할 수 없습니다.");
    }

    // 4) DB 업데이트
    int updated = userRepository.updateEmail(id, normalizedEmail);

    if (updated == 0) {
      throw new ApiException(
          ErrorCode.NOT_FOUND_RESOURCE,
          "존재하지 않는 사용자입니다. id=" + id);
    }
  }

  // ============================
  // 사용자 삭제
  // ============================
  @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#id)")
  public void delete(Long id) {

    // delete 실행
    int deleted = userRepository.deleteById(id);

    // 삭제된 row가 0이면 존재하지 않는 사용자
    if (deleted == 0) {
      throw new ApiException(
          ErrorCode.NOT_FOUND_RESOURCE,
          "존재하지 않는 사용자입니다. id=" + id);
    }
  }

  // ============================
  // 로그인
  // ============================
  public Long login(String username, String password) {
    try {
      // username으로 사용자 조회(없으면 EmptyResultDataAccessException)
      UserEntity en = userRepository.findByUsername(username);

      // 입력 비밀번호(평문) vs 저장된 비밀번호(해시) 비교
      boolean ok = passwordEncoder.matches(password, en.getPassword());

      if (!ok) {
        // 비밀번호 틀림
        throw new ApiException(ErrorCode.INVALID_REQUEST,
            "비밀번호가 올바르지 않습니다.");
      }

      // 로그인 성공 시 사용자 id 반환(세션/토큰 발급 등에 사용)
      return en.getId();

    } catch (EmptyResultDataAccessException e) {
      // username 자체가 없음
      throw new ApiException(ErrorCode.NOT_FOUND_RESOURCE,
          "존재하지 않는 사용자입니다. username=" + username);
    }
  }
}
