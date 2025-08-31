package cn.lmao.cloudown.controller;

import cn.lmao.cloudown.model.dto.ApiResponse;
import cn.lmao.cloudown.model.entity.User;
import cn.lmao.cloudown.model.enums.ErrorOperationStatus;
import cn.lmao.cloudown.service.UserService;
import cn.lmao.cloudown.util.JwtUtil;
import cn.lmao.cloudown.util.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final Logger log = LogUtil.getLogger();

    @PostMapping("/login")
    public ApiResponse<String> login(
            @RequestBody User loginRequest) {
        String email = loginRequest.getEmail();
        log.info("接收到用户登录请求: email={}", email);

        try {
            // 2. 检查邮箱和密码
            if (email == null || email.isEmpty() || loginRequest.getPassword() == null) {
                log.warn("登录失败: 邮箱或密码为空, email={}", email);
                return ApiResponse.error(400, "邮箱或密码不能为空");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(email);

            // 记录安全审计日志
            LogUtil.audit("USER_LOGIN", Map.of("email", email, "success", true));

            log.info("用户登录成功: email={}", email);

            return ApiResponse.success("登录成功", jwt);
        } catch (BadCredentialsException e) {
            // 记录安全审计日志
            LogUtil.audit("USER_LOGIN", Map.of("email", email, "success", false, "reason", "密码错误"));

            log.warn("用户登录失败: 邮箱或密码错误, email={}", email);

            return ApiResponse.exception(ErrorOperationStatus.BAD_CREDENTIALS);
        } catch (Exception e) {
            log.error("用户登录异常: email={}, error={}", email, e.getMessage(), e);
            return ApiResponse.error(500, "登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ApiResponse<String> register(
            @RequestBody User registUser,
            HttpServletRequest request) {
        String email = registUser.getEmail();
        String password = registUser.getPassword();

        log.info("接收到用户注册请求: email={}", email);

        try {
            // 1. 检查注册信息是否为空
            if (password == null || email == null) {
                log.warn("注册失败: 注册信息不完整, password={}, email={}", password, email);
                return ApiResponse.error(400, "注册信息不能为空");
            }

            // 2. 注册用户
            userService.register(registUser);
            log.info("注册用户成功: email={}", email);

            // 记录安全审计日志
            LogUtil.audit("USER_REGISTER", Map.of("email", email));

            return ApiResponse.success("注册成功");
        } catch (Exception e) {
            log.error("用户注册异常:  email={}, error={}", email, e.getMessage(), e);
            return ApiResponse.error(500, "注册失败: " + e.getMessage());
        }
    }
}