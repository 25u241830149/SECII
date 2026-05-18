package com.campushub.common.audit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;

@Component
@ConfigurationProperties(prefix = "campushub.audit.sensitive-word")
public class SensitiveWordFilter {

    private boolean enabled = true;

    private String replacement = "***";

    private Set<String> words = new LinkedHashSet<>();

    private final Set<String> normalizedWords = new CopyOnWriteArraySet<>();

    public boolean containsSensitiveWord(String content) {
        return !findSensitiveWords(content).isEmpty();
    }

    public List<String> findSensitiveWords(String content) {
        if (!enabled || content == null || content.isBlank() || normalizedWords.isEmpty()) {
            return List.of();
        }

        String normalizedContent = normalize(content);
        Set<String> matchedWords = new LinkedHashSet<>();
        for (String word : normalizedWords) {
            if (normalizedContent.contains(word)) {
                matchedWords.add(word);
            }
        }
        return List.copyOf(matchedWords);
    }

    public String filter(String content) {
        if (!enabled || content == null || content.isBlank() || normalizedWords.isEmpty()) {
            return content;
        }

        String filtered = content;
        for (String word : normalizedWords) {
            filtered = filtered.replaceAll("(?iu)" + java.util.regex.Pattern.quote(word), replacement);
        }
        return filtered;
    }

    public void validate(String content) {
        List<String> matchedWords = findSensitiveWords(content);
        if (!matchedWords.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "内容包含违规词汇，请修改后再提交");
        }
    }

    public void addWord(String word) {
        String normalizedWord = normalize(word);
        if (!normalizedWord.isBlank()) {
            words.add(word);
            normalizedWords.add(normalizedWord);
        }
    }

    public void addWords(Collection<String> words) {
        if (words == null) {
            return;
        }
        words.forEach(this::addWord);
    }

    public Set<String> getWords() {
        return Collections.unmodifiableSet(words);
    }

    public void setWords(Set<String> words) {
        this.words = words == null ? new LinkedHashSet<>() : new LinkedHashSet<>(words);
        rebuildNormalizedWords();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement == null ? "***" : replacement;
    }

    private void rebuildNormalizedWords() {
        normalizedWords.clear();
        List<String> sortedWords = new ArrayList<>(words);
        sortedWords.sort((left, right) -> Integer.compare(right.length(), left.length()));
        for (String word : sortedWords) {
            String normalizedWord = normalize(word);
            if (!normalizedWord.isBlank()) {
                normalizedWords.add(normalizedWord);
            }
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
