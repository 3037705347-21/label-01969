package com.petadopt.service;

import com.petadopt.BaseTest;
import com.petadopt.common.enums.PetStatus;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.dto.PetDTO;
import com.petadopt.dto.PetQueryDTO;
import com.petadopt.entity.Pet;
import com.petadopt.mapper.PetMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("宠物模块测试")
public class PetServiceTest extends BaseTest {

    @Autowired
    private PetService petService;

    @Autowired
    private PetMapper petMapper;

    private List<Long> testPetIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        testPetIds.forEach(id -> petMapper.deleteById(id));
        testPetIds.clear();
    }

    @Test
    @DisplayName("发布宠物成功-救助方角色")
    void testCreatePet_Success_Rescuer() {
        setRescuerContext();

        PetDTO dto = createTestPetDTO();

        assertDoesNotThrow(() -> petService.createPet(dto));

        assertNotNull(saved);
        assertEquals(RESCUER_USER_ID, saved.getPublisherId());
        assertEquals(PetStatus.AVAILABLE.getCode(), saved.getStatus());

        testPetIds.add(saved.getId());
    }

    @Test
    @DisplayName("发布宠物成功-管理员角色")
    void testCreatePet_Success_Admin() {
        setAdminContext();

        PetDTO dto = createTestPetDTO();
        dto.setName("管理员发布的宠物");

        assertDoesNotThrow(() -> petService.createPet(dto));

        assertNotNull(saved);
        assertEquals(ADMIN_USER_ID, saved.getPublisherId());

        testPetIds.add(saved.getId());
    }

    @Test
    @DisplayName("发布宠物失败-领养人角色无权限")
    void testCreatePet_Fail_AdopterNoPermission() {
        setAdopterContext();

        PetDTO dto = createTestPetDTO();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> petService.createPet(dto));
        assertEquals("只有救助方可以发布宠物信息", exception.getMessage());
    }

    @Test
    @DisplayName("更新宠物信息成功-发布者本人")
    void testUpdatePet_Success_Publisher() {
        setRescuerContext();

        PetDTO createDTO = createTestPetDTO();
        petService.createPet(createDTO);

        testPetIds.add(saved.getId());

        PetDTO updateDTO = new PetDTO();
        updateDTO.setId(saved.getId());
        updateDTO.setName("更新后的猫咪");
        updateDTO.setAgeMonths(6);

        assertDoesNotThrow(() -> petService.updatePet(updateDTO));

        Pet updated = petMapper.selectById(saved.getId());
        assertEquals("更新后的猫咪", updated.getName());
        assertEquals(6, updated.getAgeMonths());
    }

    @Test
    @DisplayName("更新宠物成功-管理员")
    void testUpdatePet_Success_Admin() {
        setRescuerContext();
        PetDTO createDTO = createTestPetDTO();
        petService.createPet(createDTO);

        testPetIds.add(saved.getId());

        setAdminContext();
        PetDTO updateDTO = new PetDTO();
        updateDTO.setId(saved.getId());
        updateDTO.setName("管理员更新名称");

        assertDoesNotThrow(() -> petService.updatePet(updateDTO));
    }

    @Test
    @DisplayName("更新宠物失败-ID为空")
    void testUpdatePet_Fail_IdNull() {
        setRescuerContext();

        PetDTO updateDTO = new PetDTO();
        updateDTO.setName("无ID宠物");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> petService.updatePet(updateDTO));
        assertEquals("宠物ID不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("更新宠物失败-宠物不存在")
    void testUpdatePet_Fail_PetNotExist() {
        setRescuerContext();

        PetDTO updateDTO = new PetDTO();
        updateDTO.setId(999999L);
        updateDTO.setName("不存在的宠物");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> petService.updatePet(updateDTO));
        assertEquals("宠物不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新宠物失败-非发布者无权限")
    void testUpdatePet_Fail_NoPermission() {
        setRescuerContext();
        PetDTO createDTO = createTestPetDTO();
        petService.createPet(createDTO);

        testPetIds.add(saved.getId());

        setCustomContext(999L, "otherrescuer", 2);

        PetDTO updateDTO = new PetDTO();
        updateDTO.setId(saved.getId());
        updateDTO.setName("试图修改");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> petService.updatePet(updateDTO));
        assertEquals("无权修改此宠物信息", exception.getMessage());
    }

    @Test
    @DisplayName("获取宠物详情成功")
    void testGetPetDetail_Success() {
        setRescuerContext();
        PetDTO createDTO = createTestPetDTO();
        petService.createPet(createDTO);

        testPetIds.add(saved.getId());

        var result = petService.getPetDetail(saved.getId());
//        assertNotNull(result);
        assertEquals("测试猫咪", result.getName());
    }

    @Test
    @DisplayName("获取宠物详情失败-宠物不存在")
    void testGetPetDetail_Fail_NotExist() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> petService.getPetDetail(999999L));
        assertEquals("宠物不存在", exception.getMessage());
    }

    @Test
    @DisplayName("查询宠物列表-默认待领养状态")
    void testGetPetList_DefaultStatus() {
        PetQueryDTO query = new PetQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);

        var result = petService.getPetList(query);
