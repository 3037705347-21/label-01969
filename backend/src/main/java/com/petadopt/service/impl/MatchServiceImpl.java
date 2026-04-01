package com.petadopt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.entity.Pet;
import com.petadopt.entity.User;
import com.petadopt.mapper.PetMapper;
import com.petadopt.mapper.UserMapper;
import com.petadopt.service.MatchService;
import com.petadopt.vo.AdopterMatchVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 领养匹配服务实现
 * 根据宠物的领养要求自动筛选匹配合适的领养人
 */
@Service
public class MatchServiceImpl implements MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MatchServiceImpl.class);
    private final PetMapper petMapper;
    private final UserMapper userMapper;

    public MatchServiceImpl(PetMapper petMapper, UserMapper userMapper) {
        this.petMapper = petMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<AdopterMatchVO> matchAdoptersForPet(Long petId, Integer limit) {
        // 获取宠物信息
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            logger.error("匹配领养人失败-宠物不存在: petId={}", petId);
            throw new BusinessException("宠物不存在");
        }

        logger.info("开始为宠物匹配领养人: petId={}, petName={}, requireExperience={}, allowSingleLiving={}",
                petId, pet.getName(), pet.getRequireExperience(), pet.getAllowSingleLiving());

        // 查询所有正常状态的领养人（roleType=3, status=1）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRoleType, 3)  // 领养人
               .eq(User::getStatus, 1)     // 正常状态
               .eq(User::getDeleted, 0);   // 未删除

        List<User> adopters = userMapper.selectList(wrapper);

        if (adopters.isEmpty()) {
            logger.info("没有找到符合条件的领养人: petId={}", petId);
            return new ArrayList<>();
        }

        // 计算每个领养人的匹配分数
        List<AdopterMatchVO> matchResults = adopters.stream()
                .map(adopter -> calculateMatch(pet, adopter))
                .sorted(Comparator.comparingInt(AdopterMatchVO::getMatchScore).reversed())
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());

        logger.info("为宠物匹配领养人完成: petId={}, petName={}, candidateCount={}, resultCount={}",
                petId, pet.getName(), adopters.size(), matchResults.size());
        return matchResults;
    }

    /**
     * 计算领养人与宠物的匹配度
     */
    private AdopterMatchVO calculateMatch(Pet pet, User adopter) {
        AdopterMatchVO vo = new AdopterMatchVO();
        vo.setUserId(adopter.getId());
        vo.setUsername(adopter.getUsername());
        vo.setRealName(adopter.getRealName());
        vo.setPhone(adopter.getPhone());
        vo.setAddress(adopter.getAddress());
        vo.setOccupation(adopter.getOccupation());
        vo.setPetExperience(adopter.getPetExperience());
        vo.setLivingEnvironment(adopter.getLivingEnvironment());
        vo.setVerified(adopter.getVerifyStatus() != null && adopter.getVerifyStatus() == 1);

        int score = 0;
        List<String> reasons = new ArrayList<>();

        // 1. 实名认证加分 (+20分)
        if (adopter.getVerifyStatus() != null && adopter.getVerifyStatus() == 1) {
            score += 20;
            reasons.add("已完成实名认证");
        }

        // 2. 养宠经验匹配 (+30分)
        boolean hasExperience = adopter.getPetExperience() != null && !adopter.getPetExperience().isEmpty();
        if (pet.getRequireExperience() != null && pet.getRequireExperience() == 1) {
            // 宠物要求有经验
            if (hasExperience) {
                score += 30;
                reasons.add("有养宠经验，符合要求");
            } else {
                // 没有经验但宠物要求有，扣分
                score -= 10;
                reasons.add("缺少养宠经验");
            }
        } else {
            // 宠物不要求经验，有经验加分
            if (hasExperience) {
                score += 15;
                reasons.add("有养宠经验");
            }
        }

        // 3. 居住环境评估 (+20分)
        if (adopter.getLivingEnvironment() != null && !adopter.getLivingEnvironment().isEmpty()) {
            score += 10;
            reasons.add("已填写居住环境信息");

            // 检查是否独居
            String env = adopter.getLivingEnvironment().toLowerCase();
            boolean seemsSingleLiving = env.contains("独居") || env.contains("一个人") || env.contains("单身");

            if (pet.getAllowSingleLiving() != null && pet.getAllowSingleLiving() == 0) {
                // 宠物不允许独居
                if (seemsSingleLiving) {
                    score -= 15;
                    reasons.add("独居环境，宠物不适合独居");
                } else {
                    score += 10;
                    reasons.add("非独居环境，符合要求");
                }
            } else {
                score += 10;
            }
        }

        // 4. 地区匹配 (+15分)
        if (pet.getLocation() != null && adopter.getAddress() != null) {
            String petLocation = pet.getLocation().toLowerCase();
            String adopterAddress = adopter.getAddress().toLowerCase();

            // 简单的地区匹配：检查是否在同一城市
            if (isSameCity(petLocation, adopterAddress)) {
                score += 15;
                reasons.add("同城领养，便于家访");
            } else if (isSameProvince(petLocation, adopterAddress)) {
                score += 8;
                reasons.add("同省领养");
            }
        }

        // 5. 资料完整度 (+15分)
        int completeness = 0;
        if (adopter.getRealName() != null && !adopter.getRealName().isEmpty()) completeness++;
        if (adopter.getPhone() != null && !adopter.getPhone().isEmpty()) completeness++;
        if (adopter.getAddress() != null && !adopter.getAddress().isEmpty()) completeness++;
        if (adopter.getOccupation() != null && !adopter.getOccupation().isEmpty()) completeness++;
        if (adopter.getPetExperience() != null && !adopter.getPetExperience().isEmpty()) completeness++;

        score += completeness * 3;
        if (completeness >= 4) {
            reasons.add("资料完整度高");
        }

        // 确保分数在0-100之间
        score = Math.max(0, Math.min(100, score));

        vo.setMatchScore(score);
        vo.setMatchReason(String.join("；", reasons));

        return vo;
    }

    /**
     * 判断是否同城
     */
    private boolean isSameCity(String location1, String location2) {
        String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "南京", "成都", "武汉", "西安", "重庆",
                "天津", "苏州", "郑州", "长沙", "东莞", "沈阳", "青岛", "合肥", "佛山", "宁波"};

        for (String city : cities) {
            if (location1.contains(city) && location2.contains(city)) {
                return true;
            }
        }

        // 检查区级匹配
        String[] districts = {"朝阳", "海淀", "浦东", "徐汇", "天河", "福田", "南山", "西湖"};
        for (String district : districts) {
            if (location1.contains(district) && location2.contains(district)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否同省
     */
    private boolean isSameProvince(String location1, String location2) {
        String[] provinces = {"北京", "上海", "天津", "重庆", "广东", "江苏", "浙江", "山东", "河南",
                "四川", "湖北", "湖南", "河北", "福建", "陕西", "安徽", "辽宁", "江西", "云南", "广西"};

        for (String province : provinces) {
            if (location1.contains(province) && location2.contains(province)) {
                return true;
            }
        }
        return false;
    }
}
