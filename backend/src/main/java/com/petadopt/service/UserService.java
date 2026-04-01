package com.petadopt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petadopt.common.result.PageResult;
import com.petadopt.dto.LoginDTO;
import com.petadopt.dto.RegisterDTO;
import com.petadopt.dto.VerifyDTO;
import com.petadopt.entity.User;
import com.petadopt.vo.LoginVO;
import com.petadopt.vo.UserVO;

public interface UserService extends IService<User> {
    LoginVO login(LoginDTO dto);
    void register(RegisterDTO dto);
    UserVO getCurrentUser();
    void updateUser(User user);
    void submitVerify(VerifyDTO dto);
    PageResult<UserVO> getUserList(Integer roleType, Integer status, Integer pageNum, Integer pageSize);
    void updateUserStatus(Long userId, Integer status);
}