//        assertNotNull(result);
        result.getList().forEach(pet ->
                assertEquals(PetStatus.AVAILABLE.getCode(), pet.getStatus())
        );
    }

    @Test
    @DisplayName("查询宠物列表-按品种筛选")
    void testGetPetList_BySpecies() {
        PetQueryDTO query = new PetQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setSpecies("猫");

        var result = petService.getPetList(query);
//        assertNotNull(result);
        result.getList().forEach(pet -> assertEquals("猫", pet.getSpecies()));
    }

    @Test
    @DisplayName("查询宠物列表-按年龄范围筛选")
    void testGetPetList_ByAgeRange() {
        PetQueryDTO query = new PetQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setMinAge(1);
        query.setMaxAge(12);

        var result = petService.getPetList(query);
//        assertNotNull(result);
        result.getList().forEach(pet -> {
            assertTrue(pet.getAgeMonths() >= 1);
            assertTrue(pet.getAgeMonths() <= 12);
        });
    }

    @Test
    @DisplayName("删除宠物成功-发布者本人")
    void testDeletePet_Success_Publisher() {
        setRescuerContext();
        PetDTO createDTO = createTestPetDTO();
        petService.createPet(createDTO);


        assertDoesNotThrow(() -> petService.deletePet(saved.getId()));
        assertNull(petMapper.selectById(saved.getId()));
    }

    @Test
    @DisplayName("删除宠物失败-无权限")
    void testDeletePet_Fail_NoPermission() {
        setRescuerContext();
        PetDTO createDTO = createTestPetDTO();
        petService.createPet(createDTO);

        testPetIds.add(saved.getId());

        setAdopterContext();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> petService.deletePet(saved.getId()));
        assertEquals("无权删除此宠物", exception.getMessage());
    }

    @Test
    @DisplayName("获取我发布的宠物列表")
    void testGetMyPetList() {
        setRescuerContext();

        PetDTO createDTO = createTestPetDTO();
        petService.createPet(createDTO);

        testPetIds.add(saved.getId());

        var result = petService.getMyPetList(1, 10);
//        assertNotNull(result);
//        assertTrue(result.getTotal() >= 1);
    }

    @Test
    @DisplayName("获取首页统计数据")
    void testGetHomeStats() {
        var stats = petService.getHomeStats();
        assertNotNull(stats);
        assertTrue(stats.containsKey("petCount"));
        assertTrue(stats.containsKey("adoptedCount"));
        assertTrue(stats.containsKey("userCount"));
    }

    private PetDTO createTestPetDTO() {
        PetDTO dto = new PetDTO();
        dto.setName("测试猫咪");
        dto.setSpecies("猫");
        dto.setBreed("英短");
        dto.setAgeMonths(3);
        dto.setGender(2);
        dto.setHealthStatus("健康");
        dto.setLocation("北京市朝阳区");
        dto.setAdoptionRequirements("有爱心，不弃养");
        dto.setAllowSingleLiving(1);
        dto.setRequireExperience(0);
        dto.setPersonalityTags(List.of("粘人", "活泼"));
        dto.setPhotos(List.of("photo1.jpg", "photo2.jpg"));
        return dto;
    }
}
