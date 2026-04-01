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
import com.petadopt.dto.FollowUpDTO;
import com.petadopt.entity.AdoptionApplication;
import com.petadopt.entity.FollowUpRecord;
import com.petadopt.entity.Pet;
import com.petadopt.entity.User;
import com.petadopt.mapper.AdoptionApplicationMapper;
import com.petadopt.mapper.FollowUpRecordMapper;
import com.petadopt.mapper.PetMapper;
import com.petadopt.mapper.UserMapper;
import com.petadopt.service.FollowUpService;
import com.petadopt.service.MessageService;
import com.petadopt.util.UserContext;
import com.petadopt.vo.FollowUpRecordVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;


@Service
@Transactional
public class FollowUpServiceImpl extends ServiceImpl<FollowUpRecordMapper, FollowUpRecord> implements FollowUpService {

    private static final Logger logger = LoggerFactory.getLogger(FollowUpServiceImpl.class);
    private final PetMapper petMapper;
    private final UserMapper userMapper;
    private final AdoptionApplicationMapper applicationMapper;
    private final MessageService messageService;

    public FollowUpServiceImpl(PetMapper petMapper, UserMapper userMapper, AdoptionApplicationMapper applicationMapper, MessageService messageService) {
        this.petMapper = petMapper;
        this.userMapper = userMapper;
        this.applicationMapper = applicationMapper;
        this.messageService = messageService;
    }

    @Override
    public void submitFollowUp(FollowUpDTO dto) {
        Long userId = UserContext.getUserId();
        AdoptionApplication application = applicationMapper.selectById(dto.getApplicationId());
        if (application == null) {
            logger.error("提交跟进记录失败-申请不存在: userId={}, applicationId={}", userId, dto.getApplicationId());
            throw new BusinessException("领养申请不存在");
        }
        if (!application.getApplicantId().equals(userId)) {
            logger.error("提交跟进记录权限不足: userId={}, applicationId={}, applicantId={}", userId, dto.getApplicationId(), application.getApplicantId());
            throw new BusinessException("无权提交此跟进记录");
        }
        if (application.getFinalStatus() != 1) {
            logger.error("提交跟进记录失败-申请未成功: userId={}, applicationId={}, finalStatus={}", userId, dto.getApplicationId(), application.getFinalStatus());
            throw new BusinessException("只有领养成功的申请才能提交跟进记录");
        }

        FollowUpRecord record = new FollowUpRecord();
        record.setPetId(dto.getPetId());
        record.setAdopterId(userId);
        record.setApplicationId(dto.getApplicationId());
        record.setContent(dto.getContent());
        record.setStatus(1);
        record.setSubmitTime(LocalDateTime.now());

        if (dto.getPhotos() != null && !dto.getPhotos().isEmpty()) {
            record.setPhotos(JSONUtil.toJsonStr(dto.getPhotos()));
        }
        if (dto.getVideos() != null && !dto.getVideos().isEmpty()) {
            record.setVideos(JSONUtil.toJsonStr(dto.getVideos()));
        }

        save(record);

        Pet pet = petMapper.selectById(dto.getPetId());
        User adopter = userMapper.selectById(userId);
        String adopterName = adopter != null ? (adopter.getRealName() != null ? adopter.getRealName() : adopter.getUsername()) : "未知";

        if (pet != null) {
            // 通知救助方
            messageService.sendMessage(pet.getPublisherId(), "新的跟进记录",
                    "宠物\"" + pet.getName() + "\"有新的跟进记录，领养人：" + adopterName + "，请查看。", 3);

            // 通知所有管理员
            notifyAdmins("新的跟进记录",
                    "宠物\"" + pet.getName() + "\"有新的跟进记录，领养人：" + adopterName + "，请查看。");
        }

        logger.info("提交跟进记录成功: userId={}, recordId={}, petId={}, petName={}, applicationId={}",
                userId, record.getId(), dto.getPetId(), pet != null ? pet.getName() : "unknown", dto.getApplicationId());
    }

