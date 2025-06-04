package com.historical.voting.user.service.Impl;

import com.historical.voting.user.exception.FileProcessException;
import com.historical.voting.user.service.VideoProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
@Service
public class VideoProcessServiceImpl implements VideoProcessService {

    @Value("${app.upload.video-path}")
    private String videoUploadPath;

    @Value("${app.upload.thumbnail-path}")
    private String thumbnailPath;

    public String processVideo(String inputPath) {
        try {
            String outputFileName = "processed_" + new File(inputPath).getName();
            String outputPath = Paths.get(videoUploadPath, outputFileName).toString();
            
            // 使用FFmpeg进行视频转码
            ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", inputPath,
                "-c:v", "libx264",     // 视频编码器
                "-crf", "23",          // 压缩质量（0-51，值越小质量越好）
                "-preset", "medium",    // 编码速度预设
                "-c:a", "aac",         // 音频编码器
                "-b:a", "128k",        // 音频比特率
                "-movflags", "+faststart",  // 支持在下载时播放
                outputPath
            );
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new FileProcessException("视频转码失败");
            }
            
            // 提取视频封面
            extractThumbnail(inputPath);
            
            return outputFileName;
        } catch (IOException | InterruptedException e) {
            log.error("视频处理失败", e);
            throw new FileProcessException("视频处理失败", e);
        }
    }
    
    public String extractThumbnail(String videoPath) {
        try {
            String thumbnailFileName = new File(videoPath).getName().replaceFirst("[.][^.]+$", "") + "_thumb.jpg";
            String outputPath = Paths.get(thumbnailPath, thumbnailFileName).toString();
            
            // 使用FFmpeg提取第一帧作为封面
            ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", videoPath,
                "-vframes", "1",    // 只提取一帧
                "-an",             // 不处理音频
                "-ss", "0",        // 从视频开始处截取
                "-y",              // 覆盖已存在的文件
                outputPath
            );
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new FileProcessException("封面提取失败");
            }
            
            return thumbnailFileName;
        } catch (IOException | InterruptedException e) {
            log.error("封面提取失败", e);
            throw new FileProcessException("封面提取失败", e);
        }
    }
} 