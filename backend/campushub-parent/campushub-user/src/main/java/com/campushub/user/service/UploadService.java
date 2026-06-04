package com.campushub.user.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.user.dto.UploadResultDTO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path uploadBasePath;
    private final String publicPrefix;
    private final long avatarMaxSize;
    private final long studentCardMaxSize;

    public UploadService(
            @Value("${campushub.file.upload-base-path:./uploads}") String uploadBasePath,
            @Value("${campushub.file.public-prefix:/uploads}") String publicPrefix,
            @Value("${campushub.file.avatar-max-size:5242880}") long avatarMaxSize,
            @Value("${campushub.file.student-card-max-size:5242880}") long studentCardMaxSize
    ) {
        this.uploadBasePath = Paths.get(uploadBasePath).toAbsolutePath().normalize();
        this.publicPrefix = normalizePublicPrefix(publicPrefix);
        this.avatarMaxSize = avatarMaxSize;
        this.studentCardMaxSize = studentCardMaxSize;
        log.info("Upload base path resolved to: {}", this.uploadBasePath);
    }

    public UploadResultDTO uploadAvatar(Long userId, MultipartFile file) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED_MESSAGE);
        }
        return saveImage(file, "avatars", String.valueOf(userId), avatarMaxSize);
    }

    public UploadResultDTO uploadStudentCard(String studentId, MultipartFile file) {
        String directoryKey = sanitizeDirectoryKey(studentId);
        return saveImage(file, "student-cards", directoryKey, studentCardMaxSize);
    }

    public UploadResultDTO uploadTaskImage(Long userId, MultipartFile file) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED_MESSAGE);
        }
        return saveImage(file, "task-images", String.valueOf(userId), avatarMaxSize);
    }

    private UploadResultDTO saveImage(MultipartFile file, String category, String ownerKey, long maxSize) {
        validateImage(file, maxSize);

        String contentType = normalizeContentType(file.getContentType());
        String extension = EXTENSIONS.get(contentType);
        String filename = UUID.randomUUID() + extension;
        Path targetDirectory = uploadBasePath.resolve(category).resolve(ownerKey).normalize();
        Path targetFile = targetDirectory.resolve(filename).normalize();

        if (!targetFile.startsWith(uploadBasePath)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上传路径不合法");
        }

        try {
            Files.createDirectories(targetDirectory);
            // 使用 transferTo 替代 Files.copy，由 Servlet 容器的 Part.write()
            // 保证文件完整写入，避免 Linux 上 MultipartFile 输入流的潜在问题
            file.transferTo(targetFile.toFile());
            log.info("File saved: {} ({} bytes)", targetFile, file.getSize());
        } catch (IOException ex) {
            log.error("Failed to save file to {}: {}", targetFile, ex.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "文件保存失败");
        }

        String fileUrl = publicPrefix + "/" + category + "/" + ownerKey + "/" + filename;
        log.debug("File URL: {}", fileUrl);
        return new UploadResultDTO(fileUrl, file.getOriginalFilename(), contentType, file.getSize());
    }

    private static void validateImage(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要上传的图片");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片大小超过限制");
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持 JPG、PNG、WebP 图片");
        }
    }

    private static String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    }

    private static String sanitizeDirectoryKey(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上传目录标识不能为空");
        }
        String sanitized = value.trim().replaceAll("[^A-Za-z0-9._-]", "_");
        if (sanitized.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上传目录标识不合法");
        }
        return sanitized;
    }

    private static String normalizePublicPrefix(String value) {
        String prefix = value == null || value.isBlank() ? "/uploads" : value.trim();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        while (prefix.endsWith("/") && prefix.length() > 1) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }
}
