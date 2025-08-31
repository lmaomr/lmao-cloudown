package cn.lmao.cloudown.config;

import cn.lmao.cloudown.model.dto.ApiResponse;
import cn.lmao.cloudown.model.enums.ErrorOperationStatus;
import cn.lmao.cloudown.util.JsonUtil;
import cn.lmao.cloudown.util.JwtUtil;
import cn.lmao.cloudown.util.LogUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final Logger log = LogUtil.getLogger();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 公共路径常量
    public static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/avatar/**",
            "/thumb/**"
    };

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // 添加一个 ThreadLocal 来存储已验证的请求
    private static final Set<String> requestIds = new HashSet<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        // 获取请求的唯一标识
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null) {
            requestId = MDC.get("traceId");
            response.setHeader("X-Request-ID", requestId);
        }

        // 检查这个请求是否已经验证过
        if (requestIds.contains(requestId)) {
            // 如果已经验证过，直接放行
            log.debug("已验证过的requestId:{}", requestId);
            filterChain.doFilter(request, response);
            return;
        }

        // 获取请求URI
        String requestURI = request.getRequestURI();
        log.debug("JWT过滤器处理请求: {}, 方法: {}", requestURI, request.getMethod());

        // 使用AntPathMatcher检查是否为公共路径
        boolean isPublicPath = false;
        for (String pattern : PUBLIC_URLS) {
            if (pathMatcher.match(pattern, requestURI)) {
                isPublicPath = true;
                log.debug("公共路径匹配成功: {} 匹配模式 {}", requestURI, pattern);
                break;
            }
        }

        if (isPublicPath) {
            log.debug("公共路径, 直接放行: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 获取并验证 JWT token
            String authHeader = request.getHeader("Authorization");
            log.debug("处理请求: {}, Authorization: {}", requestURI, authHeader != null ? "存在" : "不存在");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("请求没有Authorization头或格式不正确，直接放行: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            try {
                // 2. 验证 token 并设置认证信息
                String email = jwtUtil.getEmailFromHeader(authHeader);

                if (email != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    // 3. 设置认证信息
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("认证成功: 用户 {} 访问 {}", email, requestURI);
                    // 4. 继续执行过滤器链
                    filterChain.doFilter(request, response);
                } else {
                    log.warn("认证失败: 无法从token中获取邮箱: {}", requestURI);
                    handleAuthenticationFailure(response, "无效的认证令牌");
                }
            } catch (JwtException e) {
                log.error("处理请求异常，JwtException: {}, 路径: {}", e.getMessage(), requestURI);
                handleAuthenticationFailure(response, "认证令牌验证失败: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("处理请求异常: {}, 路径: {}", e.getMessage(), requestURI);
            handleAuthenticationFailure(response, "认证过程发生错误");
        } finally {
            // 记录请求ID
            requestIds.add(requestId);
        }
    }

    private void handleAuthenticationFailure(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<?> errorResponse = ApiResponse.exception(ErrorOperationStatus.TOKEN_FORMAT_ERROR);
        // 在日志中记录详细错误信息
        log.error("认证失败: {}", message);

        response.getWriter().write(JsonUtil.toJson(errorResponse));
    }
}