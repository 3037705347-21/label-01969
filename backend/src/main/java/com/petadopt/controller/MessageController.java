package com.petadopt.controller;

import com.petadopt.common.result.PageResult;
import com.petadopt.common.result.Result;
import com.petadopt.entity.Message;
import com.petadopt.service.MessageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/list")
    public Result<PageResult<Message>> getMessageList(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(messageService.getMessageList(type, pageNum, pageSize));
    }

    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        messageService.markAllAsRead();
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(messageService.getUnreadCount());
    }
}
