package com.petadopt.controller;

import com.petadopt.aspect.OperLog;
import com.petadopt.common.result.PageResult;
import com.petadopt.common.result.Result;
import com.petadopt.dto.LoginDTO;
import com.petadopt.dto.RegisterDTO;
import com.petadopt.dto.VerifyDTO;
import com.petadopt.entity.User;
import com.petadopt.service.UserService;
import com.petadopt.vo.LoginVO;
import com.petadopt.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @OperLog(module = "用户管理", action = "用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/info")
    public Result<UserVO> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    @PutMapping("/update")
    @OperLog(module = "用户管理", action = "更新用户信息")
    public Result<Void> updateUser(@RequestBody User user) {
        userService.updateUser(user);
        return Result.success();
    }

    @PostMapping("/verify")
    @OperLog(module = "用户管理", action = "提交实名认证")
    public Result<Void> submitVerify(@Valid @RequestBody VerifyDTO dto) {
        userService.submitVerify(dto);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<UserVO>> getUserList(
            @RequestParam(required = false) Integer roleType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.getUserList(roleType, status, pageNum, pageSize));
    }

    @PutMapping("/status")
    @OperLog(module = "用户管理", action = "更新用户状态")
    public Result<Void> updateUserStatus(@RequestParam Long userId, @RequestParam Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.success();
    }
}
