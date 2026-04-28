package com.campushub.common.audit;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SensitiveWordFilter {

    public List<String> findSensitiveWords(String content) {
        return Collections.emptyList();
    }

    public boolean containsSensitiveWord(String content) {
        return !findSensitiveWords(content).isEmpty();
    }
}