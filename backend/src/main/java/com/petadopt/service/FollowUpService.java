package com.petadopt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petadopt.common.result.PageResult;
import com.petadopt.dto.FollowUpDTO;
import com.petadopt.entity.FollowUpRecord;
import com.petadopt.vo.FollowUpRecordVO;

public interface FollowUpService extends IService<FollowUpRecord> {
    void submitFollowUp(FollowUpDTO dto);
    PageResult<FollowUpRecordVO> getMyRecords(Integer pageNum, Integer pageSize);
    PageResult<FollowUpRecordVO> getPetRecords(Long petId, Integer pageNum, Integer pageSize);
    PageResult<FollowUpRecordVO> getPendingList(Integer pageNum, Integer pageSize);
    void reviewFollowUp(Long id, Integer status, String comment);
    void reclaimPet(Long petId, String reason);
}
