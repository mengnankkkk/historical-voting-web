package com.historical.voting.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.historical.voting.user.Factory.TranscodingStrategyFactory;
import com.historical.voting.user.annotation.RateLimit;
import com.historical.voting.user.entity.Video;
import com.historical.voting.user.exception.FileProcessException;
import com.historical.voting.user.mapper.VideoMapper;
import com.historical.voting.user.service.VideoService;
import com.historical.voting.user.util.RedisKeyUtil;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Value("${app.upload.video-path}")
    private String videoUploadPath;

    @Value("${app.upload.thumbnail-path}")
    private String thumbnailPath;

    private final VideoMapper videoMapper;
    private final StringRedisTemplate redisTemplate;
    private final TranscodingStrategyFactory transcodingFactory;
    private final ExecutorService executor;

    private static final int THUMBNAIL_TIME_SEC = 0;

    public VideoServiceImpl(VideoMapper videoMapper, StringRedisTemplate redisTemplate,
                            TranscodingStrategyFactory transcodingFactory, ExecutorService executor) {
        this.videoMapper = videoMapper;
        this.redisTemplate = redisTemplate;
        this.transcodingFactory = transcodingFactory;
        this.executor = executor;
    }

    // === 视频上传处理 ===
    @Override
    public String processVideo(String inputPath) {
        String outputFileName = "processed_" + new File(inputPath).getName();
        String outputPath = Paths.get(videoUploadPath, outputFileName).toString();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", inputPath,
                    "-c:v", "libx264", "-crf", "23",
                    "-preset", "medium",
                    "-c:a", "aac", "-b:a", "128k",
                    "-movflags", "+faststart",
                    outputPath
            );

            if (pb.start().waitFor() != 0) {
                throw new FileProcessException("视频转码失败");
            }

            extractThumbnail(inputPath);
            return outputFileName;
        } catch (IOException | InterruptedException e) {
            throw new FileProcessException("视频处理失败", e);
        }
    }

    public void extractThumbnail(String videoPath) {
        String thumbnailFileName = new File(videoPath).getName().replaceFirst("[.][^.]+$", "") + "_thumb.jpg";
        String outputPath = Paths.get(thumbnailPath, thumbnailFileName).toString();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", videoPath,
                    "-vframes", "1", "-an", "-ss", String.valueOf(THUMBNAIL_TIME_SEC),
                    "-y", outputPath
            );

            if (pb.start().waitFor() != 0) {
                throw new FileProcessException("封面提取失败");
            }
        } catch (IOException | InterruptedException e) {
            throw new FileProcessException("封面提取失败", e);
        }
    }

    // === ABR 转码（HLS/DASH） ===
    @Override
    public void submitTranscoding(Long videoId, String filename) {
        String inputPath = Paths.get(videoUploadPath, filename).toString();

        for (String resolution : new String[]{"720p", "480p", "360p"}) {
            String outDir = String.format("%s/%d/%s/hls", thumbnailPath, videoId, resolution);
            executor.submit(() -> {
                try {
                    String scaledFile = thumbnailPath + "/tmp-" + videoId + "-" + resolution + ".mp4";
                    ProcessBuilder scalePb = new ProcessBuilder(
                            "ffmpeg", "-i", inputPath,
                            "-vf", "scale=-2:" + resolution.replace("p", ""),
                            "-c:v", "libx264", "-preset", "fast", "-crf", "23",
                            "-c:a", "aac", "-b:a", "128k",
                            "-y", scaledFile
                    );

                    if (scalePb.start().waitFor() != 0) {
                        throw new RuntimeException("分辨率压缩失败");
                    }

                    transcodingFactory.get("hls").transcode(scaledFile, outDir);
                } catch (Exception e) {
                    log.error("转码异常：{}", e.getMessage(), e);
                }
            });
        }
    }

    // === 播放记录 ===
    @Override
    @RateLimit(permitsPerSecond = 5, key = "#userId")
    public void recordView(Long videoId, String userIp) {
        String key = RedisKeyUtil.viewIpKey(videoId, userIp);
        if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "1", 1, TimeUnit.MINUTES))) {
            redisTemplate.opsForValue().increment(RedisKeyUtil.viewCountKey(videoId));
        }
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void syncViewCounts() {
        Set<String> keys = redisTemplate.keys(RedisKeyUtil.viewCountKey("*"));
        if (keys != null) {
            for (String key : keys) {
                Long videoId = RedisKeyUtil.extractId(key);
                int count = Integer.parseInt(redisTemplate.opsForValue().get(key));
                videoMapper.incrementViewCount(videoId, count);
                redisTemplate.delete(key);
            }
        }
    }

    // === 点赞踩 ===
    @Override
    public void likeVideo(Long videoId, String userId) {
        redisTemplate.opsForSet().remove(RedisKeyUtil.dislikeKey(videoId), userId);
        redisTemplate.opsForSet().add(RedisKeyUtil.likeKey(videoId), userId);
    }

    @Override
    public void dislikeVideo(Long videoId, String userId) {
        redisTemplate.opsForSet().remove(RedisKeyUtil.likeKey(videoId), userId);
        redisTemplate.opsForSet().add(RedisKeyUtil.dislikeKey(videoId), userId);
    }

    @Override
    public int getLikeCount(Long videoId) {
        return Math.toIntExact(redisTemplate.opsForSet().size(RedisKeyUtil.likeKey(videoId)));
    }

    @Override
    public int getDislikeCount(Long videoId) {
        return Math.toIntExact(redisTemplate.opsForSet().size(RedisKeyUtil.dislikeKey(videoId)));
    }

    // === 分享 & 收藏 ===
    @Override
    public void recordShare(Long videoId) {
        redisTemplate.opsForValue().increment(RedisKeyUtil.shareKey(videoId));
    }

    @Override
    public int getShareCount(Long videoId) {
        String val = redisTemplate.opsForValue().get(RedisKeyUtil.shareKey(videoId));
        return val == null ? 0 : Integer.parseInt(val);
    }

    // === 标签分页 ===
    @Override
    public Page<Video> pageByTags(int pageNum, int pageSize, String tagKeyword) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(tagKeyword)) {
            wrapper.like("tags", tagKeyword);
        }
        wrapper.orderByDesc("publish_time");
        return this.page(page, wrapper);
    }
}


