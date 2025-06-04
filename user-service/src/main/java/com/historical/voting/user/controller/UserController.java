package com.historical.voting.user.controller;

import com.historical.voting.user.entity.User;


import javax.validation.Valid;

import com.historical.voting.user.service.EmailService;
import com.historical.voting.user.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/register/send-code")
    public ResponseEntity<Void> sendVerificationCode(@RequestParam String email) {
        emailService.sendVerificationCode(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {
        userService.register(
            request.getUsername(),
            request.getPassword(),
            request.getEmail(),
            request.getVerificationCode()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid LoginRequest request) {
        Map<String, String> tokens = userService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestAttribute String username) {
        userService.logout(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@RequestAttribute String username) {
        User user = userService.getUserProfile(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @RequestAttribute String username,
            @RequestBody @Valid UpdateProfileRequest request) {
        User updateInfo = new User();
        updateInfo.setNickname(request.getNickname());
        updateInfo.setBirthday(request.getBirthday());
        updateInfo.setAvatar(request.getAvatar());
        updateInfo.setPhoneNumber(request.getPhoneNumber());

        userService.updateProfile(username, updateInfo);
        return ResponseEntity.ok().build();
    }
}

@Data
class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String verificationCode;
}

@Data
class LoginRequest {
    private String username;
    private String password;
}

@Data
class UpdateProfileRequest {
    private String nickname;
    private String avatar;
    private String phoneNumber;
    private java.time.LocalDate birthday;
} 