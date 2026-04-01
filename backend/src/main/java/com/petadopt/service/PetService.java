package com.petadopt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petadopt.common.result.PageResult;
import com.petadopt.dto.PetDTO;
import com.petadopt.dto.PetQueryDTO;
import com.petadopt.entity.Pet;
import com.petadopt.vo.PetVO;

public interface PetService extends IService<Pet> {
    void createPet(PetDTO dto);
    void updatePet(PetDTO dto);
    PetVO getPetDetail(Long id);
    PageResult<PetVO> getPetList(PetQueryDTO query);
    void updatePetStatus(Long id, Integer status);
    void deletePet(Long id);
    PageResult<PetVO> getMyPetList(Integer pageNum, Integer pageSize);
    PageResult<PetVO> getMyAdoptedPets(Integer pageNum, Integer pageSize);
    java.util.Map<String, Long> getHomeStats();
}
