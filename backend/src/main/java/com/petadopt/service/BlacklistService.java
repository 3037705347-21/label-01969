package com.petadopt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petadopt.common.result.PageResult;
import com.petadopt.entity.Blacklist;

public interface BlacklistService extends IService<Blacklist> {
    void addToBlacklist(Long userId, Integer type, String reason);
    void removeFromBlacklist(Long id);
    PageResult<Blacklist> getBlacklistPage(Integer type, Integer pageNum, Integer pageSize);
}
