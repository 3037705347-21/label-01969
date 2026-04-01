package com.petadopt.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.common.result.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.allowed-types}")
    private String allowedTypes;

    @Value("${file.max-size}")
    private Long maxSize;

    private Path uploadDir;

    @PostConstruct
    public void init() {
        uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
            logger.info("上传目录初始化成功: {}", uploadDir);
        } catch (IOException e) {
            logger.error("创建上传目录失败: {}", uploadDir, e);
        }
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小不能超过10MB");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        
        if (suffix == null || suffix.isEmpty()) {
            throw new BusinessException("无法识别文件类型");
        }
        
        List<String> allowedList = Arrays.asList(allowedTypes.split(","));
        if (!allowedList.contains(suffix.toLowerCase())) {
            throw new BusinessException("不支持的文件类型: " + suffix);
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFileName = IdUtil.fastSimpleUUID() + "." + suffix;
        String relativePath = datePath + "/" + newFileName;

        try {
            Path targetDir = uploadDir.resolve(datePath);
            Files.createDirectories(targetDir);
            
            Path targetFile = targetDir.resolve(newFileName);
            
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            logger.info("文件上传成功: {}, 大小: {} bytes", relativePath, file.getSize());
            return Result.success("/uploads/" + relativePath);
        } catch (IOException e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
}
