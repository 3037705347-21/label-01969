package com.petadopt.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadopt.BaseServiceTest;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.dto.LoginDTO;
import com.petadopt.dto.RegisterDTO;
import com.petadopt.entity.User;
import com.petadopt.mapper.UserMapper;
import com.petadopt.service.impl.UserServiceImpl;
import com.petadopt.util.JwtUtil;
import com.petadopt.util.UserContext;
import com.petadopt.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest extends BaseServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUpBase() {
        setBaseMapper(userService, userMapper);
    }

    private User testUser;
    private LoginDTO loginDTO;
    private RegisterDTO registerDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(DigestUtil.md5Hex("123456"));
        testUser.setStatus(1);
        testUser.setRoleType(3);

        loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");

        registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("123456");
        registerDTO.setPhone("13800138000");
        registerDTO.setRoleType(3);
    }

    @Test
    void testLogin_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
            when(jwtUtil.generateToken(any(), any(), any())).thenReturn("test-token");

            LoginVO result = userService.login(loginDTO);

            assertNotNull(result);
            assertNotNull(result.getToken());
            assertEquals("test-token", result.getToken());
            assertNotNull(result.getUserInfo());
            assertEquals("testuser", result.getUserInfo().getUsername());
        }
    }

    @Test
    void testLogin_UserNotExist() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void testLogin_PasswordError() {
        loginDTO.setPassword("wrongpassword");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void testLogin_AccountDisabled() {
        testUser.setStatus(0);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });

        assertEquals("账号已被禁用", exception.getMessage());
    }

    @Test
    void testLogin_Blacklisted() {
        testUser.setStatus(2);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });

        assertEquals("您已被加入黑名单", exception.getMessage());
    }

    @Test
    void testRegister_Success() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });

        assertDoesNotThrow(() -> {
            userService.register(registerDTO);
        });

        verify(userMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void testRegister_UsernameExists() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerDTO);
        });

        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    void testRegister_PhoneExists() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerDTO);
        });

        assertEquals("手机号已被注册", exception.getMessage());
    }

    @Test
    void testGetCurrentUser_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUser).thenReturn(testUser);

            var result = userService.getCurrentUser();

            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            assertEquals("领养人", result.getRoleName());
        }
    }

    @Test
    void testUpdateUser_Success() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            User updateUser = new User();
            updateUser.setRealName("Test Name");
            updateUser.setPhone("13900139000");

            assertDoesNotThrow(() -> {
                userService.updateUser(updateUser);
            });

            verify(userMapper, times(1)).updateById(any(User.class));
        }
    }

    @Test
    void testUpdateUser_UserNotExist() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(999L);
            when(userMapper.selectById(999L)).thenReturn(null);

            User updateUser = new User();
            updateUser.setRealName("Test Name");

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.updateUser(updateUser);
            });

            assertEquals("用户不存在", exception.getMessage());
        }
    }
}
