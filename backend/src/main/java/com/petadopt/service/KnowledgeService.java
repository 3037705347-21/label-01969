package com.petadopt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petadopt.common.result.PageResult;
import com.petadopt.dto.KnowledgeDTO;
import com.petadopt.entity.Knowledge;

public interface KnowledgeService extends IService<Knowledge> {
    void createKnowledge(KnowledgeDTO dto);
    void updateKnowledge(KnowledgeDTO dto);
    Knowledge getKnowledgeDetail(Long id);
    PageResult<Knowledge> getKnowledgeList(Integer category, Integer status, Integer pageNum, Integer pageSize);
    void deleteKnowledge(Long id);
}
