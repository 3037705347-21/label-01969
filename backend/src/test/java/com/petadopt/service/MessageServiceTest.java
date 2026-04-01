package com.petadopt.service;

import com.petadopt.BaseTest;
import com.petadopt.entity.Message;
import com.petadopt.mapper.MessageMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("消息模块测试")
public class MessageServiceTest extends BaseTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageMapper messageMapper;

    private List<Long> testMessageIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        testMessageIds.forEach(id -> messageMapper.deleteById(id));
        testMessageIds.clear();
    }

    @Test
    @DisplayName("发送消息成功")
    void testSendMessage_Success() {
        assertDoesNotThrow(() ->
                messageService.sendMessage(ADOPTER_USER_ID, "测试消息标题", "测试消息内容", 1));

        assertNotNull(message);
        assertEquals(0, message.getIsRead());
        assertEquals(1, message.getType());

        testMessageIds.add(message.getId());
    }

    @Test
    @DisplayName("获取消息列表")
    void testGetMessageList() {
        messageService.sendMessage(ADMIN_USER_ID, "列表测试", "内容", 1);

        testMessageIds.add(message.getId());

        setCustomContext(ADMIN_USER_ID, "admin", 1);
        var result = messageService.getMessageList(1, 10);
//        assertNotNull(result);
//        assertTrue(result.getTotal() >= 1);
    }

    @Test
    @DisplayName("标记消息已读")
    void testMarkRead() {
        messageService.sendMessage(ADOPTER_USER_ID, "已读测试", "内容", 2);

        testMessageIds.add(message.getId());

        setAdopterContext();
        assertDoesNotThrow(() -> messageService.markRead(message.getId()));

        Message updated = messageMapper.selectById(message.getId());
        assertEquals(1, updated.getIsRead());
    }

    @Test
    @DisplayName("标记全部已读")
    void testMarkAllRead() {
        messageService.sendMessage(RESCUER_USER_ID, "全部已读1", "内容", 1);
        messageService.sendMessage(RESCUER_USER_ID, "全部已读2", "内容", 2);

        setRescuerContext();
        assertDoesNotThrow(() -> messageService.markAllRead());
    }

    @Test
    @DisplayName("获取未读消息数量")
    void testGetUnreadCount() {
        messageService.sendMessage(ADOPTER_USER_ID, "未读测试", "内容", 3);

        testMessageIds.add(message.getId());

        setAdopterContext();
        var count = messageService.getUnreadCount();
        assertTrue(count >= 1);
    }
}
