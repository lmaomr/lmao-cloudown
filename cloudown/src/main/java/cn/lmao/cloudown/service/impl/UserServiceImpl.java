package cn.lmao.cloudown.service.impl;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import cn.lmao.cloudown.excepiton.CustomException;
import cn.lmao.cloudown.model.entity.User;
import cn.lmao.cloudown.model.enums.ErrorOperationStatus;
import cn.lmao.cloudown.repository.UserRepository;
import cn.lmao.cloudown.service.UserService;
import cn.lmao.cloudown.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService{

    private final UserRepository userRepository;
    private final Logger log = LogUtil.getLogger();
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(User user) {
        log.debug("开始注册新用户: {}", user.getEmail());
        
        // 检查邮箱是否已存在
        if (userRepository.getUserByEmail(user.getEmail()) != null) {
            log.warn("邮箱[{}]已被注册，注册失败", user.getEmail());
            throw new CustomException(ErrorOperationStatus.EMAIL_EXISTS);
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 保存用户
        userRepository.save(user);
        log.debug("用户[{}]注册成功", user.getEmail());
    }

    @Override
    public User getUserInfo(String email) {
        log.debug("获取用户信息: {}", email);
        if (email == null) {
            throw new CustomException(ErrorOperationStatus.EMPTY_EMAIL);
        }
        return userRepository.getUserByEmail(email);
    }

    @Override
    public void updateUserInfo(User user) {
        log.debug("更新用户信息: {}", user.getEmail());
        if (user.getId() == null) {
            log.warn("更新用户信息失败，用户ID不能为空");
            throw new CustomException(ErrorOperationStatus.USER_NOT_FOUND);
        }
        userRepository.save(user);
    }

    @Override
    public void updateUserPassword(String email, String oldPassword, String newPassword) {
        log.debug("更新用户密码: {}", email);
        if (email == null || oldPassword == null || newPassword == null) {
            log.warn("更新用户密码失败: 缺少必要参数");
            throw new CustomException(ErrorOperationStatus.EMPTY_PARAMETER);
        }

        User user = userRepository.getUserByEmail(email);
        if (user == null) {
            log.warn("用户[{}]不存在，无法更新密码", email);
            throw new CustomException(ErrorOperationStatus.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("旧密码错误，无法更新密码");
            throw new CustomException(ErrorOperationStatus.PASSWORD_ERROR);
        }

        // 检查新密码是否与旧密码相同
        if (oldPassword.equals(newPassword)) {
            log.warn("新密码与旧密码相同，无法更新密码");
            throw new CustomException(ErrorOperationStatus.PASSWORD_NOT_MATCH);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.debug("用户[{}]密码更新成功", email);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getUserList() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("加载用户信息: {}", email);
        User user = userRepository.getUserByEmail(email);
        if (user == null) {
            log.warn("用户[{}]不存在", email);
            throw new UsernameNotFoundException(ErrorOperationStatus.USER_NOT_FOUND.getMsg());
        }
        
        log.debug("开始身份验证: {}", user.getEmail());
        user.setLastLoginTime(java.time.LocalDateTime.now());
        userRepository.save(user); // 更新最后登录时间
        
        log.debug("身份验证成功: {}", user.getEmail());
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getName()))
        );  
    }

}
