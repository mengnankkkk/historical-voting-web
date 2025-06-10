package com.historical.voting.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.historical.voting.user.interceptor.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                // 静态资源
                .antMatchers("/", "/index.html", "/static/**", "/favicon.ico").permitAll()
                .antMatchers("/css/**", "/js/**", "/img/**", "/fonts/**", "/webjars/**").permitAll()
                // 登录、注册接口
                .antMatchers("/register.html").permitAll()
                .antMatchers("/api/users/register", "/api/users/register/send-code").permitAll()
                .antMatchers("/api/users/login/**").permitAll()
                .antMatchers("/api/files/upload/image").permitAll()
                .antMatchers("/api/files/**").permitAll()
                // 错误页面
                .antMatchers("/error", "/login", "/login.html").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
                .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 