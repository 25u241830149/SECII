package com.campushub.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentAuditService {

    private final SensitiveWordFilter sensitiveWordFilter;

    public boolean auditText(String content) {
        return !sensitiveWordFilter.containsSensitiveWord(content);
    }
}