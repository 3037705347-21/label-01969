package com.petadopt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petadopt.common.result.PageResult;
import com.petadopt.entity.Message;

public interface MessageService extends IService<Message> {
    PageResult<Message> getMessageList(Integer type, Integer pageNum, Integer pageSize);
    void markAsRead(Long id);
    void markAllAsRead();
    Long getUnreadCount();
    void sendMessage(Long userId, String title, String content, Integer type);
}
