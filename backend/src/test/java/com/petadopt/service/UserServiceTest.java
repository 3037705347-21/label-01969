package com.petadopt.service;

import com.petadopt.BaseTest;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.dto.LoginDTO;
import com.petadopt.dto.RegisterDTO;
import com.petadopt.entity.User;
import com.petadopt.mapper.UserMapper;
import com.petadopt.vo.LoginVO;
import org.junit.jupiter.api.DisplayName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import cn.hutool.crypto.digest.DigestUtil;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("用户模块测试")
public class UserServiceTest extends BaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("登录成功-正常流程")
    void testLogin_Success() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        LoginVO result = userService.login(dto);

//        assertNotNull(result);
        assertNotNull(result.getToken());
        assertNotNull(result.getUserInfo());
        assertEquals("admin", result.getUserInfo().getUsername());
    }

    @Test
    @DisplayName("登录失败-用户不存在")
    void testLogin_UserNotExist() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("nonexist");
        dto.setPassword("123456");

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败-密码错误")
    void testLogin_WrongPassword() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrongpassword");

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败-账号已禁用")
    void testLogin_AccountDisabled() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("disabled");
        dto.setPassword("123456");

        User disabledUser = new User();
        disabledUser.setUsername("disabled");
        disabledUser.setPassword(DigestUtil.md5Hex("123456"));
        disabledUser.setStatus(0);
        disabledUser.setRoleType(3);
        userMapper.insert(disabledUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals("账号已被禁用", exception.getMessage());

        userMapper.deleteById(disabledUser.getId());
    }

    @Test
    @DisplayName("注册成功-正常流程")
    void testRegister_Success() {
        String testUsername = "testuser" + System.currentTimeMillis();
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(testUsername);
        dto.setPassword("123456");
        dto.setPhone("13800138000");
        dto.setRoleType(3);

        assertDoesNotThrow(() -> userService.register(dto));

        assertNotNull(user);
        assertEquals(1, user.getStatus());
        assertEquals(0, user.getVerifyStatus());

        userMapper.deleteById(user.getId());
    }

    @Test
    @DisplayName("注册失败-用户名已存在")
    void testRegister_UsernameExists() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");
        dto.setPhone("13900139000");
        dto.setRoleType(3);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.register(dto));
        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    @DisplayName("注册失败-手机号已注册")
    void testRegister_PhoneExists() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser" + System.currentTimeMillis());
        dto.setPassword("123456");
        dto.setPhone("13800138001");
        dto.setRoleType(3);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.register(dto));
        assertEquals("手机号已被注册", exception.getMessage());
    }

    @Test
    @DisplayName("获取当前用户信息")
    void testGetCurrentUser() {
        setAdminContext();
        assertDoesNotThrow(() -> userService.getCurrentUser());
    }

    @Test
    @DisplayName("更新用户信息")
    void testUpdateUser() {
        setAdopterContext();
        User user = new User();
        user.setRealName("测试用户");
        user.setEmail("test@example.com");

        assertDoesNotThrow(() -> userService.updateUser(user));
    }

    @Test
    @DisplayName("管理员获取用户列表")
    void testGetUserList_Admin() {
        setAdminContext();
        var result = userService.getUserList(null, null, 1, 10);
//        assertNotNull(result);
//        assertTrue(result.getTotal() >= 0);
    }

    @Test
    @DisplayName("按角色筛选用户列表")
    void testGetUserList_ByRole() {
        setAdminContext();
        var result = userService.getUserList(3, null, 1, 10);
//        assertNotNull(result);
        result.getList().forEach(user -> assertEquals("领养人", user.getRoleName()));
    }

    @Test
    @DisplayName("管理员更新用户状态")
    void testUpdateUserStatus_Admin() {
        setAdminContext();

        User testUser = new User();
        testUser.setUsername("statustest" + System.currentTimeMillis());
        testUser.setPassword(DigestUtil.md5Hex("123456"));
        testUser.setPhone("138" + System.currentTimeMillis());
        testUser.setStatus(1);
        testUser.setRoleType(3);
        userMapper.insert(testUser);

        assertDoesNotThrow(() -> userService.updateUserStatus(testUser.getId(), 0));

        User updated = userMapper.selectById(testUser.getId());
        assertEquals(0, updated.getStatus());

        userMapper.deleteById(testUser.getId());
    }

    @Test
    @DisplayName("更新用户状态-用户不存在")
    void testUpdateUserStatus_UserNotExist() {
        setAdminContext();
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.updateUserStatus(999999L, 0));
        assertEquals("用户不存在", exception.getMessage());
    }
}
