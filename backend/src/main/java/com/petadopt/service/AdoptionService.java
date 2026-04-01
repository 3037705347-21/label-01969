package com.petadopt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petadopt.common.result.PageResult;
import com.petadopt.dto.AdoptionApplyDTO;
import com.petadopt.dto.ReviewDTO;
import com.petadopt.entity.AdoptionApplication;
import com.petadopt.vo.AdoptionApplicationVO;

public interface AdoptionService extends IService<AdoptionApplication> {
    void apply(AdoptionApplyDTO dto);
    PageResult<AdoptionApplicationVO> getMyApplications(Integer pageNum, Integer pageSize);
    PageResult<AdoptionApplicationVO> getReceivedApplications(Integer status, Integer pageNum, Integer pageSize);
    PageResult<AdoptionApplicationVO> getPendingReviewList(Integer pageNum, Integer pageSize);
    void rescueReview(ReviewDTO dto);
    void adminReview(ReviewDTO dto);
    void updateHomeVisit(ReviewDTO dto);
    AdoptionApplicationVO getApplicationDetail(Long id);
}
