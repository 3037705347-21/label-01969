package com.petadopt.controller;

import com.petadopt.aspect.OperLog;
import com.petadopt.common.result.PageResult;
import com.petadopt.common.result.Result;
import com.petadopt.dto.PetDTO;
import com.petadopt.dto.PetQueryDTO;
import com.petadopt.service.PetService;
import com.petadopt.vo.PetVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping("/create")
    @OperLog(module = "宠物管理", action = "发布宠物")
    public Result<Void> createPet(@Valid @RequestBody PetDTO dto) {
        petService.createPet(dto);
        return Result.success();
    }

    @PutMapping("/update")
    @OperLog(module = "宠物管理", action = "更新宠物信息")
    public Result<Void> updatePet(@Valid @RequestBody PetDTO dto) {
        petService.updatePet(dto);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    public Result<PetVO> getPetDetail(@PathVariable Long id) {
        return Result.success(petService.getPetDetail(id));
    }

    @GetMapping("/list")
    public Result<PageResult<PetVO>> getPetList(PetQueryDTO query) {
        return Result.success(petService.getPetList(query));
    }

    @PutMapping("/status")
    @OperLog(module = "宠物管理", action = "更新宠物状态")
    public Result<Void> updatePetStatus(@RequestParam Long id, @RequestParam Integer status) {
        petService.updatePetStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperLog(module = "宠物管理", action = "删除宠物")
    public Result<Void> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return Result.success();
    }

    @GetMapping("/my-list")
    public Result<PageResult<PetVO>> getMyPetList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(petService.getMyPetList(pageNum, pageSize));
    }

    @GetMapping("/my-adopted")
    public Result<PageResult<PetVO>> getMyAdoptedPets(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(petService.getMyAdoptedPets(pageNum, pageSize));
    }

    @GetMapping("/stats")
    public Result<java.util.Map<String, Long>> getHomeStats() {
        return Result.success(petService.getHomeStats());
    }
}
