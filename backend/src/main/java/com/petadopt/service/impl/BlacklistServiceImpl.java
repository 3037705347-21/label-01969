package com.petadopt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.common.result.PageResult;
import com.petadopt.entity.Blacklist;
import com.petadopt.entity.User;
import com.petadopt.mapper.BlacklistMapper;
import com.petadopt.mapper.UserMapper;
import com.petadopt.service.BlacklistService;
import com.petadopt.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
public class BlacklistServiceImpl extends ServiceImpl<BlacklistMapper, Blacklist> implements BlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistServiceImpl.class);
    private final UserMapper userMapper;

    public BlacklistServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public void addToBlacklist(Long userId, Integer type, String reason) {
        Long operatorId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            logger.error("添加黑名单失败-用户不存在: operatorId={}, targetUserId={}", operatorId, userId);
            throw new BusinessException("用户不存在");
        }

        Long count = lambdaQuery()
                .eq(Blacklist::getUserId, userId)
                .count();
        if (count > 0) {
            logger.error("添加黑名单失败-用户已在黑名单: operatorId={}, targetUserId={}", operatorId, userId);
            throw new BusinessException("该用户已在黑名单中");
        }

        Blacklist blacklist = new Blacklist();
        blacklist.setUserId(userId);
        blacklist.setType(type);
        blacklist.setReason(reason);
        blacklist.setOperatorId(operatorId);
        blacklist.setCreateTime(LocalDateTime.now());
        save(blacklist);

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setStatus(2);
        userMapper.updateById(updateUser);

        logger.info("添加黑名单成功: operatorId={}, targetUserId={}, username={}, type={}, reason={}",
                operatorId, userId, user.getUsername(), type, reason);
    }

    @Override
    @Transactional
    public void removeFromBlacklist(Long id) {
        Long operatorId = UserContext.getUserId();
        Blacklist blacklist = getById(id);
        if (blacklist == null) {
            logger.error("移除黑名单失败-记录不存在: operatorId={}, blacklistId={}", operatorId, id);
            throw new BusinessException("黑名单记录不存在");
        }

        removeById(id);

        User updateUser = new User();
        updateUser.setId(blacklist.getUserId());
        updateUser.setStatus(1);
        userMapper.updateById(updateUser);

        logger.info("移除黑名单成功: operatorId={}, targetUserId={}, blacklistId={}", operatorId, blacklist.getUserId(), id);
    }

    @Override
    public PageResult<Blacklist> getBlacklistPage(Integer type, Integer pageNum, Integer pageSize) {
        Page<Blacklist> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(Blacklist::getType, type);
        }
        wrapper.orderByDesc(Blacklist::getCreateTime);

        Page<Blacklist> result = page(page, wrapper);
        logger.debug("查询黑名单列表: type={}, total={}", type, result.getTotal());
        return PageResult.of(result);
    }
}
