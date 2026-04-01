package com.petadopt.service;

import com.petadopt.vo.AdopterMatchVO;
import java.util.List;

/**
 * 领养匹配服务
 */
public interface MatchService {
    
    /**
     * 根据宠物要求匹配合适的领养人
     * @param petId 宠物ID
     * @param limit 返回数量限制
     * @return 匹配的领养人列表（按匹配度排序）
     */
    List<AdopterMatchVO> matchAdoptersForPet(Long petId, Integer limit);
}
