package com.historical.voting.user.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * 获取当前登录用户名
     * @return 用户名，未登录时返回 "anonymous"
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // 通常是用户名
        }
        return "anonymous";
    }
}
