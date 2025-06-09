package com.historical.voting.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.ffmpeg")
public class FfmpegConfig  {
    /**
     * ffmpeg 可执行文件路径
     */
    private String executable;
}
