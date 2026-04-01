package com.petadopt.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.common.result.PageResult;
import com.petadopt.dto.KnowledgeDTO;
import com.petadopt.entity.Knowledge;
import com.petadopt.mapper.KnowledgeMapper;
import com.petadopt.service.KnowledgeService;
import com.petadopt.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class KnowledgeServiceImpl extends ServiceImpl<KnowledgeMapper, Knowledge> implements KnowledgeService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeServiceImpl.class);

    @Override
    public void createKnowledge(KnowledgeDTO dto) {
        Long userId = UserContext.getUserId();
        Knowledge knowledge = new Knowledge();
        BeanUtil.copyProperties(dto, knowledge);
        knowledge.setAuthorId(userId);
        knowledge.setViewCount(0);
        knowledge.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        save(knowledge);
        
        logger.info("创建知识库文章成功: userId={}, articleId={}, title={}, category={}", userId, knowledge.getId(), knowledge.getTitle(), knowledge.getCategory());
    }

    @Override
    public void updateKnowledge(KnowledgeDTO dto) {
        Long userId = UserContext.getUserId();
        if (dto.getId() == null) {
            logger.error("更新知识库文章失败-ID为空: userId={}", userId);
            throw new BusinessException("文章ID不能为空");
        }
        
        Knowledge exist = getById(dto.getId());
        if (exist == null) {
            logger.error("更新知识库文章失败-文章不存在: userId={}, articleId={}", userId, dto.getId());
            throw new BusinessException("文章不存在");
        }
        
        Knowledge knowledge = new Knowledge();
        BeanUtil.copyProperties(dto, knowledge);
        updateById(knowledge);
        
        logger.info("更新知识库文章成功: userId={}, articleId={}, title={}", userId, dto.getId(), dto.getTitle());
    }

    @Override
    public Knowledge getKnowledgeDetail(Long id) {
        Knowledge knowledge = getById(id);
        if (knowledge == null) {
            logger.warn("获取知识库文章详情失败-文章不存在: articleId={}", id);
            throw new BusinessException("文章不存在");
        }
        
        lambdaUpdate()
                .eq(Knowledge::getId, id)
                .setSql("view_count = view_count + 1")
                .update();
        
        return knowledge;
    }

    @Override
    public PageResult<Knowledge> getKnowledgeList(Integer category, Integer status, Integer pageNum, Integer pageSize) {
        Page<Knowledge> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Knowledge> wrapper = new LambdaQueryWrapper<>();
        
        if (category != null) {
            wrapper.eq(Knowledge::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Knowledge::getStatus, status);
        } else {
            wrapper.eq(Knowledge::getStatus, 1);
        }
        wrapper.orderByDesc(Knowledge::getCreateTime);
        
        Page<Knowledge> result = page(page, wrapper);
        logger.debug("查询知识库文章列表: category={}, status={}, total={}", category, status, result.getTotal());
        return PageResult.of(result);
    }

    @Override
    public void deleteKnowledge(Long id) {
        Long userId = UserContext.getUserId();
        Knowledge knowledge = getById(id);
        if (knowledge == null) {
            logger.error("删除知识库文章失败-文章不存在: userId={}, articleId={}", userId, id);
            throw new BusinessException("文章不存在");
        }
        
        removeById(id);
        logger.info("删除知识库文章成功: userId={}, articleId={}, title={}", userId, id, knowledge.getTitle());
    }
}
