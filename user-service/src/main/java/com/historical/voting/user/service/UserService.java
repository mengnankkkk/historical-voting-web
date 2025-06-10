package com.historical.voting.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.historical.voting.user.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {
    public void register(String username, String password, String email, String verificationCode);
    public Map<String, String> login(String username, String password);
    public void logout(String username);
    public void updateExperience(String username, int amount);
    public User getUserProfile(String username);
    public void updateProfile(String username, User updateInfo);
}
