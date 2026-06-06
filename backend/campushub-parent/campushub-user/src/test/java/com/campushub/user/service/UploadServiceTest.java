package com.campushub.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.user.dto.UploadResultDTO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class UploadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadAvatarStoresImageAndReturnsPublicUrl() throws IOException {
        UploadService uploadService = new UploadService(tempDir.toString(), "/uploads", 1024, 1024);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        UploadResultDTO result = uploadService.uploadAvatar(7L, file);

        assertTrue(result.fileUrl().startsWith("/uploads/avatars/7/"));
        assertTrue(result.fileUrl().endsWith(".png"));
        assertEquals("avatar.png", result.originalFilename());
        assertEquals("image/png", result.contentType());
        assertEquals(3L, result.size());
        Path savedFile = tempDir.resolve(result.fileUrl().replaceFirst("^/uploads/", ""));
        assertTrue(Files.exists(savedFile));
    }

    @Test
    void uploadStudentCardSanitizesStudentIdDirectory() throws IOException {
        UploadService uploadService = new UploadService(tempDir.toString(), "/uploads", 1024, 1024);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "card.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        UploadResultDTO result = uploadService.uploadStudentCard("2026/0001", file);

        assertTrue(result.fileUrl().startsWith("/uploads/student-cards/2026_0001/"));
        Path savedFile = tempDir.resolve(result.fileUrl().replaceFirst("^/uploads/", ""));
        assertTrue(Files.exists(savedFile));
    }

    @Test
    void uploadRejectsUnsupportedContentType() {
        UploadService uploadService = new UploadService(tempDir.toString(), "/uploads", 1024, 1024);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "evil.exe",
                "application/octet-stream",
                new byte[]{1, 2, 3}
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> uploadService.uploadAvatar(7L, file));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
    }

    @Test
    void uploadRejectsOversizedFile() {
        UploadService uploadService = new UploadService(tempDir.toString(), "/uploads", 2, 1024);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.webp",
                "image/webp",
                new byte[]{1, 2, 3}
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> uploadService.uploadAvatar(7L, file));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
    }

    @Test
    void uploadIgnoresPathTraversalInOriginalFilename() throws IOException {
        UploadService uploadService = new UploadService(tempDir.toString(), "/uploads", 1024, 1024);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../etc/passwd.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        UploadResultDTO result = uploadService.uploadAvatar(7L, file);

        Path savedFile = tempDir.resolve(result.fileUrl().replaceFirst("^/uploads/", ""));
        assertTrue(savedFile.normalize().startsWith(tempDir.normalize()));
        assertTrue(Files.exists(savedFile));
        assertEquals("../../etc/passwd.png", result.originalFilename());
    }
}
