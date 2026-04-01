package com.petadopt.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadopt.common.enums.PetStatus;
import com.petadopt.common.enums.RoleType;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.common.result.PageResult;
import com.petadopt.dto.PetDTO;
import com.petadopt.dto.PetQueryDTO;
import com.petadopt.entity.Pet;
import com.petadopt.entity.User;
import com.petadopt.mapper.PetMapper;
import com.petadopt.mapper.UserMapper;
import com.petadopt.service.PetService;
import com.petadopt.util.UserContext;
import com.petadopt.vo.PetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {

    private static final Logger logger = LoggerFactory.getLogger(PetServiceImpl.class);
    private final UserMapper userMapper;

    public PetServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void createPet(PetDTO dto) {
        Long userId = UserContext.getUserId();
        Integer roleType = UserContext.getRoleType();
        if (!RoleType.RESCUER.getCode().equals(roleType) && !RoleType.ADMIN.getCode().equals(roleType)) {
            logger.error("发布宠物权限不足: userId={}, roleType={}", userId, roleType);
            throw new BusinessException("只有救助方可以发布宠物信息");
        }

        Pet pet = new Pet();
        BeanUtil.copyProperties(dto, pet);
        pet.setPublisherId(userId);
        pet.setStatus(PetStatus.AVAILABLE.getCode());

        if (dto.getPersonalityTags() != null) {
            pet.setPersonalityTags(JSONUtil.toJsonStr(dto.getPersonalityTags()));
        }
        if (dto.getPhotos() != null) {
            pet.setPhotos(JSONUtil.toJsonStr(dto.getPhotos()));
        }
        if (dto.getVideos() != null) {
            pet.setVideos(JSONUtil.toJsonStr(dto.getVideos()));
        }

        save(pet);
        logger.info("发布宠物信息成功: userId={}, petId={}, name={}", userId, pet.getId(), pet.getName());
    }

    @Override
    public void updatePet(PetDTO dto) {
        Long userId = UserContext.getUserId();
        if (dto.getId() == null) {
            logger.error("更新宠物失败-ID为空: userId={}", userId);
            throw new BusinessException("宠物ID不能为空");
        }

        Pet existPet = getById(dto.getId());
        if (existPet == null) {
            logger.error("更新宠物失败-宠物不存在: userId={}, petId={}", userId, dto.getId());
            throw new BusinessException("宠物不存在");
        }

        Integer roleType = UserContext.getRoleType();
        if (!existPet.getPublisherId().equals(userId) && !RoleType.ADMIN.getCode().equals(roleType)) {
            logger.error("更新宠物权限不足: userId={}, petId={}, publisherId={}", userId, dto.getId(), existPet.getPublisherId());
            throw new BusinessException("无权修改此宠物信息");
        }

        Pet pet = new Pet();
        BeanUtil.copyProperties(dto, pet);

        if (dto.getPersonalityTags() != null) {
            pet.setPersonalityTags(JSONUtil.toJsonStr(dto.getPersonalityTags()));
        }
        if (dto.getPhotos() != null) {
            pet.setPhotos(JSONUtil.toJsonStr(dto.getPhotos()));
        }
        if (dto.getVideos() != null) {
            pet.setVideos(JSONUtil.toJsonStr(dto.getVideos()));
        }

        updateById(pet);
        logger.info("更新宠物信息成功: userId={}, petId={}", userId, dto.getId());
    }

    @Override
    public PetVO getPetDetail(Long id) {
        Pet pet = getById(id);
        if (pet == null) {
            logger.warn("获取宠物详情失败-宠物不存在: petId={}", id);
            throw new BusinessException("宠物不存在");
        }
        return convertToVO(pet);
    }

    @Override
    public PageResult<PetVO> getPetList(PetQueryDTO query) {
        Page<Pet> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getSpecies())) {
            wrapper.eq(Pet::getSpecies, query.getSpecies());
        }
        if (StringUtils.hasText(query.getBreed())) {
            wrapper.like(Pet::getBreed, query.getBreed());
        }
        if (query.getMinAge() != null) {
            wrapper.ge(Pet::getAgeMonths, query.getMinAge());
        }
        if (query.getMaxAge() != null) {
            wrapper.le(Pet::getAgeMonths, query.getMaxAge());
        }
        if (query.getGender() != null) {
            wrapper.eq(Pet::getGender, query.getGender());
        }
        if (StringUtils.hasText(query.getLocation())) {
            wrapper.like(Pet::getLocation, query.getLocation());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Pet::getStatus, query.getStatus());
        } else {
            wrapper.eq(Pet::getStatus, PetStatus.AVAILABLE.getCode());
        }

        wrapper.orderByDesc(Pet::getCreateTime);
        Page<Pet> result = page(page, wrapper);

        Page<PetVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).toList());

        return PageResult.of(voPage);
    }

    @Override
    public void updatePetStatus(Long id, Integer status) {
        Long userId = UserContext.getUserId();
        Pet pet = getById(id);
        if (pet == null) {
            logger.error("更新宠物状态失败-宠物不存在: userId={}, petId={}", userId, id);
            throw new BusinessException("宠物不存在");
        }

        Integer roleType = UserContext.getRoleType();
        if (!pet.getPublisherId().equals(userId) && !RoleType.ADMIN.getCode().equals(roleType)) {
            logger.error("更新宠物状态权限不足: userId={}, petId={}, publisherId={}", userId, id, pet.getPublisherId());
            throw new BusinessException("无权修改此宠物状态");
        }

        Integer oldStatus = pet.getStatus();
        Pet updatePet = new Pet();
        updatePet.setId(id);
        updatePet.setStatus(status);
        updateById(updatePet);

        logger.info("更新宠物状态成功: userId={}, petId={}, oldStatus={}, newStatus={}", userId, id, oldStatus, status);
    }

    @Override
    public void deletePet(Long id) {
        Long userId = UserContext.getUserId();
        Pet pet = getById(id);
        if (pet == null) {
            logger.error("删除宠物失败-宠物不存在: userId={}, petId={}", userId, id);
            throw new BusinessException("宠物不存在");
        }

        Integer roleType = UserContext.getRoleType();
        if (!pet.getPublisherId().equals(userId) && !RoleType.ADMIN.getCode().equals(roleType)) {
            logger.error("删除宠物权限不足: userId={}, petId={}, publisherId={}", userId, id, pet.getPublisherId());
            throw new BusinessException("无权删除此宠物");
        }

        removeById(id);
        logger.info("删除宠物成功: userId={}, petId={}, petName={}", userId, id, pet.getName());
    }

    @Override
    public PageResult<PetVO> getMyPetList(Integer pageNum, Integer pageSize) {
        Page<Pet> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getPublisherId, UserContext.getUserId());
        wrapper.orderByDesc(Pet::getCreateTime);

        Page<Pet> result = page(page, wrapper);

        Page<PetVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).toList());

        return PageResult.of(voPage);
    }

    @Override
    public PageResult<PetVO> getMyAdoptedPets(Integer pageNum, Integer pageSize) {
        Page<Pet> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getAdopterId, UserContext.getUserId());
        wrapper.orderByDesc(Pet::getUpdateTime);

        Page<Pet> result = page(page, wrapper);

        Page<PetVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).toList());

        return PageResult.of(voPage);
    }

    @Override
    public java.util.Map<String, Long> getHomeStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        // 待领养宠物数
        Long petCount = lambdaQuery().eq(Pet::getStatus, PetStatus.AVAILABLE.getCode()).count();
        // 成功领养数
        Long adoptedCount = lambdaQuery().eq(Pet::getStatus, PetStatus.ADOPTED.getCode()).count();
        // 注册用户数
        Long userCount = userMapper.selectCount(null);
        stats.put("petCount", petCount);
        stats.put("adoptedCount", adoptedCount);
        stats.put("userCount", userCount);
        return stats;
    }

    private PetVO convertToVO(Pet pet) {
        PetVO vo = new PetVO();
        BeanUtil.copyProperties(pet, vo);

        User publisher = userMapper.selectById(pet.getPublisherId());
        if (publisher != null) {
            vo.setPublisherName(publisher.getOrgName() != null ? publisher.getOrgName() : publisher.getRealName());
        }

        if (StringUtils.hasText(pet.getPersonalityTags())) {
            vo.setPersonalityTags(JSONUtil.toList(pet.getPersonalityTags(), String.class));
        }
        if (StringUtils.hasText(pet.getPhotos())) {
            vo.setPhotos(JSONUtil.toList(pet.getPhotos(), String.class));
        }
        if (StringUtils.hasText(pet.getVideos())) {
            vo.setVideos(JSONUtil.toList(pet.getVideos(), String.class));
        }

        vo.setGenderDisplay(pet.getGender() == 1 ? "公" : "母");
        vo.setStatusDisplay(PetStatus.getNameByCode(pet.getStatus()));

        int months = pet.getAgeMonths() != null ? pet.getAgeMonths() : 0;
        if (months < 12) {
            vo.setAgeDisplay(months + "个月");
        } else {
            int years = months / 12;
            int remainMonths = months % 12;
            vo.setAgeDisplay(remainMonths > 0 ? years + "岁" + remainMonths + "个月" : years + "岁");
        }

        return vo;
    }
}
