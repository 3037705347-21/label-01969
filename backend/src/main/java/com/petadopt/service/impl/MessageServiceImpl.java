package com.petadopt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadopt.common.result.PageResult;
import com.petadopt.entity.Message;
import com.petadopt.mapper.MessageMapper;
import com.petadopt.service.MessageService;
import com.petadopt.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Override
    public PageResult<Message> getMessageList(Integer type, Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        if (type != null) {
            wrapper.eq(Message::getType, type);
        }
        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> result = page(page, wrapper);
        logger.debug("查询消息列表: userId={}, type={}, total={}", userId, type, result.getTotal());
        return PageResult.of(result);
    }

    @Override
    public void markAsRead(Long id) {
        Long userId = UserContext.getUserId();
        Message message = getById(id);
        if (message != null && message.getUserId().equals(userId)) {
            Message update = new Message();
            update.setId(id);
            update.setIsRead(1);
            updateById(update);
            logger.debug("标记消息已读: userId={}, messageId={}", userId, id);
        }
    }

    @Override
    public void markAllAsRead() {
        Long userId = UserContext.getUserId();
        long count = lambdaQuery()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .count();
        
        lambdaUpdate()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1)
                .update();
        
        logger.info("标记全部消息已读: userId={}, count={}", userId, count);
    }

    @Override
    public Long getUnreadCount() {
        return lambdaQuery()
                .eq(Message::getUserId, UserContext.getUserId())
                .eq(Message::getIsRead, 0)
                .count();
    }

    @Override
    public void sendMessage(Long userId, String title, String content, Integer type) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        save(message);
        
        logger.info("发送消息成功: targetUserId={}, messageId={}, title={}, type={}", userId, message.getId(), title, type);
    }
}
