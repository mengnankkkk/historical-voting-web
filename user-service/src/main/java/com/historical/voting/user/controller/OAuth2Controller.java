package com.historical.voting.user.controller;


import com.historical.voting.user.service.Impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final UserServiceImpl userService;

    @GetMapping("/success")
    public RedirectView success(@AuthenticationPrincipal OAuth2User oauth2User) {
        try {
            if (oauth2User == null) {
                log.error("OAuth2User is null");
                return new RedirectView("/login?error=oauth2");
            }

            String email = oauth2User.getAttribute("email");
            String username = oauth2User.getAttribute("login");
            String avatarUrl = oauth2User.getAttribute("avatar_url");

            if (username == null) {
                log.error("GitHub username is null");
                return new RedirectView("/login?error=github_username");
            }

            // 处理GitHub登录
            Map<String, String> tokens = userService.handleOAuth2Login(username, email, avatarUrl);

            // 重定向到前端，并带上token
            String redirectUrl = String.format("/?accessToken=%s&refreshToken=%s",
                    tokens.get("accessToken"),
                    tokens.get("refreshToken"));

            return new RedirectView(redirectUrl);
        } catch (Exception e) {
            log.error("OAuth2 login failed", e);
            return new RedirectView("/login?error=" + e.getMessage());
        }
    }

    @GetMapping("/failure")
    public RedirectView failure() {
        return new RedirectView("/login?error=oauth2");
    }
} 