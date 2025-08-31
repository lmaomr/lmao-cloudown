package cn.lmao.cloudown.service;

import java.util.List;

import cn.lmao.cloudown.model.entity.User;

public interface UserService {

    // 注册用户
    void register(User user);

    // 获取用户信息
    User getUserInfo(String email);

    // 更新用户信息
    void updateUserInfo(User user);

    void updateUserPassword(String email, String oldPassword, String newPassword);

    // 删除用户
    void deleteUser(Long id);

    // 获取用户列表
    List<User> getUserList();

}
