package com.campushub.user.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class UserMapperXmlContractTest {

    @Test
    void userQueriesAlwaysFilterLogicalDeletedRows() throws IOException {
        String xml = resourceText("mapper/user/UserMapper.xml");

        assertTrue(xml.contains("id=\"selectactivebyid\""));
        assertTrue(xml.contains("id=\"selectactivebystudentid\""));
        assertTrue(xml.contains("id=\"existsbystudentid\""));
        assertEquals(4, countOccurrences(xml, "is_deleted = false"));
    }

    @Test
    void verificationQueriesDoNotAssumeLogicalDeleteColumn() throws IOException {
        String xml = resourceText("mapper/user/UserVerificationMapper.xml");

        assertFalse(xml.contains("is_deleted"));
        assertTrue(xml.contains("order by create_time desc, id desc"));
        assertTrue(xml.contains("order by create_time asc, id asc"));
    }

    private static String resourceText(String path) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(path)) {
            assertNotNull(inputStream, path + " should be packaged as a test resource");
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("\\s+", " ")
                    .toLowerCase(Locale.ROOT);
        }
    }

    private static int countOccurrences(String value, String needle) {
        int count = 0;
        int index = value.indexOf(needle);
        while (index >= 0) {
            count++;
            index = value.indexOf(needle, index + needle.length());
        }
        return count;
    }
}
