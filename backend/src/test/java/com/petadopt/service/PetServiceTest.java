package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadopt.BaseServiceTest;
import com.petadopt.common.enums.PetStatus;
import com.petadopt.common.enums.RoleType;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.dto.PetDTO;
import com.petadopt.dto.PetQueryDTO;
import com.petadopt.entity.Pet;
import com.petadopt.entity.User;
import com.petadopt.mapper.PetMapper;
import com.petadopt.mapper.UserMapper;
import com.petadopt.service.impl.PetServiceImpl;
import com.petadopt.util.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PetServiceTest extends BaseServiceTest {

    @Mock
    private PetMapper petMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private PetServiceImpl petService;

    @BeforeEach
    void setUpBase() {
        setBaseMapper(petService, petMapper);
    }

    private Pet testPet;
    private PetDTO petDTO;
    private User rescuerUser;

    @BeforeEach
    void setUp() {
        rescuerUser = new User();
        rescuerUser.setId(1L);
        rescuerUser.setRoleType(RoleType.RESCUER.getCode());
        rescuerUser.setOrgName("测试救助站");

        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("测试猫咪");
        testPet.setPublisherId(1L);
        testPet.setStatus(PetStatus.AVAILABLE.getCode());
        testPet.setSpecies("猫");
        testPet.setBreed("橘猫");
        testPet.setAgeMonths(12);
        testPet.setGender(1);
        testPet.setLocation("北京市朝阳区");

        petDTO = new PetDTO();
        petDTO.setName("测试猫咪");
        petDTO.setSpecies("猫");
        petDTO.setBreed("橘猫");
        petDTO.setAgeMonths(12);
        petDTO.setGender(1);
        petDTO.setLocation("北京市朝阳区");
        petDTO.setPersonalityTags(Arrays.asList("粘人", "活泼"));
        petDTO.setPhotos(Arrays.asList("/uploads/photo1.jpg"));
    }

    @Test
    void testCreatePet_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.RESCUER.getCode());
            when(petMapper.insert(any(Pet.class))).thenReturn(1);

            assertDoesNotThrow(() -> {
                petService.createPet(petDTO);
            });

            verify(petMapper, times(1)).insert(any(Pet.class));
        }
    }

    @Test
    void testCreatePet_PermissionDenied() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(2L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.ADOPTER.getCode());

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                petService.createPet(petDTO);
            });

            assertEquals("只有救助方可以发布宠物信息", exception.getMessage());
        }
    }

    @Test
    void testCreatePet_AdminCanCreate() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(99L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.ADMIN.getCode());
            when(petMapper.insert(any(Pet.class))).thenReturn(1);

            assertDoesNotThrow(() -> {
                petService.createPet(petDTO);
            });

            verify(petMapper, times(1)).insert(any(Pet.class));
        }
    }

    @Test
    void testUpdatePet_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.RESCUER.getCode());
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(petMapper.updateById(any(Pet.class))).thenReturn(1);

            petDTO.setId(1L);
            petDTO.setName("更新后的名字");

            assertDoesNotThrow(() -> {
                petService.updatePet(petDTO);
            });

            verify(petMapper, times(1)).updateById(any(Pet.class));
        }
    }

    @Test
    void testUpdatePet_IdIsNull() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                petService.updatePet(petDTO);
            });

            assertEquals("宠物ID不能为空", exception.getMessage());
        }
    }

    @Test
    void testUpdatePet_NotExist() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            when(petMapper.selectById(999L)).thenReturn(null);

            petDTO.setId(999L);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                petService.updatePet(petDTO);
            });

            assertEquals("宠物不存在", exception.getMessage());
        }
    }

    @Test
    void testUpdatePet_PermissionDenied() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(2L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.RESCUER.getCode());
            when(petMapper.selectById(1L)).thenReturn(testPet);

            petDTO.setId(1L);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                petService.updatePet(petDTO);
            });

            assertEquals("无权修改此宠物信息", exception.getMessage());
        }
    }

    @Test
    void testUpdatePet_AdminCanUpdate() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(99L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.ADMIN.getCode());
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(petMapper.updateById(any(Pet.class))).thenReturn(1);

            petDTO.setId(1L);

            assertDoesNotThrow(() -> {
                petService.updatePet(petDTO);
            });

            verify(petMapper, times(1)).updateById(any(Pet.class));
        }
    }

    @Test
    void testGetPetDetail_Success() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userMapper.selectById(1L)).thenReturn(rescuerUser);

        var result = petService.getPetDetail(1L);

        assertNotNull(result);
        assertEquals("测试猫咪", result.getName());
        assertEquals("测试救助站", result.getPublisherName());
        assertEquals("1岁", result.getAgeDisplay());
        assertEquals("公", result.getGenderDisplay());
        assertEquals("待领养", result.getStatusDisplay());
    }

    @Test
    void testGetPetDetail_NotExist() {
        when(petMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            petService.getPetDetail(999L);
        });

        assertEquals("宠物不存在", exception.getMessage());
    }

    @Test
    void testGetPetList_WithFilters() {
        PetQueryDTO query = new PetQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setSpecies("猫");
        query.setBreed("橘猫");
        query.setMinAge(6);
        query.setMaxAge(24);
        query.setGender(1);
        query.setLocation("北京");

        Page<Pet> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.singletonList(testPet));
        pageResult.setTotal(1L);

        when(petMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
        when(userMapper.selectById(1L)).thenReturn(rescuerUser);

        var result = petService.getPetList(query);

        assertNotNull(result);
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getList().size());
    }

    @Test
    void testUpdatePetStatus_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.RESCUER.getCode());
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(petMapper.updateById(any(Pet.class))).thenReturn(1);

            assertDoesNotThrow(() -> {
                petService.updatePetStatus(1L, PetStatus.ADOPTED.getCode());
            });

            verify(petMapper, times(1)).updateById(any(Pet.class));
        }
    }

    @Test
    void testDeletePet_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.RESCUER.getCode());
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(petMapper.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> {
                petService.deletePet(1L);
            });

            verify(petMapper, times(1)).deleteById(1L);
        }
    }

    @Test
    void testDeletePet_PermissionDenied() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(2L);
            mocked.when(UserContext::getRoleType).thenReturn(RoleType.RESCUER.getCode());
            when(petMapper.selectById(1L)).thenReturn(testPet);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                petService.deletePet(1L);
            });

            assertEquals("无权删除此宠物", exception.getMessage());
        }
    }

    @Test
    void testGetMyPetList_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);

            Page<Pet> pageResult = new Page<>(1, 10);
            pageResult.setRecords(Collections.singletonList(testPet));
            pageResult.setTotal(1L);

            when(petMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
            when(userMapper.selectById(1L)).thenReturn(rescuerUser);

            var result = petService.getMyPetList(1, 10);

            assertNotNull(result);
            assertEquals(1L, result.getTotal());
        }
    }

    @Test
    void testGetHomeStats_Success() {
        when(petMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L, 5L);
        when(userMapper.selectCount(null)).thenReturn(50L);

        var stats = petService.getHomeStats();

        assertNotNull(stats);
        assertEquals(10L, stats.get("petCount"));
        assertEquals(5L, stats.get("adoptedCount"));
        assertEquals(50L, stats.get("userCount"));
    }
}
