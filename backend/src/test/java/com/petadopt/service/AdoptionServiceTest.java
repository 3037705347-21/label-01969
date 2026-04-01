package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadopt.BaseServiceTest;
import com.petadopt.common.enums.ApplicationStatus;
import com.petadopt.common.enums.PetStatus;
import com.petadopt.common.enums.RoleType;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.dto.AdoptionApplyDTO;
import com.petadopt.dto.ReviewDTO;
import com.petadopt.entity.AdoptionApplication;
import com.petadopt.entity.Pet;
import com.petadopt.entity.User;
import com.petadopt.mapper.AdoptionApplicationMapper;
import com.petadopt.mapper.PetMapper;
import com.petadopt.mapper.UserMapper;
import com.petadopt.service.impl.AdoptionServiceImpl;
import com.petadopt.util.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AdoptionServiceTest extends BaseServiceTest {

    @Mock
    private AdoptionApplicationMapper applicationMapper;

    @Mock
    private PetMapper petMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private AdoptionServiceImpl adoptionService;

    @BeforeEach
    void setUpBase() {
        setBaseMapper(adoptionService, applicationMapper);
    }

    private Pet testPet;
    private User adopterUser;
    private User rescuerUser;
    private User adminUser;
    private AdoptionApplication testApplication;
    private AdoptionApplyDTO applyDTO;
    private ReviewDTO reviewDTO;

    @BeforeEach
    void setUp() {
        adopterUser = new User();
        adopterUser.setId(1L);
        adopterUser.setRoleType(RoleType.ADOPTER.getCode());
        adopterUser.setUsername("adopter");
        adopterUser.setRealName("领养人测试");

        rescuerUser = new User();
        rescuerUser.setId(2L);
        rescuerUser.setRoleType(RoleType.RESCUER.getCode());
        rescuerUser.setUsername("rescuer");

        adminUser = new User();
        adminUser.setId(99L);
        adminUser.setRoleType(RoleType.ADMIN.getCode());

        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("测试猫咪");
        testPet.setPublisherId(2L);
        testPet.setStatus(PetStatus.AVAILABLE.getCode());

        testApplication = new AdoptionApplication();
        testApplication.setId(1L);
        testApplication.setPetId(1L);
        testApplication.setApplicantId(1L);
        testApplication.setRescueReviewStatus(0);
        testApplication.setAdminReviewStatus(0);
        testApplication.setHomeVisitStatus(0);
        testApplication.setFinalStatus(ApplicationStatus.PENDING.getCode());

        applyDTO = new AdoptionApplyDTO();
        applyDTO.setPetId(1L);
        applyDTO.setSelfIntroduction("我喜欢小动物");

        reviewDTO = new ReviewDTO();
        reviewDTO.setApplicationId(1L);
        reviewDTO.setStatus(1);
        reviewDTO.setComment("审核通过");
    }

    @Test
    void testApply_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.ADOPTER.getCode());
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(applicationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(applicationMapper.insert(any(AdoptionApplication.class))).thenReturn(1);
            when(petMapper.updateById(any(Pet.class))).thenReturn(1);

            assertDoesNotThrow(() -> {
                adoptionService.apply(applyDTO);
            });

            verify(applicationMapper, times(1)).insert(any(AdoptionApplication.class));
            verify(petMapper, times(1)).updateById(any(Pet.class));
            verify(messageService, times(1)).sendMessage(eq(2L), anyString(), anyString(), eq(2));
        }
    }

    @Test
    void testApply_NotAdopterRole() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(2L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.RESCUER.getCode());

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adoptionService.apply(applyDTO);
            });

            assertEquals("只有领养人可以提交领养申请", exception.getMessage());
        }
    }

    @Test
    void testApply_PetNotExist() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.ADOPTER.getCode());
            when(petMapper.selectById(999L)).thenReturn(null);

            applyDTO.setPetId(999L);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adoptionService.apply(applyDTO);
            });

            assertEquals("宠物不存在", exception.getMessage());
        }
    }

    @Test
    void testApply_PetNotAvailable() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.ADOPTER.getCode());
            testPet.setStatus(PetStatus.ADOPTED.getCode());
            when(petMapper.selectById(1L)).thenReturn(testPet);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adoptionService.apply(applyDTO);
            });

            assertEquals("该宠物当前不可领养", exception.getMessage());
        }
    }

    @Test
    void testApply_DuplicateApplication() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.ADOPTER.getCode());
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(applicationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adoptionService.apply(applyDTO);
            });

            assertEquals("您已提交过该宠物的领养申请", exception.getMessage());
        }
    }

    @Test
    void testGetMyApplications_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);

            Page<AdoptionApplication> pageResult = new Page<>(1, 10);
            pageResult.setRecords(Collections.singletonList(testApplication));
            pageResult.setTotal(1L);

            when(applicationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(userMapper.selectById(1L)).thenReturn(adopterUser);

            var result = adoptionService.getMyApplications(1, 10);

            assertNotNull(result);
            assertEquals(1L, result.getTotal());
        }
    }

    @Test
    void testGetReceivedApplications_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(2L);

            when(petMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(testPet));

            Page<AdoptionApplication> pageResult = new Page<>(1, 10);
            pageResult.setRecords(Collections.singletonList(testApplication));
            pageResult.setTotal(1L);

            when(applicationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(userMapper.selectById(1L)).thenReturn(adopterUser);

            var result = adoptionService.getReceivedApplications(null, 1, 10);

            assertNotNull(result);
            assertEquals(1L, result.getTotal());
        }
    }

    @Test
    void testRescueReview_Approve_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(2L);
            when(applicationMapper.selectById(1L)).thenReturn(testApplication);
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(applicationMapper.updateById(any(AdoptionApplication.class))).thenReturn(1);
            when(userMapper.selectById(1L)).thenReturn(adopterUser);
            when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(adminUser));

            adoptionService.rescueReview(reviewDTO);

            verify(applicationMapper, times(1)).updateById(any(AdoptionApplication.class));
            verify(messageService, atLeastOnce()).sendMessage(anyLong(), anyString(), anyString(), eq(2));
        }
    }

    @Test
    void testRescueReview_Reject_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(2L);
            reviewDTO.setStatus(2);
            when(applicationMapper.selectById(1L)).thenReturn(testApplication);
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(applicationMapper.updateById(any(AdoptionApplication.class))).thenReturn(1);
            when(petMapper.updateById(any(Pet.class))).thenReturn(1);

            adoptionService.rescueReview(reviewDTO);

            verify(applicationMapper, times(1)).updateById(any(AdoptionApplication.class));
            verify(petMapper, times(1)).updateById(any(Pet.class));
        }
    }

    @Test
    void testRescueReview_PermissionDenied() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(999L);
            when(applicationMapper.selectById(1L)).thenReturn(testApplication);
            when(petMapper.selectById(1L)).thenReturn(testPet);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adoptionService.rescueReview(reviewDTO);
            });

            assertEquals("无权审核此申请", exception.getMessage());
        }
    }

    @Test
    void testAdminReview_Approve_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(99L);
            testApplication.setRescueReviewStatus(1);
            when(applicationMapper.selectById(1L)).thenReturn(testApplication);
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(applicationMapper.updateById(any(AdoptionApplication.class))).thenReturn(1);
            when(userMapper.selectById(1L)).thenReturn(adopterUser);

            adoptionService.adminReview(reviewDTO);

            verify(applicationMapper, times(1)).updateById(any(AdoptionApplication.class));
            verify(messageService, atLeastOnce()).sendMessage(anyLong(), anyString(), anyString(), eq(2));
        }
    }

    @Test
    void testAdminReview_NotPassedRescueReview() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(99L);
            testApplication.setRescueReviewStatus(0);
            when(applicationMapper.selectById(1L)).thenReturn(testApplication);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adoptionService.adminReview(reviewDTO);
            });

            assertEquals("该申请尚未通过救助方审核", exception.getMessage());
        }
    }

    @Test
    void testUpdateHomeVisit_Pass_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(99L);
            reviewDTO.setStatus(2);
            when(applicationMapper.selectById(1L)).thenReturn(testApplication);
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(userMapper.selectById(1L)).thenReturn(adopterUser);
            when(applicationMapper.updateById(any(AdoptionApplication.class))).thenReturn(1);
            when(petMapper.updateById(any(Pet.class))).thenReturn(1);

            adoptionService.updateHomeVisit(reviewDTO);

            verify(applicationMapper, times(1)).updateById(any(AdoptionApplication.class));
            verify(petMapper, times(1)).updateById(any(Pet.class));
        }
    }

    @Test
    void testGetApplicationDetail_Success() {
        when(applicationMapper.selectById(1L)).thenReturn(testApplication);
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userMapper.selectById(1L)).thenReturn(adopterUser);

        var result = adoptionService.getApplicationDetail(1L);

        assertNotNull(result);
        assertEquals("测试猫咪", result.getPetName());
    }

    @Test
    void testGetApplicationDetail_NotExist() {
        when(applicationMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adoptionService.getApplicationDetail(999L);
        });

        assertEquals("申请不存在", exception.getMessage());
    }
}
