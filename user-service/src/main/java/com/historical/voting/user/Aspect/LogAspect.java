package com.historical.voting.user.Aspect;


import com.historical.voting.user.annotation.LoginLog;
import com.historical.voting.user.annotation.OperationLog;
import com.historical.voting.user.dto.LogDTO;
import com.historical.voting.user.util.AsyncLogUtils;
import com.historical.voting.user.util.RequestContextUtils;
import com.historical.voting.user.util.SecurityUtils;
import com.mysql.cj.log.Log;
import com.mysql.cj.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.Joinpoint;
import org.apache.catalina.connector.Request;
import org.apache.catalina.security.SecurityUtil;
import org.apache.kafka.common.protocol.types.Field;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.security.Security;
import java.time.LocalDateTime;

import static sun.net.www.protocol.http.HttpURLConnection.userAgent;

@Aspect
@Component
@Slf4j
public class LogAspect {
    @Autowired
    private AsyncLogUtils asyncLogUtils;

    @Pointcut("@annotation(com.historical.voting.user.annotation.OperationLog)")
    public void operationLogPointcut() {}

    @Pointcut("@annotation(com.historical.voting.user.annotation.LoginLog)")
    public void loginLogPointcut() {}

    @Around("operationLogPointcut()")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable{
        Method method  = ((MethodSignature)joinPoint.getSignature()).getMethod();
        OperationLog logAnno = method.getAnnotation(OperationLog.class);


        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration =System.currentTimeMillis()-start;

        LogDTO log = buildLog(joinPoint,logAnno.type(),logAnno.action(),duration);
        asyncLogUtils.record(log);
        return result;
    }
    @Around("loginLogPointcut()")
    public Object logLogin(ProceedingJoinPoint joinPoint) throws Throwable{
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        LoginLog loganno = method.getAnnotation(LoginLog.class);

        Object result = joinPoint.proceed();
        LogDTO logDTO = buildLog(joinPoint,"登录模块",loganno.action(),0);
        asyncLogUtils.record(logDTO);
        return result;
    }

    private LogDTO buildLog(ProceedingJoinPoint joinpoint,String value,String action,long time){
        HttpServletRequest request = RequestContextUtils.getRequest();
        String ip  =request.getRemoteAddr();
        String UserAgent = request.getHeader("User-Agent");
        String username = SecurityUtils.getCurrentUsername();
        return LogDTO.builder()
                .username(username)
                .module(value)
                .action(action)
                .ip(ip)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .duration(time)
                .build();
    }
}
