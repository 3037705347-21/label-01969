package com.petadopt.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadopt.common.enums.ApplicationStatus;
import com.petadopt.common.enums.PetStatus;
import com.petadopt.common.enums.RoleType;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.common.result.PageResult;
import com.petadopt.dto.AdoptionApplyDTO;
import com.petadopt.dto.ReviewDTO;
import com.petadopt.entity.AdoptionApplication;
import com.petadopt.entity.Pet;
import com.petadopt.entity.User;
import com.petadopt.mapper.AdoptionApplicationMapper;
import com.petadopt.mapper.PetMapper;
import com.petadopt.mapper.UserMapper;
import com.petadopt.service.AdoptionService;
import com.petadopt.service.MessageService;
import com.petadopt.util.UserContext;
import com.petadopt.vo.AdoptionApplicationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional
public class AdoptionServiceImpl extends ServiceImpl<AdoptionApplicationMapper, AdoptionApplication> implements AdoptionService {

    private static final Logger logger = LoggerFactory.getLogger(AdoptionServiceImpl.class);
    private final PetMapper petMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;

    public AdoptionServiceImpl(PetMapper petMapper, UserMapper userMapper, MessageService messageService) {
        this.petMapper = petMapper;
        this.userMapper = userMapper;
        this.messageService = messageService;
    }

    @Override
    @Transactional
    public void apply(AdoptionApplyDTO dto) {
        Long userId = UserContext.getUserId();
        Integer roleType = UserContext.getRoleType();
        if (!RoleType.ADOPTER.getCode().equals(roleType)) {
            logger.error("提交领养申请权限不足: userId={}, roleType={}", userId, roleType);
            throw new BusinessException("只有领养人可以提交领养申请");
        }

        Pet pet = petMapper.selectById(dto.getPetId());
        if (pet == null) {
            logger.error("提交领养申请失败-宠物不存在: userId={}, petId={}", userId, dto.getPetId());
            throw new BusinessException("宠物不存在");
        }
        if (!PetStatus.AVAILABLE.getCode().equals(pet.getStatus())) {
            logger.error("提交领养申请失败-宠物不可领养: userId={}, petId={}, status={}", userId, dto.getPetId(), pet.getStatus());
            throw new BusinessException("该宠物当前不可领养");
        }

        Long count = lambdaQuery()
                .eq(AdoptionApplication::getPetId, dto.getPetId())
                .eq(AdoptionApplication::getApplicantId, userId)
                .eq(AdoptionApplication::getFinalStatus, ApplicationStatus.PENDING.getCode())
                .count();
        if (count > 0) {
            logger.error("提交领养申请失败-重复申请: userId={}, petId={}", userId, dto.getPetId());
            throw new BusinessException("您已提交过该宠物的领养申请");
        }

        AdoptionApplication application = new AdoptionApplication();
        BeanUtil.copyProperties(dto, application);
        application.setApplicantId(userId);
        application.setRescueReviewStatus(0);
        application.setAdminReviewStatus(0);
        application.setHomeVisitStatus(0);
        application.setFinalStatus(ApplicationStatus.PENDING.getCode());

        save(application);

        Pet updatePet = new Pet();
        updatePet.setId(pet.getId());
        updatePet.setStatus(PetStatus.REVIEWING.getCode());
        petMapper.updateById(updatePet);

        messageService.sendMessage(pet.getPublisherId(), "新的领养申请",
                "您发布的宠物\"" + pet.getName() + "\"收到了新的领养申请，请及时处理。", 2);

        logger.info("提交领养申请成功: userId={}, applicationId={}, petId={}, petName={}", userId, application.getId(), dto.getPetId(), pet.getName());
    }

    @Override
    public PageResult<AdoptionApplicationVO> getMyApplications(Integer pageNum, Integer pageSize) {
        Page<AdoptionApplication> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdoptionApplication::getApplicantId, UserContext.getUserId());
        wrapper.orderByDesc(AdoptionApplication::getCreateTime);

