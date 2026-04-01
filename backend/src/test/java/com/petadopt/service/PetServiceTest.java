package com.petadopt.service;

import com.petadopt.common.enums.PetStatus;
import com.petadopt.common.enums.RoleType;
import com.petadopt.dto.PetDTO;
import com.petadopt.entity.Pet;
import com.petadopt.entity.User;
import com.petadopt.mapper.PetMapper;
import com.petadopt.mapper.UserMapper;
import com.petadopt.service.impl.PetServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("宠物服务测试")
class PetServiceTest {

    @Mock
    private PetMapper petMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private PetServiceImpl petService;

    private Pet testPet;
    private User testRescuer;

    @BeforeEach
    void setUp() {
        testRescuer = new User();
        testRescuer.setId(1L);
        testRescuer.setUsername("rescuer");
        testRescuer.setRoleType(RoleType.RESCUER.getCode());

        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("小白");
        testPet.setSpecies("狗");
        testPet.setBreed("金毛");
        testPet.setAgeMonths(12);
        testPet.setGender(1);
        testPet.setPublisherId(1L);
        testPet.setStatus(PetStatus.AVAILABLE.getCode());
    }

    @Test
    @DisplayName("测试救助方发布宠物成功")
    void testCreatePet_Success() {
        PetDTO petDTO = new PetDTO();
        petDTO.setName("小白");
        petDTO.setSpecies("狗");
        petDTO.setBreed("金毛");
        petDTO.setAgeMonths(12);
        petDTO.setGender(1);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);
            mockedUserContext.when(UserContext::getRoleType).thenReturn(RoleType.RESCUER.getCode());

            when(petMapper.insert(any(Pet.class))).thenAnswer(invocation -> {
                Pet pet = invocation.getArgument(0);
                pet.setId(1L);
                return 1;
            });

            assertDoesNotThrow(() -> petService.createPet(petDTO));

            verify(petMapper, times(1)).insert(any(Pet.class));
        }
    }

    @Test
    @DisplayName("测试管理员发布宠物成功")
    void testCreatePet_AdminSuccess() {
        PetDTO petDTO = new PetDTO();
        petDTO.setName("喵喵");
        petDTO.setSpecies("猫");
        petDTO.setBreed("英短");
        petDTO.setAgeMonths(6);
        petDTO.setGender(2);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(99L);
            mockedUserContext.when(UserContext::getRoleType).thenReturn(RoleType.ADMIN.getCode());

            when(petMapper.insert(any(Pet.class))).thenAnswer(invocation -> {
                Pet pet = invocation.getArgument(0);
                pet.setId(2L);
                return 1;
            });

            assertDoesNotThrow(() -> petService.createPet(petDTO));

            verify(petMapper, times(1)).insert(any(Pet.class));
        }
    }

    @Test
    @DisplayName("测试领养人尝试发布宠物-权限不足")
    void testCreatePet_PermissionDenied() {
        PetDTO petDTO = new PetDTO();
        petDTO.setName("测试");
        petDTO.setSpecies("狗");
        petDTO.setBreed("土狗");
        petDTO.setAgeMonths(3);
        petDTO.setGender(1);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(2L);
            mockedUserContext.when(UserContext::getRoleType).thenReturn(RoleType.ADOPTER.getCode());

            Exception exception = assertThrows(RuntimeException.class, () -> {
                petService.createPet(petDTO);
            });

            assertTrue(exception.getMessage().contains("只有救助方可以发布宠物信息"));
        }
    }

    @Test
    @DisplayName("测试获取宠物详情成功")
    void testGetPetDetail_Success() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userMapper.selectById(1L)).thenReturn(testRescuer);

        var result = petService.getPetDetail(1L);

        assertNotNull(result);
        assertEquals("小白", result.getName());
        verify(petMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("测试获取宠物详情-宠物不存在")
    void testGetPetDetail_NotFound() {
        when(petMapper.selectById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            petService.getPetDetail(999L);
        });

        assertTrue(exception.getMessage().contains("宠物不存在"));
    }

    @Test
    @DisplayName("测试救助方更新自己的宠物成功")
    void testUpdatePet_Success() {
        PetDTO petDTO = new PetDTO();
        petDTO.setId(1L);
        petDTO.setName("小白更新");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);
            mockedUserContext.when(UserContext::getRoleType).thenReturn(RoleType.RESCUER.getCode());

            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(petMapper.updateById(any(Pet.class))).thenReturn(1);

            assertDoesNotThrow(() -> petService.updatePet(petDTO));

            verify(petMapper, times(1)).updateById(any(Pet.class));
        }
    }

    @Test
    @DisplayName("测试更新宠物-宠物不存在")
    void testUpdatePet_NotFound() {
        PetDTO petDTO = new PetDTO();
        petDTO.setId(999L);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(1L);

            when(petMapper.selectById(999L)).thenReturn(null);

            Exception exception = assertThrows(RuntimeException.class, () -> {
                petService.updatePet(petDTO);
            });

            assertTrue(exception.getMessage().contains("宠物不存在"));
        }
    }

    @Test
    @DisplayName("测试删除宠物-权限不足")
    void testDeletePet_PermissionDenied() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(2L);
            mockedUserContext.when(UserContext::getRoleType).thenReturn(RoleType.RESCUER.getCode());

            when(petMapper.selectById(1L)).thenReturn(testPet);

            Exception exception = assertThrows(RuntimeException.class, () -> {
                petService.deletePet(1L);
            });

            assertTrue(exception.getMessage().contains("无权删除此宠物"));
        }
    }
}
