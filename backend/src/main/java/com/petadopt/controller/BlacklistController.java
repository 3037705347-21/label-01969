package com.petadopt.controller;

import com.petadopt.aspect.OperLog;
import com.petadopt.common.result.PageResult;
import com.petadopt.common.result.Result;
import com.petadopt.entity.Blacklist;
import com.petadopt.service.BlacklistService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blacklist")
public class BlacklistController {

    private final BlacklistService blacklistService;

    public BlacklistController(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @PostMapping("/add")
    @OperLog(module = "黑名单管理", action = "添加黑名单")
    public Result<Void> addToBlacklist(
            @RequestParam Long userId,
            @RequestParam Integer type,
            @RequestParam String reason) {
        blacklistService.addToBlacklist(userId, type, reason);
        return Result.success();
    }

    @DeleteMapping("/remove/{id}")
    @OperLog(module = "黑名单管理", action = "移除黑名单")
    public Result<Void> removeFromBlacklist(@PathVariable Long id) {
        blacklistService.removeFromBlacklist(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<Blacklist>> getBlacklistPage(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(blacklistService.getBlacklistPage(type, pageNum, pageSize));
    }
}
