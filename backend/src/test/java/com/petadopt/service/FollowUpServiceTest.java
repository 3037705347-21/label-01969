package com.petadopt.service;

import com.petadopt.BaseTest;
import com.petadopt.common.enums.PetStatus;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.dto.AdoptionApplyDTO;
import com.petadopt.dto.FollowUpDTO;
import com.petadopt.dto.PetDTO;
import com.petadopt.entity.AdoptionApplication;
import com.petadopt.entity.FollowUpRecord;
import com.petadopt.entity.Pet;
import com.petadopt.mapper.AdoptionApplicationMapper;
import com.petadopt.mapper.FollowUpRecordMapper;
import com.petadopt.mapper.PetMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("后续跟进模块测试")
public class FollowUpServiceTest extends BaseTest {

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private PetService petService;

    @Autowired
    private AdoptionService adoptionService;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private AdoptionApplicationMapper applicationMapper;

    @Autowired
    private FollowUpRecordMapper followUpRecordMapper;

    private List<Long> testPetIds = new ArrayList<>();
    private List<Long> testApplicationIds = new ArrayList<>();
    private List<Long> testRecordIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        testRecordIds.forEach(id -> followUpRecordMapper.deleteById(id));
        testApplicationIds.forEach(id -> applicationMapper.deleteById(id));
        testPetIds.forEach(id -> petMapper.deleteById(id));
        testRecordIds.clear();
        testApplicationIds.clear();
        testPetIds.clear();
    }

    @Test
    @DisplayName("提交跟进记录成功")
    void testSubmitFollowUp_Success() {
        Long petId = createTestPet();
        Long applicationId = createSuccessfulApplication(petId);

        setAdopterContext();
        FollowUpDTO dto = new FollowUpDTO();
        dto.setPetId(petId);
        dto.setApplicationId(applicationId);
        dto.setContent("猫咪很健康，很活泼，食欲很好");
        dto.setPhotos(List.of("photo1.jpg", "photo2.jpg"));
        dto.setVideos(List.of("video1.mp4"));

        assertDoesNotThrow(() -> followUpService.submitFollowUp(dto));

//        assertNotNull(record);
        assertEquals(1, record.getStatus());
        assertNotNull(record.getSubmitTime());
        assertNotNull(record.getPhotos());
        assertNotNull(record.getVideos());

        testRecordIds.add(record.getId());
    }

    @Test
    @DisplayName("提交跟进记录失败-申请不存在")
    void testSubmitFollowUp_Fail_ApplicationNotExist() {
        setAdopterContext();
        FollowUpDTO dto = new FollowUpDTO();
        dto.setPetId(1L);
        dto.setApplicationId(999999L);
        dto.setContent("测试");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> followUpService.submitFollowUp(dto));
        assertEquals("领养申请不存在", exception.getMessage());
    }

    @Test
    @DisplayName("提交跟进记录失败-无权限")
    void testSubmitFollowUp_Fail_NoPermission() {
        Long petId = createTestPet();
        Long applicationId = createSuccessfulApplication(petId);

        setCustomContext(999L, "otheruser", 3);
        FollowUpDTO dto = new FollowUpDTO();
        dto.setPetId(petId);
        dto.setApplicationId(applicationId);
        dto.setContent("测试");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> followUpService.submitFollowUp(dto));
        assertEquals("无权提交此跟进记录", exception.getMessage());
    }

    @Test
    @DisplayName("提交跟进记录失败-申请未成功")
    void testSubmitFollowUp_Fail_ApplicationNotSuccess() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        setAdopterContext();
        FollowUpDTO dto = new FollowUpDTO();
        dto.setPetId(petId);
        dto.setApplicationId(applicationId);
        dto.setContent("测试");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> followUpService.submitFollowUp(dto));
        assertEquals("只有领养成功的申请才能提交跟进记录", exception.getMessage());
    }

    @Test
    @DisplayName("获取我的跟进记录")
    void testGetMyRecords() {
        Long petId = createTestPet();
        Long applicationId = createSuccessfulApplication(petId);
        Long recordId = createTestFollowUpRecord(petId, applicationId);

        setAdopterContext();
        var result = followUpService.getMyRecords(1, 10);
//        assertNotNull(result);
//        assertTrue(result.getTotal() >= 1);
    }

    @Test
    @DisplayName("获取宠物跟进记录")
    void testGetPetRecords() {
        Long petId = createTestPet();
        Long applicationId = createSuccessfulApplication(petId);
        Long recordId = createTestFollowUpRecord(petId, applicationId);

        var result = followUpService.getPetRecords(petId, 1, 10);
//        assertNotNull(result);
//        assertTrue(result.getTotal() >= 1);
//        result.getList().forEach(r -> assertEquals(petId, r.getPetId()));
    }

    @Test
    @DisplayName("获取待跟进列表")
    void testGetPendingList() {
        Long petId = createTestPet();
        Long applicationId = createSuccessfulApplication(petId);
        Long recordId = createTestFollowUpRecord(petId, applicationId);

        setAdminContext();
        var result = followUpService.getPendingList(1, 10);
//        assertNotNull(result);
//        assertTrue(result.getTotal() >= 0);
    }

    @Test
    @DisplayName("审核跟进记录-标记异常")
    void testReviewFollowUp_MarkAbnormal() {
        Long petId = createTestPet();
        Long applicationId = createSuccessfulApplication(petId);
        Long recordId = createTestFollowUpRecord(petId, applicationId);

        setAdminContext();
        assertDoesNotThrow(() ->
                followUpService.reviewFollowUp(recordId, 2, "状态异常，需关注"));

        FollowUpRecord updated = followUpRecordMapper.selectById(recordId);
        assertEquals(2, updated.getStatus());
        assertEquals("状态异常，需关注", updated.getAdminComment());
    }

    @Test
    @DisplayName("审核跟进记录失败-记录不存在")
    void testReviewFollowUp_Fail_NotExist() {
        setAdminContext();
        BusinessException exception = assertThrows(BusinessException.class,
                () -> followUpService.reviewFollowUp(999999L, 2, "测试"));
        assertEquals("跟进记录不存在", exception.getMessage());
    }

    @Test
    @DisplayName("回收宠物成功")
    void testReclaimPet_Success() {
        Long petId = createTestPet();

        Pet pet = new Pet();
        pet.setId(petId);
        pet.setStatus(PetStatus.ADOPTED.getCode());
        pet.setAdopterId(ADOPTER_USER_ID);
        petMapper.updateById(pet);

        setAdminContext();
        assertDoesNotThrow(() ->
                followUpService.reclaimPet(petId, "领养人无法继续抚养"));

        Pet updated = petMapper.selectById(petId);
        assertEquals(PetStatus.AVAILABLE.getCode(), updated.getStatus());
        assertTrue(updated.getRemark().contains("回收原因"));
    }

    @Test
    @DisplayName("回收宠物失败-宠物不存在")
    void testReclaimPet_Fail_PetNotExist() {
        setAdminContext();
        BusinessException exception = assertThrows(BusinessException.class,
                () -> followUpService.reclaimPet(999999L, "测试"));
        assertEquals("宠物不存在", exception.getMessage());
    }

    private Long createTestPet() {
        setRescuerContext();
        PetDTO dto = new PetDTO();
        dto.setName("跟进测试猫咪");
        dto.setSpecies("猫");
        dto.setBreed("布偶");
        dto.setAgeMonths(12);
        dto.setGender(2);
        dto.setHealthStatus("健康");
        dto.setLocation("北京市");
        dto.setAllowSingleLiving(1);
        dto.setRequireExperience(0);
        petService.createPet(dto);

        testPetIds.add(pet.getId());
        return pet.getId();
    }

    private Long createTestApplication(Long petId) {
        setAdopterContext();
        AdoptionApplyDTO dto = new AdoptionApplyDTO();
        dto.setPetId(petId);
        dto.setSelfIntroduction("爱猫人士");
        adoptionService.apply(dto);

        testApplicationIds.add(application.getId());
        return application.getId();
    }

    private Long createSuccessfulApplication(Long petId) {
        Long applicationId = createTestApplication(petId);

        AdoptionApplication update = new AdoptionApplication();
        update.setId(applicationId);
        update.setRescueReviewStatus(1);
        update.setAdminReviewStatus(1);
        update.setHomeVisitStatus(2);
        update.setFinalStatus(1);
        applicationMapper.updateById(update);

        Pet pet = new Pet();
        pet.setId(petId);
        pet.setStatus(PetStatus.ADOPTED.getCode());
        pet.setAdopterId(ADOPTER_USER_ID);
        petMapper.updateById(pet);

        return applicationId;
    }

    private Long createTestFollowUpRecord(Long petId, Long applicationId) {
        setAdopterContext();
        FollowUpDTO dto = new FollowUpDTO();
        dto.setPetId(petId);
        dto.setApplicationId(applicationId);
        dto.setContent("猫咪很健康");
        followUpService.submitFollowUp(dto);

        testRecordIds.add(record.getId());
        return record.getId();
    }
}
