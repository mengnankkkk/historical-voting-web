package com.historical.voting.user.controller;


import com.historical.voting.user.service.Impl.VideoProcessServiceImpl;
import com.historical.voting.user.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final VideoProcessServiceImpl videoProcessService;

    @Value("${app.upload.image-path}")
    private String imageUploadPath;

    @Value("${app.upload.video-path}")
    private String videoUploadPath;

    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        // 验证图片文件
        FileUtils.validateImageFile(file);
        
        // 保存图片文件
        String filename = FileUtils.saveFile(file, imageUploadPath);
        
        Map<String, String> response = new HashMap<>();
        response.put("filename", filename);
        response.put("url", "/images/" + filename);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/video")
    public ResponseEntity<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        // 验证视频文件
        FileUtils.validateVideoFile(file);
        
        // 保存原始视频文件
        String originalFilename = FileUtils.saveFile(file, videoUploadPath);
        String originalPath = Paths.get(videoUploadPath, originalFilename).toString();
        
        // 处理视频（转码和提取封面）
        String processedFilename = videoProcessService.processVideo(originalPath);
        
        Map<String, String> response = new HashMap<>();
        response.put("filename", processedFilename);
        response.put("url", "/videos/" + processedFilename);
        response.put("thumbnail", "/thumbnails/" + processedFilename.replaceFirst("[.][^.]+$", "") + "_thumb.jpg");
        
        return ResponseEntity.ok(response);
    }
} 