    private void notifyAdmins(String title, String content) {
        // 查询所有管理员
        java.util.List<User> admins = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getRoleType, RoleType.ADMIN.getCode())
        );
        for (User admin : admins) {
            messageService.sendMessage(admin.getId(), title, content, 3);
        }
    }

    @Override
    public PageResult<FollowUpRecordVO> getMyRecords(Integer pageNum, Integer pageSize) {
        Page<FollowUpRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FollowUpRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUpRecord::getAdopterId, UserContext.getUserId());
        wrapper.orderByDesc(FollowUpRecord::getCreateTime);

        Page<FollowUpRecord> result = page(page, wrapper);
        return convertToVOPage(result);
    }

    @Override
    public PageResult<FollowUpRecordVO> getPetRecords(Long petId, Integer pageNum, Integer pageSize) {
        Page<FollowUpRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FollowUpRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUpRecord::getPetId, petId);
        wrapper.orderByDesc(FollowUpRecord::getCreateTime);

        Page<FollowUpRecord> result = page(page, wrapper);
        return convertToVOPage(result);
    }

    @Override
    public PageResult<FollowUpRecordVO> getPendingList(Integer pageNum, Integer pageSize) {
        Page<FollowUpRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FollowUpRecord> wrapper = new LambdaQueryWrapper<>();
        // 查询所有跟进记录，不限制状态
        wrapper.orderByDesc(FollowUpRecord::getSubmitTime);

        Page<FollowUpRecord> result = page(page, wrapper);
        return convertToVOPage(result);
    }

    @Override
    public void reviewFollowUp(Long id, Integer status, String comment) {
        Long userId = UserContext.getUserId();
        FollowUpRecord record = getById(id);
        if (record == null) {
            logger.error("审核跟进记录失败-记录不存在: userId={}, recordId={}", userId, id);
            throw new BusinessException("跟进记录不存在");
        }

        Integer oldStatus = record.getStatus();
        FollowUpRecord update = new FollowUpRecord();
        update.setId(id);
        update.setStatus(status);
        update.setAdminComment(comment);
        updateById(update);

        if (status == 2) {
            messageService.sendMessage(record.getAdopterId(), "跟进记录异常提醒",
                    "您提交的跟进记录被标记为异常，请注意。" + (StringUtils.hasText(comment) ? "备注：" + comment : ""), 3);
            logger.info("标记跟进记录异常: userId={}, recordId={}, adopterId={}, petId={}, comment={}", userId, id, record.getAdopterId(), record.getPetId(), comment);
        } else {
            logger.info("审核跟进记录: userId={}, recordId={}, oldStatus={}, newStatus={}", userId, id, oldStatus, status);
        }
    }

    @Override
    @Transactional
    public void reclaimPet(Long petId, String reason) {
        Long userId = UserContext.getUserId();
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            logger.error("回收宠物失败-宠物不存在: userId={}, petId={}", userId, petId);
            throw new BusinessException("宠物不存在");
        }

        Pet updatePet = new Pet();
        updatePet.setId(petId);
        updatePet.setStatus(PetStatus.AVAILABLE.getCode());
        updatePet.setRemark("回收原因：" + reason);
        petMapper.updateById(updatePet);

        AdoptionApplication application = applicationMapper.selectOne(
                new LambdaQueryWrapper<AdoptionApplication>()
                        .eq(AdoptionApplication::getPetId, petId)
                        .eq(AdoptionApplication::getFinalStatus, 1)
                        .orderByDesc(AdoptionApplication::getCreateTime)
                        .last("LIMIT 1")
        );

        if (application != null) {
            messageService.sendMessage(application.getApplicantId(), "宠物回收通知",
                    "您领养的宠物\"" + pet.getName() + "\"已被回收。原因：" + reason, 1);
            logger.info("回收宠物成功: userId={}, petId={}, petName={}, adopterId={}, reason={}", userId, petId, pet.getName(), application.getApplicantId(), reason);
        } else {
            logger.info("回收宠物成功(无领养记录): userId={}, petId={}, petName={}, reason={}", userId, petId, pet.getName(), reason);
        }
    }

    private PageResult<FollowUpRecordVO> convertToVOPage(Page<FollowUpRecord> page) {
        Page<FollowUpRecordVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::convertToVO).toList());
        return PageResult.of(voPage);
    }

    private FollowUpRecordVO convertToVO(FollowUpRecord record) {
        FollowUpRecordVO vo = new FollowUpRecordVO();
        BeanUtil.copyProperties(record, vo);

        Pet pet = petMapper.selectById(record.getPetId());
        if (pet != null) {
            vo.setPetName(pet.getName());
        }

        User adopter = userMapper.selectById(record.getAdopterId());
        if (adopter != null) {
            vo.setAdopterName(adopter.getRealName() != null ? adopter.getRealName() : adopter.getUsername());
        }

        if (StringUtils.hasText(record.getPhotos())) {
            vo.setPhotos(JSONUtil.toList(record.getPhotos(), String.class));
        }
        if (StringUtils.hasText(record.getVideos())) {
            vo.setVideos(JSONUtil.toList(record.getVideos(), String.class));
        }

        vo.setStatusDisplay(record.getStatus() == 1 ? "正常" : "异常");
        return vo;
    }
}
