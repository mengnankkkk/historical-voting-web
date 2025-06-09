package com.historical.voting.user.config;


import com.historical.voting.user.annotation.RateLimit;
import com.historical.voting.user.exception.RateLimitException;
import com.historical.voting.user.util.RateLimiterUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Component
@Aspect
public class RateLimitAspect {

    private final RateLimiterUtil rateLimiterUtil;


    public RateLimitAspect(RateLimiterUtil rateLimiterUtil) {
        this.rateLimiterUtil = rateLimiterUtil;
    }

    @Around("@annotation(com.historical.voting.user.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        //获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);


        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String key = rateLimit.key();
        if (key.isEmpty()){
            key = request.getRemoteAddr()+":"+method.getName();
        }

        int permits = rateLimit.permitsPerSecond();
        String message  =rateLimit.message();


        if (!rateLimiterUtil.resolveBucket(key,permits).tryConsume(1)){
            throw new RateLimitException(message);
        }

        return joinPoint.proceed();
    }
}
