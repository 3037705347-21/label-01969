package com.petadopt.controller;

import com.petadopt.aspect.OperLog;
import com.petadopt.common.result.PageResult;
import com.petadopt.common.result.Result;
import com.petadopt.dto.FollowUpDTO;
import com.petadopt.service.FollowUpService;
import com.petadopt.vo.FollowUpRecordVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
public class FollowUpController {

    private final FollowUpService followUpService;

    public FollowUpController(FollowUpService followUpService) {
        this.followUpService = followUpService;
    }

    @PostMapping("/submit")
    @OperLog(module = "跟进管理", action = "提交跟进记录")
    public Result<Void> submitFollowUp(@Valid @RequestBody FollowUpDTO dto) {
        followUpService.submitFollowUp(dto);
        return Result.success();
    }

    @GetMapping("/my-records")
    public Result<PageResult<FollowUpRecordVO>> getMyRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(followUpService.getMyRecords(pageNum, pageSize));
    }

    @GetMapping("/pet-records/{petId}")
    public Result<PageResult<FollowUpRecordVO>> getPetRecords(
            @PathVariable Long petId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(followUpService.getPetRecords(petId, pageNum, pageSize));
    }

    @GetMapping("/pending")
    public Result<PageResult<FollowUpRecordVO>> getPendingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(followUpService.getPendingList(pageNum, pageSize));
    }

    @PutMapping("/review")
    @OperLog(module = "跟进管理", action = "审核跟进记录")
    public Result<Void> reviewFollowUp(
            @RequestParam Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String comment) {
        followUpService.reviewFollowUp(id, status, comment);
        return Result.success();
    }

    @PostMapping("/reclaim")
    @OperLog(module = "跟进管理", action = "回收宠物")
    public Result<Void> reclaimPet(@RequestParam Long petId, @RequestParam String reason) {
        followUpService.reclaimPet(petId, reason);
        return Result.success();
    }
}
