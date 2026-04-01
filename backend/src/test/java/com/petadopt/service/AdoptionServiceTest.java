package com.petadopt.service;

import com.petadopt.BaseTest;
import com.petadopt.common.enums.PetStatus;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.dto.AdoptionApplyDTO;
import com.petadopt.dto.PetDTO;
import com.petadopt.dto.ReviewDTO;
import com.petadopt.entity.AdoptionApplication;
import com.petadopt.entity.Pet;
import com.petadopt.mapper.AdoptionApplicationMapper;
import com.petadopt.mapper.PetMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("领养流程模块测试")
public class AdoptionServiceTest extends BaseTest {

    @Autowired
    private AdoptionService adoptionService;

    @Autowired
    private PetService petService;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private AdoptionApplicationMapper applicationMapper;

    private List<Long> testPetIds = new ArrayList<>();
    private List<Long> testApplicationIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        testApplicationIds.forEach(id -> applicationMapper.deleteById(id));
        testPetIds.forEach(id -> petMapper.deleteById(id));
        testApplicationIds.clear();
        testPetIds.clear();
    }

    @Test
    @DisplayName("提交领养申请成功")
    void testApply_Success() {
        Long petId = createTestPet();

        setAdopterContext();
        AdoptionApplyDTO dto = new AdoptionApplyDTO();
        dto.setPetId(petId);
        dto.setSelfIntroduction("爱猫人士，有养宠经验");
        dto.setResidenceProof("居住证明链接");
        dto.setIncomeProof("收入证明链接");

        assertDoesNotThrow(() -> adoptionService.apply(dto));

//        assertNotNull(application);
        assertEquals(0, application.getRescueReviewStatus());
        assertEquals(0, application.getAdminReviewStatus());
        assertEquals(0, application.getFinalStatus());

        Pet updatedPet = petMapper.selectById(petId);
        assertEquals(PetStatus.REVIEWING.getCode(), updatedPet.getStatus());

        testApplicationIds.add(application.getId());
    }

    @Test
    @DisplayName("提交领养申请失败-非领养人角色")
    void testApply_Fail_NotAdopter() {
        Long petId = createTestPet();

        setRescuerContext();
        AdoptionApplyDTO dto = new AdoptionApplyDTO();
        dto.setPetId(petId);
        dto.setSelfIntroduction("测试");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adoptionService.apply(dto));
        assertEquals("只有领养人可以提交领养申请", exception.getMessage());
    }

    @Test
    @DisplayName("提交领养申请失败-宠物不存在")
    void testApply_Fail_PetNotExist() {
        setAdopterContext();
        AdoptionApplyDTO dto = new AdoptionApplyDTO();
        dto.setPetId(999999L);
        dto.setSelfIntroduction("测试");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adoptionService.apply(dto));
        assertEquals("宠物不存在", exception.getMessage());
    }

    @Test
    @DisplayName("提交领养申请失败-宠物不可领养")
    void testApply_Fail_PetNotAvailable() {
        Long petId = createTestPet();

        Pet pet = new Pet();
        pet.setId(petId);
        pet.setStatus(PetStatus.ADOPTED.getCode());
        petMapper.updateById(pet);

        setAdopterContext();
        AdoptionApplyDTO dto = new AdoptionApplyDTO();
        dto.setPetId(petId);
        dto.setSelfIntroduction("测试");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adoptionService.apply(dto));
        assertEquals("该宠物当前不可领养", exception.getMessage());
    }

    @Test
    @DisplayName("提交领养申请失败-重复申请")
    void testApply_Fail_DuplicateApplication() {
        Long petId = createTestPet();

        setAdopterContext();
        AdoptionApplyDTO dto = new AdoptionApplyDTO();
        dto.setPetId(petId);
        dto.setSelfIntroduction("第一次申请");
        adoptionService.apply(dto);

        AdoptionApplyDTO dto2 = new AdoptionApplyDTO();
        dto2.setPetId(petId);
        dto2.setSelfIntroduction("重复申请");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adoptionService.apply(dto2));
        assertEquals("您已提交过该宠物的领养申请", exception.getMessage());

        testApplicationIds.add(application.getId());
    }

    @Test
    @DisplayName("救助方审核通过")
    void testRescueReview_Approve() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        setRescuerContext();
        ReviewDTO dto = new ReviewDTO();
        dto.setApplicationId(applicationId);
        dto.setStatus(1);
        dto.setComment("条件符合，初审通过");

        assertDoesNotThrow(() -> adoptionService.rescueReview(dto));

        AdoptionApplication updated = applicationMapper.selectById(applicationId);
        assertEquals(1, updated.getRescueReviewStatus());
        assertEquals("条件符合，初审通过", updated.getRescueReviewComment());
        assertNotNull(updated.getRescueReviewTime());
    }

    @Test
    @DisplayName("救助方审核拒绝")
    void testRescueReview_Reject() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        setRescuerContext();
        ReviewDTO dto = new ReviewDTO();
        dto.setApplicationId(applicationId);
        dto.setStatus(2);
        dto.setComment("不符合领养条件");

        assertDoesNotThrow(() -> adoptionService.rescueReview(dto));

        AdoptionApplication updated = applicationMapper.selectById(applicationId);
        assertEquals(2, updated.getRescueReviewStatus());
        assertEquals(2, updated.getFinalStatus());

        Pet pet = petMapper.selectById(petId);
        assertEquals(PetStatus.AVAILABLE.getCode(), pet.getStatus());
    }

    @Test
    @DisplayName("救助方审核失败-无权限")
    void testRescueReview_Fail_NoPermission() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        setCustomContext(999L, "otherrescuer", 2);
        ReviewDTO dto = new ReviewDTO();
        dto.setApplicationId(applicationId);
        dto.setStatus(1);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adoptionService.rescueReview(dto));
        assertEquals("无权审核此申请", exception.getMessage());
    }

    @Test
    @DisplayName("管理员复核通过")
    void testAdminReview_Approve() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        AdoptionApplication update = new AdoptionApplication();
        update.setId(applicationId);
        update.setRescueReviewStatus(1);
        applicationMapper.updateById(update);

        setAdminContext();
        ReviewDTO dto = new ReviewDTO();
        dto.setApplicationId(applicationId);
        dto.setStatus(1);
        dto.setComment("资质审核通过");

        assertDoesNotThrow(() -> adoptionService.adminReview(dto));

        AdoptionApplication updated = applicationMapper.selectById(applicationId);
        assertEquals(1, updated.getAdminReviewStatus());
        assertEquals("资质审核通过", updated.getAdminReviewComment());
    }

    @Test
    @DisplayName("管理员复核拒绝")
    void testAdminReview_Reject() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        AdoptionApplication update = new AdoptionApplication();
        update.setId(applicationId);
        update.setRescueReviewStatus(1);
        applicationMapper.updateById(update);

        setAdminContext();
        ReviewDTO dto = new ReviewDTO();
        dto.setApplicationId(applicationId);
        dto.setStatus(2);
        dto.setComment("资质存疑");

        assertDoesNotThrow(() -> adoptionService.adminReview(dto));

        AdoptionApplication updated = applicationMapper.selectById(applicationId);
        assertEquals(2, updated.getAdminReviewStatus());
        assertEquals(2, updated.getFinalStatus());

        Pet pet = petMapper.selectById(petId);
        assertEquals(PetStatus.AVAILABLE.getCode(), pet.getStatus());
    }

    @Test
    @DisplayName("管理员复核失败-未通过救助方审核")
    void testAdminReview_Fail_NotRescueApproved() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        setAdminContext();
        ReviewDTO dto = new ReviewDTO();
        dto.setApplicationId(applicationId);
        dto.setStatus(1);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adoptionService.adminReview(dto));
        assertEquals("该申请尚未通过救助方审核", exception.getMessage());
    }

    @Test
    @DisplayName("家访通过-领养成功")
    void testHomeVisit_Pass() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        AdoptionApplication update = new AdoptionApplication();
        update.setId(applicationId);
        update.setRescueReviewStatus(1);
        update.setAdminReviewStatus(1);
        applicationMapper.updateById(update);

        setAdminContext();
        ReviewDTO dto = new ReviewDTO();
        dto.setApplicationId(applicationId);
        dto.setStatus(2);
        dto.setComment("家访通过，环境良好");

        assertDoesNotThrow(() -> adoptionService.updateHomeVisit(dto));

        AdoptionApplication updated = applicationMapper.selectById(applicationId);
        assertEquals(2, updated.getHomeVisitStatus());
        assertEquals(1, updated.getFinalStatus());

        Pet pet = petMapper.selectById(petId);
        assertEquals(PetStatus.ADOPTED.getCode(), pet.getStatus());
        assertEquals(ADOPTER_USER_ID, pet.getAdopterId());
    }

    @Test
    @DisplayName("家访不通过")
    void testHomeVisit_Fail() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        AdoptionApplication update = new AdoptionApplication();
        update.setId(applicationId);
        update.setRescueReviewStatus(1);
        update.setAdminReviewStatus(1);
        applicationMapper.updateById(update);

        setAdminContext();
        ReviewDTO dto = new ReviewDTO();
        dto.setApplicationId(applicationId);
        dto.setStatus(3);
        dto.setComment("居住环境不符合要求");

        assertDoesNotThrow(() -> adoptionService.updateHomeVisit(dto));

        AdoptionApplication updated = applicationMapper.selectById(applicationId);
        assertEquals(3, updated.getHomeVisitStatus());
        assertEquals(2, updated.getFinalStatus());

        Pet pet = petMapper.selectById(petId);
        assertEquals(PetStatus.AVAILABLE.getCode(), pet.getStatus());
    }

    @Test
    @DisplayName("获取我的申请列表")
    void testGetMyApplications() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        setAdopterContext();
        var result = adoptionService.getMyApplications(1, 10);
