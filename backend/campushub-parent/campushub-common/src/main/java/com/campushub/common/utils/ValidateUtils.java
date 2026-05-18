package com.campushub.common.utils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.regex.Pattern;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;

public final class ValidateUtils {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 50;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{4,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^\\S{8,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^\\d{4,32}$");

    private ValidateUtils() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static boolean isEmpty(Collection<?> values) {
        return values == null || values.isEmpty();
    }

    public static boolean isUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isStudentId(String studentId) {
        return studentId != null && STUDENT_ID_PATTERN.matcher(studentId).matches();
    }

    public static boolean isPositive(Long value) {
        return value != null && value > 0;
    }

    public static boolean isPositive(Integer value) {
        return value != null && value > 0;
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isValidPage(int page) {
        return page >= DEFAULT_PAGE;
    }

    public static boolean isValidPageSize(int size) {
        return size >= 1 && size <= MAX_PAGE_SIZE;
    }

    public static boolean isUrl(String value) {
        if (isBlank(value)) {
            return false;
        }
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && isNotBlank(uri.getHost());
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    public static void requireNotBlank(String value, String fieldName) {
        if (isBlank(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + "不能为空");
        }
    }

    public static void requirePositive(Long value, String fieldName) {
        if (!isPositive(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + "必须为正数");
        }
    }

    public static int normalizePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        if (!isValidPage(page)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "page 必须大于等于 1");
        }
        return page;
    }

    public static int normalizePageSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (!isValidPageSize(size)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "size 必须在 1 到 50 之间");
        }
        return size;
    }
}
