package com.koreanit.spring.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null)
            return null;

        Object p = a.getPrincipal();
        if (p instanceof LoginUser u) {
            return u.getId();
        }
        return null;
    }

    public static boolean hasRole(String roleNameWithoutPrefix) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getAuthorities() == null) {
            return false;
        }

        String target = "ROLE_" + roleNameWithoutPrefix;
        for (GrantedAuthority authority : a.getAuthorities()) {
            if (target.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}