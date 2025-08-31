package cn.lmao.cloudown.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.lmao.cloudown.model.entity.User;
import cn.lmao.cloudown.model.enums.ErrorOperationStatus;
import cn.lmao.cloudown.service.UserService;
import cn.lmao.cloudown.util.JwtUtil;
import cn.lmao.cloudown.util.LogUtil;
import cn.lmao.cloudown.excepiton.CustomException;
import cn.lmao.cloudown.model.dto.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final Logger log = LogUtil.getLogger();
    private final JwtUtil jwtUtil;

    @GetMapping("/info")
    public ApiResponse<User> getUserInfo(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmailFromHeader(token);
        log.info("获取用户信息: {}", email);
        try {
            log.info("获取用户信息成功: {}", email);
            return ApiResponse.success(userService.getUserInfo(email));
        } catch (CustomException e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
            return ApiResponse.exception(new CustomException(ErrorOperationStatus.USER_INFO_NOT_FOUND));
        }
    }

    @PutMapping("/update/nickname")
    public ApiResponse<Boolean> updateNickname(@RequestHeader("Authorization") String token, @RequestBody User user) {
        String newNickname = user.getNickname();
        if (newNickname == null) {
            log.warn("更新用户昵称失败: 缺少必要参数");
            return ApiResponse.exception(new CustomException(ErrorOperationStatus.EMPTY_PARAMETER));
        }
        String email = jwtUtil.getEmailFromHeader(token);
        log.info("更新用户昵称: {}, 新昵称: {}", email, newNickname);
        try {
            User newUser = userService.getUserInfo(email); // 确保用户存在
            newUser.setNickname(newNickname);
            userService.updateUserInfo(newUser);
            log.info("更新用户昵称成功: {}", email);
            return ApiResponse.success(true);
        } catch (CustomException e) {
            log.warn("更新用户昵称失败: {}", email, e.getMessage());
            return ApiResponse.exception(new CustomException(ErrorOperationStatus.USER_NICKNAME_UPDATE_FAILED));
        }
    }

    @PutMapping("/update/password")
    public ApiResponse<Boolean> updatePassword(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> pMap) {
        String oldPassword = pMap.get("oldPassword");
        String newPassword = pMap.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            log.warn("更新用户密码失败: 缺少必要参数");
            return ApiResponse.exception(new CustomException(ErrorOperationStatus.EMPTY_PARAMETER));
        }
        String email = jwtUtil.getEmailFromHeader(token);
        log.info("更新用户密码: {}", email);
        try {
            userService.updateUserPassword(email, oldPassword, newPassword);
            log.info("更新用户密码成功: {}", email);
            return ApiResponse.success(true);
        } catch (CustomException e) {
            log.warn("更新用户密码失败: {}", email, e.getMessage());
            return ApiResponse.exception(e);
        } catch (Exception e) {
            log.error("更新用户密码异常: {}", email, e);
            return ApiResponse.exception(new CustomException(ErrorOperationStatus.PASSWORD_CHANGE_FAILED));
        }
    }
}
