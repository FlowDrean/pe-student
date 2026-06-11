package com.student.controller;

import com.student.exception.ErrorCode;
import com.student.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    @Value("${app.upload.dir:${user.home}/.uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        validateFile(file);

        String url = saveFile(file);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return Result.ok(result);
    }

    @PostMapping("/images")
    public Result<List<String>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        if (files.size() > 9) {
            return Result.badRequest("最多上传9张图片");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file);
            urls.add(saveFile(file));
        }
        return Result.ok(urls);
    }

    /**
     * 保存文件到本地磁盘，返回访问 URL
     */
    private String saveFile(MultipartFile file) {
        try {
            // 按日期分目录: uploads/2026/06/07/uuid.ext
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path dir = Paths.get(uploadDir, datePath);
            Files.createDirectories(dir);

            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + ext;

            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());

            // 返回相对路径，前端通过 /uploads/** 映射访问
            String url = "/uploads/" + datePath + "/" + filename;
            log.info("文件上传成功: {} -> {}", originalName, url);
            return url;
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new com.student.exception.BusinessException("文件上传失败，请重试");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new com.student.exception.BusinessException("文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new com.student.exception.BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new com.student.exception.BusinessException(ErrorCode.FILE_TYPE_ERROR);
        }
    }
}
