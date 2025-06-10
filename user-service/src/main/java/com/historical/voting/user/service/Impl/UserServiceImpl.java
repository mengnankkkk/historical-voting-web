package com.historical.voting.user.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.historical.voting.user.annotation.RateLimit;
import com.historical.voting.user.config.JwtConfig;
import com.historical.voting.user.entity.User;
import com.historical.voting.user.entity.type.UserRank;
import com.historical.voting.user.exception.BusinessException;

import com.historical.voting.user.mapper.UserMapper;
import com.historical.voting.user.repository.UserRepository;
import com.historical.voting.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final EmailServiceImpl emailService;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    @RateLimit(permitsPerSecond = 3,key ="#ip")
    public void register(String username, String password, String email, String verificationCode) {
        // 验证验证码
        if (!emailService.verifyCode(email, verificationCode)) {
            throw new BusinessException("验证码无效或已过期");
        }

        // 检查用户名和邮箱是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setIsEnabled(true);
        
        userRepository.save(user);
    }

    @RateLimit(permitsPerSecond = 5,key = "#ip")
    public Map<String, String> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!user.getIsEnabled()) {
            throw new BusinessException("账号未激活");
        }

        if (user.getIsLocked()) {
            throw new BusinessException("账号已被锁定");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 生成令牌
        String accessToken = jwtConfig.generateAccessToken(username);
        String refreshToken = jwtConfig.generateRefreshToken(username);

        // 存储令牌到Redis
        String accessTokenKey = "token:access:" + username;
        String refreshTokenKey = "token:refresh:" + username;
        redisTemplate.opsForValue().set(accessTokenKey, accessToken, 30, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 7, TimeUnit.DAYS);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    public void logout(String username) {
        // 将当前token加入黑名单
        String accessTokenKey = "token:access:" + username;
        String refreshTokenKey = "token:refresh:" + username;
        String accessToken = redisTemplate.opsForValue().get(accessTokenKey);
        
        if (accessToken != null) {
            String blacklistKey = "token:blacklist:" + accessToken;
            redisTemplate.opsForValue().set(blacklistKey, "", 30, TimeUnit.MINUTES);
        }

        // 删除Redis中的token
        redisTemplate.delete(accessTokenKey);
        redisTemplate.delete(refreshTokenKey);
    }

    @RateLimit(permitsPerSecond = 3,key = "#ip")
    public void updateExperience(String username, int amount) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        user.setExperience(user.getExperience() + amount);
        
        // 检查是否需要升级
        checkAndUpdateLevel(user);
        
        userRepository.save(user);
    }


    private void checkAndUpdateLevel(User user) {
        int currentExp = user.getExperience();
        int currentLevel = user.getLevel();
        
        // 简单的升级规则：每1000经验升一级
        int newLevel = (currentExp / 1000) + 1;
        if (newLevel > currentLevel) {
            user.setLevel(newLevel);
            // 根据等级更新称号
            updateRank(user);
        }
    }

    private void updateRank(User user) {
        int level = user.getLevel();
        if (level >= 20) {
            user.setUserRank(UserRank.HISTORY_MASTER);
        } else if (level >= 15) {
            user.setUserRank(UserRank.SENIOR_COMMENTATOR);
        } else if (level >= 10) {
            user.setUserRank(UserRank.HISTORY_ENTHUSIAST);
        } else if (level >= 5) {
            user.setUserRank(UserRank.ACTIVE_VOTER);
        } else {
            user.setUserRank(UserRank.NEWCOMER);
        }
    }

    public User getUserProfile(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    public void updateProfile(String username, User updateInfo) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 更新基本信息
        Optional.ofNullable(updateInfo.getNickname()).ifPresent(user::setNickname);
        Optional.ofNullable(updateInfo.getBirthday()).ifPresent(user::setBirthday);
        Optional.ofNullable(updateInfo.getAvatar()).ifPresent(user::setAvatar);
        Optional.ofNullable(updateInfo.getPhoneNumber()).ifPresent(user::setPhoneNumber);

        userRepository.save(user);
    }
} 