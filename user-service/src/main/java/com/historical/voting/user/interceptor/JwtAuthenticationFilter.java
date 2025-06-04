package com.historical.voting.user.interceptor;

import com.historical.voting.user.config.JwtConfig;
import com.historical.voting.user.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 不需要验证token的路径
        String path = request.getRequestURI();
        if (shouldSkipAuthentication(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            processAuthentication(request, response, filterChain);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }

    private boolean shouldSkipAuthentication(String path) {
        return path.startsWith("/api/users/register") || 
               path.startsWith("/api/users/login") || 
               path.startsWith("/api/users/oauth2") ||
               !path.startsWith("/api");
    }

    private void processAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        try {
            // 首先尝试使用访问令牌
            String accessToken = extractToken(request, "Authorization");
            processAccessToken(accessToken, request, response, filterChain);
        } catch (ExpiredJwtException e) {
            // 访问令牌过期，尝试使用刷新令牌
            processRefreshToken(request, response, filterChain);
        } catch (Exception e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }

    private void processAccessToken(String accessToken, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        Claims claims = jwtConfig.validateAccessToken(accessToken);
        String username = claims.getSubject();
        
        // 检查令牌是否在Redis黑名单中
        String blacklistKey = "token:blacklist:" + accessToken;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            throw new UnauthorizedException("Token has been revoked");
        }

        // 检查Redis中是否存在有效的访问令牌
        String storedToken = redisTemplate.opsForValue().get("token:access:" + username);
        if (storedToken == null || !storedToken.equals(accessToken)) {
            throw new UnauthorizedException("Invalid access token");
        }
        
        request.setAttribute("username", username);
        filterChain.doFilter(request, response);
    }

    private void processRefreshToken(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        try {
            String refreshToken = extractToken(request, "Refresh-Token");
            Claims refreshClaims = jwtConfig.validateRefreshToken(refreshToken);
            String username = refreshClaims.getSubject();

            // 验证Redis中的刷新令牌
            String storedRefreshToken = redisTemplate.opsForValue().get("token:refresh:" + username);
            if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                throw new UnauthorizedException("Invalid refresh token");
            }
            
            // 生成新的访问令牌
            String newAccessToken = jwtConfig.generateAccessToken(username);
            
            // 更新Redis中的访问令牌
            redisTemplate.opsForValue().set("token:access:" + username, newAccessToken, 30, java.util.concurrent.TimeUnit.MINUTES);
            
            // 在响应头中返回新的访问令牌
            response.setHeader("New-Access-Token", newAccessToken);
            
            request.setAttribute("username", username);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid refresh token");
        }
    }

    private String extractToken(HttpServletRequest request, String headerName) {
        String header = request.getHeader(headerName);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        throw new UnauthorizedException("No token found in " + headerName + " header");
    }
} 