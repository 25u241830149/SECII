package com.campushub.message.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campushub.common.response.PageResponse;
import com.campushub.common.testing.ContainerizedIntegrationTestSupport;
import com.campushub.common.testing.DatabaseFixtureHelper;
import com.campushub.message.dto.MessageDTO;
import com.campushub.message.dto.NoticeCreateRequest;
import com.campushub.message.dto.NoticeDTO;
import com.campushub.message.dto.UnreadCountDTO;
import com.campushub.message.mapper.MessageMapper;
import com.campushub.message.mapper.NoticeMapper;
import com.campushub.message.service.MessageService;
import com.campushub.message.service.NoticeService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(classes = MessageFlowIntegrationTest.TestApplication.class)
@ActiveProfiles("test")
class MessageFlowIntegrationTest extends ContainerizedIntegrationTestSupport {

    @Autowired
    private MessageService messageService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registerContainerProperties(registry);
        registerSchemaProperties(registry);
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM t_message");
        jdbcTemplate.execute("DELETE FROM t_notice");
        jdbcTemplate.execute("DELETE FROM u_user WHERE student_id <> 'admin'");
    }

    @Test
    void notifyMarkReadAndDeleteFlowPersists() {
        Long receiverId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "20261001", "Receiver");

        messageService.notifyUser(receiverId, 0, "  Welcome  ", "  Hello there  ");

        UnreadCountDTO unread = messageService.unreadCount(receiverId);
        PageResponse<MessageDTO> page = messageService.list(receiverId, "SYSTEM", true, 1, 10);

        assertEquals(1L, unread.count());
        assertEquals(1L, page.total());
        assertEquals("Welcome", page.records().get(0).title());
        assertEquals("Hello there", page.records().get(0).content());

        Long messageId = page.records().get(0).messageId();
        messageService.markRead(messageId, receiverId);
        assertEquals(0L, messageService.unreadCount(receiverId).count());

        messageService.delete(messageId, receiverId);
        assertEquals(0L, messageService.list(receiverId, null, null, 1, 10).total());
    }

    @Test
    void createUpdateAndDeleteNoticeFlowPersists() {
        Long adminId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "20261002", "Admin", 1, 0, 95);

        NoticeDTO created = noticeService.create(adminId, new NoticeCreateRequest("  Launch  ", "  CampusHub live  "));

        assertNotNull(created);
        assertEquals("Launch", created.title());

        NoticeDTO updated = noticeService.update(created.noticeId(), new NoticeCreateRequest("Updated", "Body"));
        List<NoticeDTO> latest = noticeService.latest(5);

        assertEquals("Updated", updated.title());
        assertEquals(1, latest.size());
        assertEquals("Updated", latest.get(0).title());

        noticeService.delete(created.noticeId());
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM t_notice", Long.class);
        assertEquals(0L, count);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({MessageService.class, NoticeService.class})
    @MapperScan(basePackageClasses = {MessageMapper.class, NoticeMapper.class})
    static class TestApplication {
    }
}
