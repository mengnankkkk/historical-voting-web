package com.historical.voting.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LogDTO {
    private String username;
    private String module;    // 模块：订单、用户、登录等
    private String action;    // 动作：下单、收藏、登录
    private String ip;
    private String userAgent;
    private LocalDateTime timestamp;
    private long duration;    // 接口耗时
}
