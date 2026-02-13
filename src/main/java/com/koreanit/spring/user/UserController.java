package com.koreanit.spring.user;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.koreanit.spring.common.response.ApiResponse;
import com.koreanit.spring.user.dto.request.UserCreateRequest;
import com.koreanit.spring.user.dto.request.UserEmailChangeRequest;
import com.koreanit.spring.user.dto.request.UserNicknameChangeRequest;
import com.koreanit.spring.user.dto.request.UserPasswordChangeRequest;
import com.koreanit.spring.user.dto.response.UserResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody UserCreateRequest req) {
        return ApiResponse
                .ok(userService.create(req.getUsername(), req.getPassword(), req.getEmail(), req.getNickname()));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(UserMapper.toResponse(userService.get(id)));
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> list(@RequestParam(defaultValue = "1000") int limit) {
        return ApiResponse.ok(UserMapper.toResponseList(userService.list(limit)));
    }

    @PutMapping("/{id}/nickname")
    public ApiResponse<Void> changeNickname(@PathVariable Long id, @Valid @RequestBody UserNicknameChangeRequest req) {
        userService.changeNickname(id, req.getNickname());
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Void> changePassword(@PathVariable Long id, @Valid @RequestBody UserPasswordChangeRequest req) {
        userService.changePassword(id, req.getPassword());
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/email")
    public ApiResponse<Void> chanegeEmail(@PathVariable Long id, @Valid @RequestBody UserEmailChangeRequest req) {
        userService.changeEmail(id, req.getEmail());
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok();
    }
}
