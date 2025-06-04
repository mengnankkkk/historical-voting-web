package com.historical.voting.user.util;

import com.historical.voting.user.exception.FileProcessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public class FileUtils {

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList("video/mp4", "video/avi", "video/quicktime", "video/x-msvideo");

    public static void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileProcessException("文件为空");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new FileProcessException("图片大小不能超过10MB");
        }

        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new FileProcessException("不支持的图片格式");
        }

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new FileProcessException("无效的图片文件");
            }
        } catch (IOException e) {
            throw new FileProcessException("图片处理失败");
        }
    }

    public static void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileProcessException("文件为空");
        }

        if (!ALLOWED_VIDEO_TYPES.contains(file.getContentType())) {
            throw new FileProcessException("不支持的视频格式");
        }
    }

    public static String generateUniqueFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }

    public static String saveFile(MultipartFile file, String directory) throws IOException {
        String filename = generateUniqueFileName(file.getOriginalFilename());
        Path uploadPath = Paths.get(directory);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());
        return filename;
    }
} 