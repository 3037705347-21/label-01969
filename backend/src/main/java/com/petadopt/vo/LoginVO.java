package com.petadopt.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private UserVO userInfo;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserVO getUserInfo() { return userInfo; }
    public void setUserInfo(UserVO userInfo) { this.userInfo = userInfo; }
}
