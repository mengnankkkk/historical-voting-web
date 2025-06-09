package com.historical.voting.user.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.historical.voting.user.entity.ResponseResult;
import com.historical.voting.user.entity.Video;
import com.historical.voting.user.service.Impl.VideoProcessServiceImpl;
import com.historical.voting.user.service.Impl.VideoServiceImpl;
import com.historical.voting.user.service.VideoService;
import com.historical.voting.user.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /**
     * 上传视频
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public ResponseResult<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            String originName = file.getOriginalFilename();
            String targetPath = Paths.get("upload/video/", originName).toString();
            File dest = new File(targetPath);
            file.transferTo(dest);

            String processedName = videoService.processVideo(targetPath);

            // 示例中 videoId 假设为数据库插入后返回的主键
            Long videoId = 123L; // 实际应由 DB 插入后返回
            videoService.submitTranscoding(videoId, processedName);

            return ResponseResult.success("上传成功，视频转码中：" + processedName);
        } catch (IOException e) {
            return ResponseResult.failure("视频上传失败");
        }
    }

    /**
     * 播放记录
     * @param videoId
     * @param request
     * @return
     */

    @PostMapping("/{videoId}/play")
    public ResponseResult<Void> playVideo(@PathVariable Long videoId,
                                          HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        videoService.recordView(videoId, ip);
        return ResponseResult.success();
    }

    /**
     * 点赞
     * @param videoId
     * @param userId
     * @return
     */
    @PostMapping("/{videoId}/like")
    public ResponseResult<Void> like(@PathVariable Long videoId,
                                     @RequestParam String userId) {
        videoService.likeVideo(videoId, userId);
        return ResponseResult.success();
    }

    /**
     * 点踩
     * @param videoId
     * @param userId
     * @return
     */
    @PostMapping("/{videoId}/dislike")
    public ResponseResult<Void> dislike(@PathVariable Long videoId,
                                        @RequestParam String userId) {
        videoService.dislikeVideo(videoId, userId);
        return ResponseResult.success();
    }

    /**
     * 分享
     * @param videoId
     * @return
     */
    @PostMapping("/{videoId}/share")
    public ResponseResult<Void> share(@PathVariable Long videoId) {
        videoService.recordShare(videoId);
        return ResponseResult.success();
    }

    /**
     * 获取点赞和点踩
     * @param videoId
     * @return
     */
    @GetMapping("/{videoId}/stats")
    public ResponseResult<Map<String, Integer>> getStats(@PathVariable Long videoId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("likes", videoService.getLikeCount(videoId));
        stats.put("dislikes", videoService.getDislikeCount(videoId));
        stats.put("shares", videoService.getShareCount(videoId));
        return ResponseResult.success(stats);
    }

    /**
     * 根据标签分页查询
     * @param pageNum
     * @param pageSize
     * @param keyword
     * @return
     */
    @GetMapping("/tags")
    public ResponseResult<Page<Video>> pageByTags(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {

        Page<Video> result = videoService.pageByTags(pageNum, pageSize, keyword);
        return ResponseResult.success(result);
    }


} 