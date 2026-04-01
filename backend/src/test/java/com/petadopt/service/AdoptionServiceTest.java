package com.petadopt.service;

import com.petadopt.common.enums.ApplicationStatus;
import com.petadopt.common.enums.PetStatus;
import com.petadopt.common.enums.RoleType;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("领养申请服务测试")
class AdoptionServiceTest {

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

    private Pet testPet;
    private User testAdopter;
    private User testRescuer;
    private AdoptionApplication testApplication;

    @BeforeEach
    void setUp() {
        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("小白");
        testPet.setPublisherId(2L);
        testPet.setStatus(PetStatus.AVAILABLE.getCode());

        testAdopter = new User();
        testAdopter.setId(1L);
        testAdopter.setUsername("adopter");
        testAdopter.setRoleType(RoleType.ADOPTER.getCode());

        testRescuer = new User();
        testRescuer.setId(2L);
        testRescuer.setUsername("rescuer");
        testRescuer.setRoleType(RoleType.RESCUER.getCode());

        testApplication = new AdoptionApplication();
        testApplication.setId(1L);
        testApplication.setPetId(1L);
        testApplication.setApplicantId(1L);
        testApplication.setRescueReviewStatus(0);
        testApplication.setAdminReviewStatus(0);
        testApplication.setHomeVisitStatus(0);
        testApplication.setFinalStatus(ApplicationStatus.PENDING.getCode());
    }

    @Test
    @DisplayName("测试领养人提交申请成功")
    void testApply_Success() {
        AdoptionApplyDTO applyDTO = new AdoptionApplyDTO();
        applyDTO.setPetId(1L);
        applyDTO.setSelfIntroduction("我很喜欢小动物");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);
            mockedUserContext.when(UserContext::getRoleType).thenReturn(RoleType.ADOPTER.getCode());

            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(applicationMapper.selectCount(any())).thenReturn(0L);
            when(applicationMapper.insert(any(AdoptionApplication.class))).thenAnswer(invocation -> {
                AdoptionApplication app = invocation.getArgument(0);
                app.setId(1L);
                return 1;
            });
            when(petMapper.updateById(any(Pet.class))).thenReturn(1);
            doNothing().when(messageService).sendMessage(anyLong(), anyString(), anyString(), anyInt());

            assertDoesNotThrow(() -> adoptionService.apply(applyDTO));

            verify(applicationMapper, times(1)).insert(any(AdoptionApplication.class));
            verify(petMapper, times(1)).updateById(any(Pet.class));
            verify(messageService, times(1)).sendMessage(eq(2L), anyString(), anyString(), eq(2));
        }
    }

    @Test
    @DisplayName("测试提交申请-只有领养人可以申请")
    void testApply_NotAdopter() {
        AdoptionApplyDTO applyDTO = new AdoptionApplyDTO();
        applyDTO.setPetId(1L);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(2L);
            mockedUserContext.when(UserContext::getRoleType).thenReturn(RoleType.RESCUER.getCode());

            Exception exception = assertThrows(RuntimeException.class, () -> {
                adoptionService.apply(applyDTO);
            });

            assertTrue(exception.getMessage().contains("只有领养人可以提交领养申请"));
        }
    }

    @Test
    @DisplayName("测试提交申请-宠物不存在")
    void testApply_PetNotFound() {
        AdoptionApplyDTO applyDTO = new AdoptionApplyDTO();
        applyDTO.setPetId(999L);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);
            mockedUserContext.when(UserContext::getRoleType).thenReturn(RoleType.ADOPTER.getCode());

            when(petMapper.selectById(999L)).thenReturn(null);

            Exception exception = assertThrows(RuntimeException.class, () -> {
                adoptionService.apply(applyDTO);
            });

            assertTrue(exception.getMessage().contains("宠物不存在"));
        }
    }

    @Test
    @DisplayName("测试提交申请-宠物不可领养")
    void testApply_PetNotAvailable() {
        testPet.setStatus(PetStatus.ADOPTED.getCode());
        AdoptionApplyDTO applyDTO = new AdoptionApplyDTO();
        applyDTO.setPetId(1L);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);
            mockedUserContext.when(UserContext::getRoleType).thenReturn(RoleType.ADOPTER.getCode());

            when(petMapper.selectById(1L)).thenReturn(testPet);

            Exception exception = assertThrows(RuntimeException.class, () -> {
                adoptionService.apply(applyDTO);
            });

            assertTrue(exception.getMessage().contains("该宠物当前不可领养"));
        }
    }

    @Test
    @DisplayName("测试提交申请-重复申请")
    void testApply_DuplicateApplication() {
        AdoptionApplyDTO applyDTO = new AdoptionApplyDTO();
        applyDTO.setPetId(1L);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);
            mockedUserContext.when(UserContext::getRoleType).thenReturn(RoleType.ADOPTER.getCode());

            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(applicationMapper.selectCount(any())).thenReturn(1L);

            Exception exception = assertThrows(RuntimeException.class, () -> {
                adoptionService.apply(applyDTO);
            });

            assertTrue(exception.getMessage().contains("您已提交过该宠物的领养申请"));
        }
    }

    @Test
    @DisplayName("测试救助方初审通过")
    void testRescueReview_Approved() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setApplicationId(1L);
        reviewDTO.setStatus(1);
        reviewDTO.setComment("条件符合");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(2L);

            when(applicationMapper.selectById(1L)).thenReturn(testApplication);
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(applicationMapper.updateById(any(AdoptionApplication.class))).thenReturn(1);
            when(userMapper.selectById(1L)).thenReturn(testAdopter);
            doNothing().when(messageService).sendMessage(anyLong(), anyString(), anyString(), anyInt());

            assertDoesNotThrow(() -> adoptionService.rescueReview(reviewDTO));

            verify(applicationMapper, times(1)).updateById(any(AdoptionApplication.class));
            verify(messageService, atLeastOnce()).sendMessage(anyLong(), anyString(), anyString(), anyInt());
        }
    }

    @Test
    @DisplayName("测试救助方初审拒绝")
    void testRescueReview_Rejected() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setApplicationId(1L);
        reviewDTO.setStatus(2);
        reviewDTO.setComment("条件不符合");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(2L);

            when(applicationMapper.selectById(1L)).thenReturn(testApplication);
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(applicationMapper.updateById(any(AdoptionApplication.class))).thenReturn(1);
            when(petMapper.updateById(any(Pet.class))).thenReturn(1);
            doNothing().when(messageService).sendMessage(anyLong(), anyString(), anyString(), anyInt());

            assertDoesNotThrow(() -> adoptionService.rescueReview(reviewDTO));

            verify(applicationMapper, times(1)).updateById(any(AdoptionApplication.class));
            verify(petMapper, times(1)).updateById(any(Pet.class));
        }
    }

    @Test
    @DisplayName("测试救助方审核-无权审核")
    void testRescueReview_PermissionDenied() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setApplicationId(1L);
        reviewDTO.setStatus(1);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(3L);

            when(applicationMapper.selectById(1L)).thenReturn(testApplication);
            when(petMapper.selectById(1L)).thenReturn(testPet);

            Exception exception = assertThrows(RuntimeException.class, () -> {
                adoptionService.rescueReview(reviewDTO);
            });

            assertTrue(exception.getMessage().contains("无权审核此申请"));
        }
    }
}
