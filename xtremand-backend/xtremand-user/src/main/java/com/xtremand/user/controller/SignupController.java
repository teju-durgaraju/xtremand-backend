package com.xtremand.user.controller;

import com.xtremand.user.dto.SignupRequest;
import com.xtremand.common.dto.UserProfile;
import com.xtremand.user.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class SignupController {

    private final UserService userService;

    public SignupController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public UserProfile signup(@Validated @RequestBody SignupRequest request) {
        return userService.register(request);
    }
}
