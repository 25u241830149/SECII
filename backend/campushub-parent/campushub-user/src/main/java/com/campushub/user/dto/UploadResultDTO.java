package com.campushub.user.dto;

public record UploadResultDTO(
        String fileUrl,
        String originalFilename,
        String contentType,
        long size
) {
}
