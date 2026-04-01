package com.petadopt.controller;

import com.petadopt.aspect.OperLog;
import com.petadopt.common.result.PageResult;
import com.petadopt.common.result.Result;
import com.petadopt.dto.KnowledgeDTO;
import com.petadopt.entity.Knowledge;
import com.petadopt.service.KnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/create")
    @OperLog(module = "知识库管理", action = "创建文章")
    public Result<Void> createKnowledge(@Valid @RequestBody KnowledgeDTO dto) {
        knowledgeService.createKnowledge(dto);
        return Result.success();
    }

    @PutMapping("/update")
    @OperLog(module = "知识库管理", action = "更新文章")
    public Result<Void> updateKnowledge(@Valid @RequestBody KnowledgeDTO dto) {
        knowledgeService.updateKnowledge(dto);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<Knowledge>> getKnowledgeList(
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(knowledgeService.getKnowledgeList(category, status, pageNum, pageSize));
    }

    @GetMapping("/detail/{id}")
    public Result<Knowledge> getKnowledgeDetail(@PathVariable Long id) {
        return Result.success(knowledgeService.getKnowledgeDetail(id));
    }

    @DeleteMapping("/delete/{id}")
    @OperLog(module = "知识库管理", action = "删除文章")
    public Result<Void> deleteKnowledge(@PathVariable Long id) {
        knowledgeService.deleteKnowledge(id);
        return Result.success();
    }
}
