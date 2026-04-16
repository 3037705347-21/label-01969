package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.entity.Pet;
import com.petadopt.entity.User;
import com.petadopt.mapper.PetMapper;
import com.petadopt.mapper.UserMapper;
import com.petadopt.service.impl.MatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private PetMapper petMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private MatchServiceImpl matchService;

    private Pet testPet;
    private User verifiedAdopter;
    private User unverifiedAdopter;
    private User experiencedAdopter;
    private User sameCityAdopter;

    @BeforeEach
    void setUp() {
        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("测试猫咪");
        testPet.setRequireExperience(1);
        testPet.setAllowSingleLiving(0);
        testPet.setLocation("北京市朝阳区");

        verifiedAdopter = new User();
        verifiedAdopter.setId(1L);
        verifiedAdopter.setUsername("verified_user");
        verifiedAdopter.setRealName("已认证用户");
        verifiedAdopter.setPhone("13800138000");
        verifiedAdopter.setAddress("北京市海淀区");
        verifiedAdopter.setOccupation("软件工程师");
        verifiedAdopter.setPetExperience("有5年养猫经验");
        verifiedAdopter.setLivingEnvironment("与家人同住，两室一厅");
        verifiedAdopter.setVerifyStatus(1);
        verifiedAdopter.setRoleType(3);
        verifiedAdopter.setStatus(1);
        verifiedAdopter.setDeleted(0);

        unverifiedAdopter = new User();
        unverifiedAdopter.setId(2L);
        unverifiedAdopter.setUsername("unverified_user");
        unverifiedAdopter.setVerifyStatus(0);
        unverifiedAdopter.setRoleType(3);
        unverifiedAdopter.setStatus(1);
        unverifiedAdopter.setDeleted(0);

        experiencedAdopter = new User();
        experiencedAdopter.setId(3L);
        experiencedAdopter.setUsername("experienced_user");
        experiencedAdopter.setPetExperience("有10年养宠经验，养过3只猫");
        experiencedAdopter.setVerifyStatus(1);
        experiencedAdopter.setRoleType(3);
        experiencedAdopter.setStatus(1);
        experiencedAdopter.setDeleted(0);

        sameCityAdopter = new User();
        sameCityAdopter.setId(4L);
        sameCityAdopter.setUsername("samecity_user");
        sameCityAdopter.setAddress("北京市朝阳区望京");
        sameCityAdopter.setVerifyStatus(1);
        sameCityAdopter.setRoleType(3);
        sameCityAdopter.setStatus(1);
        sameCityAdopter.setDeleted(0);
    }

    @Test
    void testMatchAdoptersForPet_Success() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
            Arrays.asList(verifiedAdopter, unverifiedAdopter, experiencedAdopter, sameCityAdopter)
        );

        var results = matchService.matchAdoptersForPet(1L, 10);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.size() <= 4);

        var firstMatch = results.get(0);
        assertTrue(firstMatch.getMatchScore() >= 0 && firstMatch.getMatchScore() <= 100);
        assertNotNull(firstMatch.getMatchReason());
    }

    @Test
    void testMatchAdoptersForPet_PetNotExist() {
        when(petMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            matchService.matchAdoptersForPet(999L, 10);
        });

        assertEquals("宠物不存在", exception.getMessage());
    }

    @Test
    void testMatchAdoptersForPet_NoAdopters() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        var results = matchService.matchAdoptersForPet(1L, 10);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testMatchAdoptersForPet_LimitResults() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
            Arrays.asList(verifiedAdopter, unverifiedAdopter, experiencedAdopter, sameCityAdopter)
        );

        var results = matchService.matchAdoptersForPet(1L, 2);

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testMatchAdoptersForPet_VerifiedScoresHigher() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
            Arrays.asList(verifiedAdopter, unverifiedAdopter)
        );

        var results = matchService.matchAdoptersForPet(1L, 10);

        var verifiedResult = results.stream()
            .filter(r -> r.getUserId().equals(1L))
            .findFirst()
            .orElseThrow();

        var unverifiedResult = results.stream()
            .filter(r -> r.getUserId().equals(2L))
            .findFirst()
            .orElseThrow();

        assertTrue(verifiedResult.getMatchScore() > unverifiedResult.getMatchScore());
    }

    @Test
    void testMatchAdoptersForPet_ExperienceMatching() {
        testPet.setRequireExperience(1);
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
            Arrays.asList(experiencedAdopter, unverifiedAdopter)
        );

        var results = matchService.matchAdoptersForPet(1L, 10);

        var experiencedResult = results.stream()
            .filter(r -> r.getUserId().equals(3L))
            .findFirst()
            .orElseThrow();

        assertTrue(experiencedResult.getMatchReason().contains("有养宠经验"));
    }

    @Test
    void testMatchAdoptersForPet_SameCityBonus() {
        testPet.setLocation("北京市朝阳区");
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
            Arrays.asList(sameCityAdopter, unverifiedAdopter)
        );

        var results = matchService.matchAdoptersForPet(1L, 10);

        var sameCityResult = results.stream()
            .filter(r -> r.getUserId().equals(4L))
            .findFirst()
            .orElseThrow();

        assertTrue(sameCityResult.getMatchReason().contains("同城领养"));
    }

    @Test
    void testMatchAdoptersForPet_ScoreWithinRange() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
            Arrays.asList(verifiedAdopter, unverifiedAdopter, experiencedAdopter, sameCityAdopter)
        );

        var results = matchService.matchAdoptersForPet(1L, 10);

        for (var result : results) {
            assertTrue(result.getMatchScore() >= 0, "分数不应小于0");
            assertTrue(result.getMatchScore() <= 100, "分数不应大于100");
        }
    }

    @Test
    void testMatchAdoptersForPet_ResultsSortedByScore() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
            Arrays.asList(unverifiedAdopter, verifiedAdopter, sameCityAdopter, experiencedAdopter)
        );

        var results = matchService.matchAdoptersForPet(1L, 10);

        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getMatchScore() >= results.get(i + 1).getMatchScore(),
                "匹配结果应按分数降序排列");
        }
    }

    @Test
    void testMatchAdoptersForPet_ContainsUserInfo() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
            Collections.singletonList(verifiedAdopter)
        );

        var results = matchService.matchAdoptersForPet(1L, 10);

        var result = results.get(0);
        assertEquals(1L, result.getUserId());
        assertEquals("verified_user", result.getUsername());
        assertEquals("已认证用户", result.getRealName());
        assertEquals("13800138000", result.getPhone());
        assertEquals("北京市海淀区", result.getAddress());
        assertEquals("软件工程师", result.getOccupation());
        assertEquals("有5年养猫经验", result.getPetExperience());
        assertEquals("与家人同住，两室一厅", result.getLivingEnvironment());
        assertTrue(result.isVerified());
    }
}
