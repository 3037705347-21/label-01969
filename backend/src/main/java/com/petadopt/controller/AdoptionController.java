package com.petadopt.controller;

import com.petadopt.aspect.OperLog;
import com.petadopt.common.result.PageResult;
import com.petadopt.common.result.Result;
import com.petadopt.dto.AdoptionApplyDTO;
import com.petadopt.dto.ReviewDTO;
import com.petadopt.service.AdoptionService;
import com.petadopt.vo.AdoptionApplicationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adoption")
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionService adoptionService;

    @PostMapping("/apply")
    @OperLog(module = "领养管理", action = "提交领养申请")
    public Result<Void> apply(@Valid @RequestBody AdoptionApplyDTO dto) {
        adoptionService.apply(dto);
        return Result.success();
    }

    @GetMapping("/my-applications")
    public Result<PageResult<AdoptionApplicationVO>> getMyApplications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adoptionService.getMyApplications(pageNum, pageSize));
    }

    @GetMapping("/received")
    public Result<PageResult<AdoptionApplicationVO>> getReceivedApplications(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adoptionService.getReceivedApplications(status, pageNum, pageSize));
    }

    @GetMapping("/pending-review")
    public Result<PageResult<AdoptionApplicationVO>> getPendingReviewList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adoptionService.getPendingReviewList(pageNum, pageSize));
    }

    @PutMapping("/rescue-review")
    @OperLog(module = "领养管理", action = "救助方审核")
    public Result<Void> rescueReview(@Valid @RequestBody ReviewDTO dto) {
        adoptionService.rescueReview(dto);
        return Result.success();
    }

    @PutMapping("/admin-review")
    @OperLog(module = "领养管理", action = "管理员复核")
    public Result<Void> adminReview(@Valid @RequestBody ReviewDTO dto) {
        adoptionService.adminReview(dto);
        return Result.success();
    }

    @PutMapping("/home-visit")
    @OperLog(module = "领养管理", action = "更新家访状态")
    public Result<Void> updateHomeVisit(@Valid @RequestBody ReviewDTO dto) {
        adoptionService.updateHomeVisit(dto);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    public Result<AdoptionApplicationVO> getApplicationDetail(@PathVariable Long id) {
        return Result.success(adoptionService.getApplicationDetail(id));
    }
}