//        assertNotNull(result);
//        assertTrue(result.getTotal() >= 1);
    }

    @Test
    @DisplayName("救助方获取收到的申请")
    void testGetReceivedApplications() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        setRescuerContext();
        var result = adoptionService.getReceivedApplications(null, 1, 10);
//        assertNotNull(result);
//        assertTrue(result.getTotal() >= 1);
    }

    @Test
    @DisplayName("管理员获取待复核列表")
    void testGetPendingReviewList() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        AdoptionApplication update = new AdoptionApplication();
        update.setId(applicationId);
        update.setRescueReviewStatus(1);
        applicationMapper.updateById(update);

        setAdminContext();
        var result = adoptionService.getPendingReviewList(1, 10);
//        assertNotNull(result);
//        assertTrue(result.getTotal() >= 0);
    }

    @Test
    @DisplayName("获取申请详情")
    void testGetApplicationDetail() {
        Long petId = createTestPet();
        Long applicationId = createTestApplication(petId);

        var result = adoptionService.getApplicationDetail(applicationId);
//        assertNotNull(result);
        assertNotNull(result.getPetName());
    }

    private Long createTestPet() {
        setRescuerContext();
        PetDTO dto = new PetDTO();
        dto.setName("领养测试猫咪");
        dto.setSpecies("猫");
        dto.setBreed("美短");
        dto.setAgeMonths(6);
        dto.setGender(1);
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
}
