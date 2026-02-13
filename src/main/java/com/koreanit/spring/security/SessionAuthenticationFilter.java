package com.koreanit.spring.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.koreanit.spring.user.UserEntity;
import com.koreanit.spring.user.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class SessionAuthenticationFilter extends OncePerRequestFilter {

  public static final String SESSION_USER_ID = "LOGIN_USER_ID";

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;

  public SessionAuthenticationFilter(
      UserRepository userRepository,
      UserRoleRepository userRoleRepository) {
    this.userRepository = userRepository;
    this.userRoleRepository = userRoleRepository;
  }

  private boolean needsAuthInjection(Authentication a) {
    return (a == null) || (a instanceof AnonymousAuthenticationToken);
  }

  private List<SimpleGrantedAuthority> resolveAuthorities(Long userId) {

    List<String> roles = userRoleRepository.findRolesByUserId(userId);

    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    boolean hasUserRole = false;

    for (String role : roles) {
      if ("ROLE_USER".equals(role)) {
        hasUserRole = true;
      }
      authorities.add(new SimpleGrantedAuthority(role));
    }

    if (!hasUserRole) {
      authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    }

    return authorities;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    SecurityContext context = SecurityContextHolder.getContext();
    Authentication cur = context.getAuthentication();

    if (needsAuthInjection(cur)) {

      HttpSession session = request.getSession(false);
      if (session != null) {
        Object v = session.getAttribute(SESSION_USER_ID);

        if (v instanceof Long userId) {

          try {
            UserEntity user = userRepository.findById(userId);

            LoginUser principal = new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getNickname());

            Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                resolveAuthorities(userId));

            // Authentication을 SecurityContext에 주입
            context.setAuthentication(auth);
          } catch (EmptyResultDataAccessException e) {
            // 사용자 조회 실패 시 무시(인증 주입 안 함)
            // 세션에 쓰레기 userId가 남은 케이스: 로그인 해제 처리
            session.removeAttribute(SESSION_USER_ID);
            // 또는 session.invalidate();
            // 인증 주입 안 하고 익명으로 통과
          }
        }
      }
    }

    filterChain.doFilter(request, response);
  }
}