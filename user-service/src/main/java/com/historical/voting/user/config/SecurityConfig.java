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
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
                // 登录、注册和OAuth2相关接口
                .antMatchers("/register.html").permitAll()
                .antMatchers("/api/users/register", "/api/users/register/send-code").permitAll()
                .antMatchers("/api/users/login/**", "/api/users/oauth2/**").permitAll()
                .antMatchers("/login/oauth2/code/github").permitAll()
                .antMatchers("/api/files/upload/image").permitAll()
                .antMatchers("/api/files/**").permitAll()
                // 错误页面
                .antMatchers("/error", "/login", "/login.html").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
                .and()
            .oauth2Login()
                .loginPage("/login.html")
                .defaultSuccessUrl("/api/users/oauth2/success", true)
                .failureUrl("/api/users/oauth2/failure")
                .userInfoEndpoint()
                    .userService(oauth2UserService())
                .and()
                .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return (userRequest) -> {
            OAuth2User oauth2User = delegate.loadUser(userRequest);
            return oauth2User;
        };
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