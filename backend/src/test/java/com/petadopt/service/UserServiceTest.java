package com.petadopt.service;

import com.petadopt.dto.LoginDTO;
import com.petadopt.dto.RegisterDTO;
import com.petadopt.entity.User;
import com.petadopt.mapper.UserMapper;
import com.petadopt.service.impl.UserServiceImpl;
import com.petadopt.util.JwtUtil;
import com.petadopt.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("e10adc3949ba59abbe56e057f20f883e");
        testUser.setPhone("13800138000");
        testUser.setRoleType(3);
        testUser.setStatus(1);
        testUser.setVerifyStatus(0);
    }

    @Test
    @DisplayName("测试用户登录成功")
    void testLogin_Success() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");

        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyInt())).thenReturn("test-token");

        LoginVO result = userService.login(loginDTO);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals("test-token", result.getToken());
        verify(userMapper, times(1)).selectOne(any());
        verify(jwtUtil, times(1)).generateToken(eq(1L), eq("testuser"), eq(3));
    }

    @Test
    @DisplayName("测试用户登录-用户不存在")
    void testLogin_UserNotFound() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("nonexistent");
        loginDTO.setPassword("123456");

        when(userMapper.selectOne(any())).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginDTO);
        });

        assertTrue(exception.getMessage().contains("用户名或密码错误"));
    }

    @Test
    @DisplayName("测试用户登录-密码错误")
    void testLogin_WrongPassword() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("wrongpassword");

        when(userMapper.selectOne(any())).thenReturn(testUser);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginDTO);
        });

        assertTrue(exception.getMessage().contains("用户名或密码错误"));
    }

    @Test
    @DisplayName("测试用户登录-账号已禁用")
    void testLogin_AccountDisabled() {
        testUser.setStatus(0);
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");

        when(userMapper.selectOne(any())).thenReturn(testUser);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginDTO);
        });

        assertTrue(exception.getMessage().contains("账号已被禁用"));
    }

    @Test
    @DisplayName("测试用户登录-用户在黑名单")
    void testLogin_UserInBlacklist() {
        testUser.setStatus(2);
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");

        when(userMapper.selectOne(any())).thenReturn(testUser);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginDTO);
        });

        assertTrue(exception.getMessage().contains("您已被加入黑名单"));
    }

    @Test
    @DisplayName("测试用户注册成功")
    void testRegister_Success() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("123456");
        registerDTO.setPhone("13900139000");
        registerDTO.setRoleType(3);

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return 1;
        });

        assertDoesNotThrow(() -> userService.register(registerDTO));

        verify(userMapper, times(2)).selectCount(any());
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    @DisplayName("测试用户注册-用户名已存在")
    void testRegister_UsernameExists() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setPassword("123456");
        registerDTO.setPhone("13900139000");
        registerDTO.setRoleType(3);

        when(userMapper.selectCount(any())).thenReturn(1L);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.register(registerDTO);
        });

        assertTrue(exception.getMessage().contains("用户名已存在"));
    }

    @Test
    @DisplayName("测试用户注册-手机号已注册")
    void testRegister_PhoneExists() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("123456");
        registerDTO.setPhone("13800138000");
        registerDTO.setRoleType(3);

        when(userMapper.selectCount(any())).thenReturn(0L).thenReturn(1L);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.register(registerDTO);
        });

        assertTrue(exception.getMessage().contains("手机号已被注册"));
    }
}
