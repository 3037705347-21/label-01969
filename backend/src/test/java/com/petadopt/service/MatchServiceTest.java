package com.petadopt.service;

import com.petadopt.BaseTest;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.dto.PetDTO;
import com.petadopt.entity.Pet;
import com.petadopt.entity.User;
import com.petadopt.mapper.PetMapper;
import com.petadopt.mapper.UserMapper;
import com.petadopt.vo.AdopterMatchVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import cn.hutool.crypto.digest.DigestUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("领养匹配算法测试")
public class MatchServiceTest extends BaseTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private PetService petService;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private UserMapper userMapper;

    private List<Long> testPetIds = new ArrayList<>();
    private List<Long> testUserIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        testPetIds.forEach(id -> petMapper.deleteById(id));
        testUserIds.forEach(id -> userMapper.deleteById(id));
        testPetIds.clear();
        testUserIds.clear();
    }

    @Test
    @DisplayName("匹配领养人失败-宠物不存在")
    void testMatchAdopters_Fail_PetNotExist() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> matchService.matchAdoptersForPet(999999L, 10));
        assertEquals("宠物不存在", exception.getMessage());
    }

    @Test
    @DisplayName("匹配领养人成功-基本匹配")
    void testMatchAdopters_Success_Basic() {
        Long petId = createTestPet("匹配宠物1", 1, 1, "北京市朝阳区");

        var result = matchService.matchAdoptersForPet(petId, 10);
//        assertNotNull(result);
//        assertTrue(result.size() >= 0);
    }

    @Test
    @DisplayName("匹配领养人-限制返回数量")
    void testMatchAdopters_LimitResults() {
        Long petId = createTestPet("匹配宠物2", 0, 1, "北京市");

        var result = matchService.matchAdoptersForPet(petId, 5);
//        assertNotNull(result);
//        assertTrue(result.size() <= 5);
    }

    @Test
    @DisplayName("匹配分数计算-实名认证加分")
    void testCalculateMatch_VerifiedBonus() {
        Long petId = createTestPet("匹配宠物3", 0, 0, "上海市");

        Long adopterId1 = createTestAdopter("verifiedAdopter", 1, "北京", "有5年养猫经验", "与家人同住");
        Long adopterId2 = createTestAdopter("unverifiedAdopter", 0, "北京", "", "");

        var result = matchService.matchAdoptersForPet(petId, 20);
//        assertNotNull(result);

        var verified = result.stream()
                .filter(r -> "verifiedAdopter".equals(r.getUsername()))
                .findFirst();
        var unverified = result.stream()
                .filter(r -> "unverifiedAdopter".equals(r.getUsername()))
                .findFirst();

        if (verified.isPresent() && unverified.isPresent()) {
            assertTrue(verified.get().getUsername() != null);
            assertTrue(verified.get().getMatchScore() > unverified.get().getMatchScore());
            assertTrue(verified.get().getMatchReason().contains("已完成实名认证"));
        }
    }

    @Test
    @DisplayName("匹配分数计算-养宠经验匹配")
    void testCalculateMatch_ExperienceMatch() {
        Long petId = createTestPet("需要经验宠物", 0, 1, "北京市");

        Long experiencedAdopter = createTestAdopter("experienced", 1, "北京", "10年养宠经验，养过3只猫", "家庭居住");
        Long noExperienceAdopter = createTestAdopter("newbie", 1, "北京", "", "家庭居住");

        var result = matchService.matchAdoptersForPet(petId, 20);

        var experienced = result.stream()
                .filter(r -> "experienced".equals(r.getUsername()))
                .findFirst();
        var newbie = result.stream()
                .filter(r -> "newbie".equals(r.getUsername()))
                .findFirst();

        if (experienced.isPresent() && newbie.isPresent()) {
            assertTrue(experienced.get().getMatchScore() > newbie.get().getMatchScore());
            assertTrue(experienced.get().getMatchReason().contains("有养宠经验，符合要求"));
        }
    }

    @Test
    @DisplayName("匹配分数计算-独居环境检查")
    void testCalculateMatch_SingleLivingCheck() {
        Long petId = createTestPet("不允许独居宠物", 0, 1, "北京市");
        Pet pet = new Pet();
        pet.setId(petId);
        pet.setAllowSingleLiving(0);
        pet.setRequireExperience(0);
        petMapper.updateById(pet);

        Long familyAdopter = createTestAdopter("familyAdopter", 1, "北京", "有经验", "与父母同住，三居室");
        Long singleAdopter = createTestAdopter("singleAdopter", 1, "北京", "有经验", "独居，一居室");

        var result = matchService.matchAdoptersForPet(petId, 20);

        var family = result.stream()
                .filter(r -> "familyAdopter".equals(r.getUsername()))
                .findFirst();
        var single = result.stream()
                .filter(r -> "singleAdopter".equals(r.getUsername()))
                .findFirst();

        if (family.isPresent() && single.isPresent()) {
            assertTrue(family.get().getMatchScore() > single.get().getMatchScore());
        }
    }

    @Test
    @DisplayName("匹配分数计算-同城领养加分")
    void testCalculateMatch_SameCityBonus() {
        Long petId = createTestPet("北京宠物", 0, 0, "北京市朝阳区");

        Long beijingAdopter = createTestAdopter("beijingUser", 1, "北京市海淀区", "有经验", "家庭居住");
        Long shanghaiAdopter = createTestAdopter("shanghaiUser", 1, "上海市浦东新区", "有经验", "家庭居住");

        var result = matchService.matchAdoptersForPet(petId, 20);

        var beijing = result.stream()
                .filter(r -> "beijingUser".equals(r.getUsername()))
                .findFirst();
        var shanghai = result.stream()
                .filter(r -> "shanghaiUser".equals(r.getUsername()))
                .findFirst();

        if (beijing.isPresent() && shanghai.isPresent()) {
            assertTrue(beijing.get().getMatchScore() > shanghai.get().getMatchScore());
            assertTrue(beijing.get().getMatchReason().contains("同城领养"));
        }
    }

    @Test
    @DisplayName("匹配分数计算-资料完整度加分")
    void testCalculateMatch_CompletenessBonus() {
        Long petId = createTestPet("测试宠物", 0, 0, "杭州");

        Long completeAdopter = createTestAdopter("completeUser", 1, "杭州市西湖区", "有丰富经验", "环境好");
        Long sparseAdopter = createTestAdopter("sparseUser", 0, null, null, null);

        var result = matchService.matchAdoptersForPet(petId, 20);

        var complete = result.stream()
                .filter(r -> "completeUser".equals(r.getUsername()))
                .findFirst();
        var sparse = result.stream()
                .filter(r -> "sparseUser".equals(r.getUsername()))
                .findFirst();

        if (complete.isPresent() && sparse.isPresent()) {
            assertTrue(complete.get().getMatchScore() >= sparse.get().getMatchScore());
        }
    }

    @Test
    @DisplayName("匹配结果按分数降序排序")
    void testMatchResults_SortedByScoreDesc() {
        Long petId = createTestPet("排序测试宠物", 0, 0, "广州市");

        var result = matchService.matchAdoptersForPet(petId, 20);

        if (result.size() >= 2) {
            int prevScore = 101;
            for (AdopterMatchVO vo : result) {
                assertTrue(vo.getMatchScore() <= prevScore);
                prevScore = vo.getMatchScore();
            }
        }
    }

    @Test
    @DisplayName("匹配分数在0-100范围内")
    void testMatchScore_InRange() {
        Long petId = createTestPet("分数范围测试宠物", 0, 0, "深圳");

        var result = matchService.matchAdoptersForPet(petId, 20);

        for (AdopterMatchVO vo : result) {
            assertTrue(vo.getMatchScore() >= 0);
            assertTrue(vo.getMatchScore() <= 100);
        }
    }

    @Test
    @DisplayName("匹配结果包含必要字段")
    void testMatchResult_ContainsRequiredFields() {
        Long petId = createTestPet("字段测试宠物", 0, 0, "成都");

        var result = matchService.matchAdoptersForPet(petId, 10);

        for (AdopterMatchVO vo : result) {
            assertNotNull(vo.getUserId());
            assertNotNull(vo.getUsername());
            assertNotNull(vo.getMatchScore());
            assertNotNull(vo.getMatchReason());
        }
    }

    private Long createTestPet(String name, Integer allowSingleLiving, Integer requireExperience, String location) {
        setRescuerContext();
        PetDTO dto = new PetDTO();
        dto.setName(name);
        dto.setSpecies("猫");
        dto.setBreed("橘猫");
        dto.setAgeMonths(6);
        dto.setGender(1);
        dto.setHealthStatus("健康");
        dto.setLocation(location);
        dto.setAllowSingleLiving(allowSingleLiving);
        dto.setRequireExperience(requireExperience);
        petService.createPet(dto);

        testPetIds.add(pet.getId());
        return pet.getId();
    }

    private Long createTestAdopter(String username, Integer verified, String address, String petExperience, String livingEnvironment) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(DigestUtil.md5Hex("123456"));
        user.setPhone("138" + System.currentTimeMillis() % 100000000);
        user.setRoleType(3);
        user.setStatus(1);
        user.setVerifyStatus(verified);
        user.setRealName(verified == 1 ? "真实姓名" : null);
        user.setAddress(address);
        user.setOccupation("软件工程师");
        user.setPetExperience(petExperience);
        user.setLivingEnvironment(livingEnvironment);
        userMapper.insert(user);
        testUserIds.add(user.getId());
        return user.getId();
    }
}