        Page<AdoptionApplication> result = page(page, wrapper);
        return convertToVOPage(result);
    }

    @Override
    public PageResult<AdoptionApplicationVO> getReceivedApplications(Integer status, Integer pageNum, Integer pageSize) {
        List<Long> petIds = petMapper.selectList(
                new LambdaQueryWrapper<Pet>().eq(Pet::getPublisherId, UserContext.getUserId())
        ).stream().map(Pet::getId).toList();

        if (petIds.isEmpty()) {
            return PageResult.of(new Page<>());
        }

        Page<AdoptionApplication> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AdoptionApplication::getPetId, petIds);
        if (status != null) {
            wrapper.eq(AdoptionApplication::getRescueReviewStatus, status);
        }
        wrapper.orderByDesc(AdoptionApplication::getCreateTime);

        Page<AdoptionApplication> result = page(page, wrapper);
        return convertToVOPage(result);
    }

    @Override
    public PageResult<AdoptionApplicationVO> getPendingReviewList(Integer pageNum, Integer pageSize) {
        Page<AdoptionApplication> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdoptionApplication::getRescueReviewStatus, 1);
        // 查询待复核(0)或已通过复核但未完成家访(1且家访状态不是2通过也不是3不通过)的申请
        wrapper.and(w -> w
            .eq(AdoptionApplication::getAdminReviewStatus, 0)
            .or(orW -> orW
                .eq(AdoptionApplication::getAdminReviewStatus, 1)
                .in(AdoptionApplication::getHomeVisitStatus, 0, 1)
            )
        );
        wrapper.orderByDesc(AdoptionApplication::getCreateTime);

        Page<AdoptionApplication> result = page(page, wrapper);
        return convertToVOPage(result);
    }

    @Override
    @Transactional
    public void rescueReview(ReviewDTO dto) {
        Long userId = UserContext.getUserId();
        AdoptionApplication application = getById(dto.getApplicationId());
        if (application == null) {
            logger.error("救助方审核失败-申请不存在: userId={}, applicationId={}", userId, dto.getApplicationId());
            throw new BusinessException("申请不存在");
        }

        Pet pet = petMapper.selectById(application.getPetId());
        if (pet == null || !pet.getPublisherId().equals(userId)) {
            logger.error("救助方审核权限不足: userId={}, applicationId={}, publisherId={}", userId, dto.getApplicationId(), pet != null ? pet.getPublisherId() : null);
            throw new BusinessException("无权审核此申请");
        }

        AdoptionApplication update = new AdoptionApplication();
        update.setId(dto.getApplicationId());
        update.setRescueReviewStatus(dto.getStatus());
        update.setRescueReviewComment(dto.getComment());
        update.setRescueReviewTime(LocalDateTime.now());

        if (dto.getStatus() == 2) {
            update.setFinalStatus(ApplicationStatus.FAILED.getCode());
            Pet updatePet = new Pet();
            updatePet.setId(pet.getId());
            updatePet.setStatus(PetStatus.AVAILABLE.getCode());
            petMapper.updateById(updatePet);
            logger.info("救助方审核拒绝: userId={}, applicationId={}, petId={}, applicantId={}, comment={}", userId, dto.getApplicationId(), pet.getId(), application.getApplicantId(), dto.getComment());
        }

        updateById(update);

        String statusText = dto.getStatus() == 1 ? "通过" : "未通过";
        messageService.sendMessage(application.getApplicantId(), "领养申请审核结果",
                "您的领养申请救助方初审" + statusText + "。" + (StringUtils.hasText(dto.getComment()) ? "备注：" + dto.getComment() : ""), 2);

        // 如果救助方审核通过，通知所有管理员
        if (dto.getStatus() == 1) {
            User applicant = userMapper.selectById(application.getApplicantId());
            String applicantName = applicant != null ? (applicant.getRealName() != null ? applicant.getRealName() : applicant.getUsername()) : "未知";
            notifyAdmins("新的领养申请待复核",
                    "宠物\"" + pet.getName() + "\"的领养申请已通过救助方初审，申请人：" + applicantName + "，请及时复核。");
            logger.info("救助方审核通过: userId={}, applicationId={}, petId={}, applicantId={}", userId, dto.getApplicationId(), pet.getId(), application.getApplicantId());
        }
    }

    @Override
    @Transactional
    public void adminReview(ReviewDTO dto) {
        Long userId = UserContext.getUserId();
        AdoptionApplication application = getById(dto.getApplicationId());
        if (application == null) {
            logger.error("管理员复核失败-申请不存在: userId={}, applicationId={}", userId, dto.getApplicationId());
            throw new BusinessException("申请不存在");
        }

        if (application.getRescueReviewStatus() != 1) {
            logger.error("管理员复核失败-未通过救助方审核: userId={}, applicationId={}, rescueReviewStatus={}", userId, dto.getApplicationId(), application.getRescueReviewStatus());
            throw new BusinessException("该申请尚未通过救助方审核");
        }

        Pet pet = petMapper.selectById(application.getPetId());

        AdoptionApplication update = new AdoptionApplication();
        update.setId(dto.getApplicationId());
        update.setAdminReviewStatus(dto.getStatus());
        update.setAdminReviewComment(dto.getComment());
        update.setAdminReviewTime(LocalDateTime.now());

        if (dto.getStatus() == 2) {
            update.setFinalStatus(ApplicationStatus.FAILED.getCode());
            if (pet != null) {
                Pet updatePet = new Pet();
                updatePet.setId(pet.getId());
                updatePet.setStatus(PetStatus.AVAILABLE.getCode());
                petMapper.updateById(updatePet);
            }
            logger.info("管理员复核拒绝: userId={}, applicationId={}, applicantId={}, comment={}", userId, dto.getApplicationId(), application.getApplicantId(), dto.getComment());
        } else {
            logger.info("管理员复核通过: userId={}, applicationId={}, applicantId={}", userId, dto.getApplicationId(), application.getApplicantId());
        }

        updateById(update);

        String statusText = dto.getStatus() == 1 ? "通过" : "未通过";
        // 通知领养人
        messageService.sendMessage(application.getApplicantId(), "领养申请复核结果",
                "您的领养申请管理员复核" + statusText + "。" + (StringUtils.hasText(dto.getComment()) ? "备注：" + dto.getComment() : ""), 2);

        // 通知救助方
        if (pet != null) {
            User applicant = userMapper.selectById(application.getApplicantId());
            String applicantName = applicant != null ? (applicant.getRealName() != null ? applicant.getRealName() : applicant.getUsername()) : "未知";
            messageService.sendMessage(pet.getPublisherId(), "领养申请复核结果",
                    "宠物\"" + pet.getName() + "\"的领养申请管理员复核" + statusText + "，申请人：" + applicantName + "。", 2);
        }
    }

    @Override
    @Transactional
    public void updateHomeVisit(ReviewDTO dto) {
        Long userId = UserContext.getUserId();
        AdoptionApplication application = getById(dto.getApplicationId());
        if (application == null) {
            logger.error("更新家访状态失败-申请不存在: userId={}, applicationId={}", userId, dto.getApplicationId());
            throw new BusinessException("申请不存在");
        }

        Pet pet = petMapper.selectById(application.getPetId());
        User applicant = userMapper.selectById(application.getApplicantId());
        String applicantName = applicant != null ? (applicant.getRealName() != null ? applicant.getRealName() : applicant.getUsername()) : "未知";

        AdoptionApplication update = new AdoptionApplication();
        update.setId(dto.getApplicationId());
        update.setHomeVisitStatus(dto.getStatus());
        update.setHomeVisitComment(dto.getComment());
        update.setHomeVisitTime(LocalDateTime.now());

        String homeVisitStatusText = "";
        if (dto.getStatus() == 1) {
            homeVisitStatusText = "已安排家访";
            // 通知领养人
            messageService.sendMessage(application.getApplicantId(), "家访安排通知",
                    "您的领养申请已安排家访，请保持电话畅通。" + (StringUtils.hasText(dto.getComment()) ? "备注：" + dto.getComment() : ""), 2);
            logger.info("安排家访: userId={}, applicationId={}, applicantId={}, applicantName={}", userId, dto.getApplicationId(), application.getApplicantId(), applicantName);
        } else if (dto.getStatus() == 2) {
            homeVisitStatusText = "家访通过";
            update.setFinalStatus(ApplicationStatus.SUCCESS.getCode());
            if (pet != null) {
                Pet updatePet = new Pet();
                updatePet.setId(pet.getId());
                updatePet.setStatus(PetStatus.ADOPTED.getCode());
                updatePet.setAdopterId(application.getApplicantId());
                petMapper.updateById(updatePet);
            }

            messageService.sendMessage(application.getApplicantId(), "领养成功",
                    "恭喜您，领养申请已通过！请按照约定时间前往领取您的新伙伴。", 2);
            logger.info("家访通过-领养成功: userId={}, applicationId={}, applicantId={}, applicantName={}, petId={}, petName={}",
                    userId, dto.getApplicationId(), application.getApplicantId(), applicantName, application.getPetId(), pet != null ? pet.getName() : "unknown");
        } else if (dto.getStatus() == 3) {
            homeVisitStatusText = "家访不通过";
            update.setFinalStatus(ApplicationStatus.FAILED.getCode());
            if (pet != null) {
                Pet updatePet = new Pet();
                updatePet.setId(pet.getId());
                updatePet.setStatus(PetStatus.AVAILABLE.getCode());
                petMapper.updateById(updatePet);
            }

            messageService.sendMessage(application.getApplicantId(), "家访未通过",
                    "很遗憾，您的领养申请家访未通过。" + (StringUtils.hasText(dto.getComment()) ? "原因：" + dto.getComment() : ""), 2);
            logger.info("家访不通过: userId={}, applicationId={}, applicantId={}, applicantName={}, comment={}",
                    userId, dto.getApplicationId(), application.getApplicantId(), applicantName, dto.getComment());
        }

        updateById(update);

        // 通知救助方家访结果
        if (pet != null && dto.getStatus() > 0) {
            messageService.sendMessage(pet.getPublisherId(), "家访状态更新",
                    "宠物\"" + pet.getName() + "\"的领养申请" + homeVisitStatusText + "，申请人：" + applicantName + "。", 2);
        }
    }

    @Override
    public AdoptionApplicationVO getApplicationDetail(Long id) {
        AdoptionApplication application = getById(id);
        if (application == null) {
            logger.warn("获取申请详情失败-申请不存在: applicationId={}", id);
            throw new BusinessException("申请不存在");
        }
        return convertToVO(application);
    }

    private PageResult<AdoptionApplicationVO> convertToVOPage(Page<AdoptionApplication> page) {
        Page<AdoptionApplicationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::convertToVO).toList());
        return PageResult.of(voPage);
    }

    private void notifyAdmins(String title, String content) {
        // 查询所有管理员
        List<User> admins = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getRoleType, RoleType.ADMIN.getCode())
        );
        for (User admin : admins) {
            messageService.sendMessage(admin.getId(), title, content, 2);
        }
    }

    private AdoptionApplicationVO convertToVO(AdoptionApplication application) {
        AdoptionApplicationVO vo = new AdoptionApplicationVO();
        BeanUtil.copyProperties(application, vo);

        Pet pet = petMapper.selectById(application.getPetId());
        if (pet != null) {
            vo.setPetName(pet.getName());
            if (StringUtils.hasText(pet.getPhotos())) {
                List<String> photos = cn.hutool.json.JSONUtil.toList(pet.getPhotos(), String.class);
                if (!photos.isEmpty()) {
                    vo.setPetPhoto(photos.get(0));
                }
            }
        }

        User applicant = userMapper.selectById(application.getApplicantId());
        if (applicant != null) {
            vo.setApplicantName(applicant.getRealName() != null ? applicant.getRealName() : applicant.getUsername());
            vo.setApplicantPhone(applicant.getPhone());
        }

        vo.setFinalStatusDisplay(ApplicationStatus.getNameByCode(application.getFinalStatus()));
        return vo;
    }
}
