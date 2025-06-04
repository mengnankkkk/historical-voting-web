package com.historical.voting.user.service.Impl;

import com.historical.voting.user.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.MailException;
import com.historical.voting.user.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private static final long VERIFICATION_CODE_EXPIRATION = 5; // 验证码有效期（分钟）

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String email) {
        String verificationCode = generateVerificationCode();
        
        // 存储验证码到Redis
        String redisKey = "verification:code:" + email;
        redisTemplate.opsForValue().set(redisKey, verificationCode, VERIFICATION_CODE_EXPIRATION, TimeUnit.MINUTES);

        // 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);  // 使用配置文件中的邮箱地址
        message.setTo(email);
        message.setSubject("历史投票系统 - 验证码");
        message.setText("尊敬的用户：\n\n您好！您正在注册历史投票系统，您的验证码是: " + verificationCode + 
                      "\n\n该验证码将在" + VERIFICATION_CODE_EXPIRATION + "分钟后过期。\n\n" +
                      "如果这不是您的操作，请忽略此邮件。\n\n" +
                      "历史投票系统团队");
        
        try {
            mailSender.send(message);
            log.info("验证码邮件已发送至: {}", email);
        } catch (MailException e) {
            log.error("发送验证码邮件失败: {}", e.getMessage());
            redisTemplate.delete(redisKey);  // 删除Redis中的验证码
            throw new BusinessException("验证码发送失败，请稍后重试: " + e.getMessage());
        }
    }

    public boolean verifyCode(String email, String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        String redisKey = "verification:code:" + email;
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        
        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(redisKey);
            log.info("验证码验证成功: {}", email);
            return true;
        }
        log.warn("验证码验证失败: {}", email);
        return false;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
} 