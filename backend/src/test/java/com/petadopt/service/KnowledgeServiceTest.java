package com.petadopt.service;

import com.petadopt.BaseTest;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.dto.KnowledgeDTO;
import com.petadopt.entity.Knowledge;
import com.petadopt.mapper.KnowledgeMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("知识库模块测试")
public class KnowledgeServiceTest extends BaseTest {

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    private List<Long> testKnowledgeIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        testKnowledgeIds.forEach(id -> knowledgeMapper.deleteById(id));
        testKnowledgeIds.clear();
    }

    @Test
    @DisplayName("创建知识库文章成功")
    void testCreateKnowledge_Success() {
        setAdminContext();
        KnowledgeDTO dto = new KnowledgeDTO();
        dto.setTitle("猫咪日常护理指南");
        dto.setContent("猫咪需要定期梳毛、剪指甲、清洁耳朵...");
        dto.setCategory(1);
        dto.setStatus(1);

        assertDoesNotThrow(() -> knowledgeService.createKnowledge(dto));

        assertNotNull(article);
        assertEquals(1, article.getCategory());
        assertEquals(ADMIN_USER_ID, article.getAuthorId());

        testKnowledgeIds.add(article.getId());
    }

    @Test
    @DisplayName("更新知识库文章成功")
    void testUpdateKnowledge_Success() {
        setAdminContext();
        KnowledgeDTO createDTO = new KnowledgeDTO();
        createDTO.setTitle("测试文章");
        createDTO.setContent("测试内容");
        createDTO.setCategory(2);
        createDTO.setStatus(0);
        knowledgeService.createKnowledge(createDTO);

        testKnowledgeIds.add(article.getId());

        KnowledgeDTO updateDTO = new KnowledgeDTO();
        updateDTO.setId(article.getId());
        updateDTO.setTitle("更新后的标题");
        updateDTO.setContent("更新后的内容");

        assertDoesNotThrow(() -> knowledgeService.updateKnowledge(updateDTO));

        Knowledge updated = knowledgeMapper.selectById(article.getId());
        assertEquals("更新后的标题", updated.getTitle());
        assertEquals("更新后的内容", updated.getContent());
    }

    @Test
    @DisplayName("获取知识库文章列表")
    void testGetKnowledgeList() {
        var result = knowledgeService.getKnowledgeList(1, null, 1, 10);
//        assertNotNull(result);
//        assertTrue(result.getTotal() >= 0);
    }

    @Test
    @DisplayName("获取文章详情成功")
    void testGetKnowledgeDetail_Success() {
        setAdminContext();
        KnowledgeDTO dto = new KnowledgeDTO();
        dto.setTitle("详情测试");
        dto.setContent("详情内容");
        dto.setCategory(3);
        dto.setStatus(1);
        knowledgeService.createKnowledge(dto);

        testKnowledgeIds.add(article.getId());

        var result = knowledgeService.getKnowledgeDetail(article.getId());
//        assertNotNull(result);
        assertEquals("详情测试", result.getTitle());

        Knowledge viewed = knowledgeMapper.selectById(article.getId());
        assertTrue(viewed.getViewCount() >= 1);
    }

    @Test
    @DisplayName("获取文章详情失败-不存在")
    void testGetKnowledgeDetail_Fail_NotExist() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> knowledgeService.getKnowledgeDetail(999999L));
        assertEquals("文章不存在", exception.getMessage());
    }

    @Test
    @DisplayName("删除文章成功")
    void testDeleteKnowledge_Success() {
        setAdminContext();
        KnowledgeDTO dto = new KnowledgeDTO();
        dto.setTitle("删除测试");
        dto.setContent("删除内容");
        dto.setCategory(1);
        dto.setStatus(1);
        knowledgeService.createKnowledge(dto);


        assertDoesNotThrow(() -> knowledgeService.deleteKnowledge(article.getId()));
        assertNull(knowledgeMapper.selectById(article.getId()));
    }
}
