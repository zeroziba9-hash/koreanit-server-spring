package com.koreanit.spring.user.dto.response;

import java.time.LocalDateTime;

import com.koreanit.spring.user.User;

public class UserResponse {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getNickname() { return nickname; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // domain -> dto 
    public static UserResponse from(User u) {
        UserResponse r = new UserResponse();
        r.id = u.getId();
        r.username = u.getUsername();
        r.password = u.getPassword();
        r.email = u.getEmail();
        r.nickname = u.getNickname();
        r.createdAt = u.getCreatedAt();
        return r;
    }
}