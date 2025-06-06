package com.historical.voting.user.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 限流标识 key，可用 userId、IP 等拼接
     */
    String key() default "";

    /**
     * 每秒最大请求次数
     */
    int permitsPerSecond() default 5;

    /**
     * 限流失败提示
     */
    String message() default "请求过于频繁，请稍后再试";

}
