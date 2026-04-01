package com.petadopt.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadopt.common.enums.RoleType;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.common.result.PageResult;
import com.petadopt.dto.LoginDTO;
import com.petadopt.dto.RegisterDTO;
import com.petadopt.dto.VerifyDTO;
import com.petadopt.entity.User;
import com.petadopt.mapper.UserMapper;
import com.petadopt.service.UserService;
import com.petadopt.util.JwtUtil;
import com.petadopt.util.UserContext;
import com.petadopt.vo.LoginVO;
import com.petadopt.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final JwtUtil jwtUtil;

    public UserServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();

        if (user == null) {
            logger.error("登录失败-用户不存在: username={}", dto.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        String encryptPwd = DigestUtil.md5Hex(dto.getPassword());
        if (!encryptPwd.equals(user.getPassword())) {
            logger.error("登录失败-密码错误: username={}, userId={}", dto.getUsername(), user.getId());
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            logger.error("登录失败-账号已禁用: username={}, userId={}", dto.getUsername(), user.getId());
            throw new BusinessException("账号已被禁用");
        }
        if (user.getStatus() == 2) {
            logger.error("登录失败-用户在黑名单: username={}, userId={}", dto.getUsername(), user.getId());
            throw new BusinessException("您已被加入黑名单");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleType());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserInfo(convertToVO(user));

        logger.info("用户登录成功: username={}, userId={}, roleType={}", user.getUsername(), user.getId(), user.getRoleType());
        return vo;
    }

    @Override
    public void register(RegisterDTO dto) {
        Long count = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .count();
        if (count > 0) {
            logger.error("注册失败-用户名已存在: username={}", dto.getUsername());
            throw new BusinessException("用户名已存在");
        }

        count = lambdaQuery()
                .eq(User::getPhone, dto.getPhone())
                .count();
        if (count > 0) {
            logger.error("注册失败-手机号已注册: phone={}", dto.getPhone());
            throw new BusinessException("手机号已被注册");
        }

        User user = new User();
        BeanUtil.copyProperties(dto, user);
        user.setPassword(DigestUtil.md5Hex(dto.getPassword()));
        user.setStatus(1);
        user.setVerifyStatus(0);

        save(user);
        logger.info("用户注册成功: username={}, userId={}, roleType={}", user.getUsername(), user.getId(), user.getRoleType());
    }

    @Override
    public UserVO getCurrentUser() {
        User user = UserContext.getUser();
        return convertToVO(user);
    }

    @Override
    public void updateUser(User user) {
        Long currentUserId = UserContext.getUserId();
        User existUser = getById(currentUserId);
        if (existUser == null) {
            logger.error("更新用户信息失败-用户不存在: userId={}", currentUserId);
            throw new BusinessException("用户不存在");
        }

        user.setId(currentUserId);
        user.setPassword(null);
        user.setStatus(null);
        user.setRoleType(null);

        updateById(user);
        logger.info("用户信息更新成功: userId={}", currentUserId);
    }

    @Override
    public void submitVerify(VerifyDTO dto) {
        Long currentUserId = UserContext.getUserId();
        User existUser = getById(currentUserId);
        if (existUser == null) {
            logger.error("提交认证失败-用户不存在: userId={}", currentUserId);
            throw new BusinessException("用户不存在");
        }

        User updateUser = new User();
        updateUser.setId(currentUserId);
        updateUser.setRealName(dto.getRealName());
        updateUser.setIdCard(dto.getIdCard());
        updateUser.setAddress(dto.getAddress());
        updateUser.setOccupation(dto.getOccupation());
        updateUser.setPetExperience(dto.getPetExperience());
        updateUser.setLivingEnvironment(dto.getLivingEnvironment());
        updateUser.setOrgName(dto.getOrgName());
        updateUser.setOrgLicense(dto.getOrgLicense());
        updateUser.setVerifyStatus(1);

        updateById(updateUser);
        logger.info("用户提交实名认证成功: userId={}, realName={}", currentUserId, dto.getRealName());
    }

    @Override
    public PageResult<UserVO> getUserList(Integer roleType, Integer status, Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (roleType != null) {
            wrapper.eq(User::getRoleType, roleType);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = page(page, wrapper);

        Page<UserVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).toList());

        logger.debug("查询用户列表: roleType={}, status={}, total={}", roleType, status, result.getTotal());
        return PageResult.of(voPage);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        Long operatorId = UserContext.getUserId();
        User user = getById(userId);
        if (user == null) {
            logger.error("更新用户状态失败-用户不存在: operatorId={}, targetUserId={}", operatorId, userId);
            throw new BusinessException("用户不存在");
        }

        Integer oldStatus = user.getStatus();
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setStatus(status);
        updateById(updateUser);

        logger.info("更新用户状态成功: operatorId={}, targetUserId={}, username={}, oldStatus={}, newStatus={}", operatorId, userId, user.getUsername(), oldStatus, status);
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        vo.setRoleName(RoleType.getNameByCode(user.getRoleType()));
        return vo;
    }
}
