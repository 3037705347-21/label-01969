package com.petadopt.controller;

import com.petadopt.common.result.Result;
import com.petadopt.service.MatchService;
import com.petadopt.vo.AdopterMatchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 领养匹配控制器
 */
@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    /**
     * 获取宠物的推荐领养人列表
     * @param petId 宠物ID
     * @param limit 返回数量限制（默认10）
     */
    @GetMapping("/adopters/{petId}")
    public Result<List<AdopterMatchVO>> getMatchedAdopters(
            @PathVariable Long petId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(matchService.matchAdoptersForPet(petId, limit));
    }
}
