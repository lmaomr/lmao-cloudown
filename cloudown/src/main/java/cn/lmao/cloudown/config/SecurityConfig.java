package cn.lmao.cloudown.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import cn.lmao.cloudown.model.dto.ApiResponse;
import cn.lmao.cloudown.model.enums.ErrorOperationStatus;
import cn.lmao.cloudown.util.LogUtil;

import org.slf4j.Logger;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置类
 * 负责应用的安全配置，包括认证、授权、密码加密等
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final Logger log = LogUtil.getLogger();

        /**
         * 安全过滤器链配置
         * 
         * @param http                    HttpSecurity 对象
         * @param jwtAuthenticationFilter JWT认证过滤器
         * @param traceIdFilter           TraceID过滤器
         * @return 配置好的 SecurityFilterChain
         * @throws Exception 配置异常
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        TraceIdFilter traceIdFilter) throws Exception {

                log.info("配置安全过滤器链");

                http
                // 禁用CSRF保护（因为使用JWT无状态认证）
                .csrf(csrf -> csrf.disable())

                // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 配置会话管理为无状态（STATELESS）
                .sessionManagement(session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置请求授权规则
                .authorizeHttpRequests(auth -> {
                        log.info("配置请求授权规则");
                        auth
                        // 允许公共路径无需认证
                        .requestMatchers(JwtAuthenticationFilter.PUBLIC_URLS).permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated();
                })

                .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    log.error("认证失败: {}, 路径: {}", authException.getMessage(), request.getRequestURI());
                    response.setContentType("application/json"); 
                    response.setCharacterEncoding("UTF-8"); 
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write(
                        ApiResponse.exception(ErrorOperationStatus.TOKEN_FORMAT_ERROR).getMsg().toString());
                }))

                // 添加TraceID过滤器（最先执行）
                .addFilterBefore(traceIdFilter, UsernamePasswordAuthenticationFilter.class)
                // 添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * 密码编码器配置
         * 
         * @return BCryptPasswordEncoder 实例
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                // 使用BCrypt强哈希算法加密密码
                return new BCryptPasswordEncoder();
        }

        /**
         * 认证管理器配置
         * 
         * @param authConfig 认证配置
         * @return 认证管理器实例
         * @throws Exception 配置异常
         */
        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        /**
         * CORS配置源
         * 
         * @return CorsConfigurationSource 实例
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // 允许所有来源，明确包括IP地址访问
                configuration.setAllowedOriginPatterns(List.of("*"));
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS")); // 允许的HTTP方法
                configuration.setAllowedHeaders(List.of("*")); // 允许所有头
                configuration.setExposedHeaders(List.of("Authorization")); // 暴露的响应头
                configuration.setAllowCredentials(true); // 允许携带凭证

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration); // 应用到所有路径
                return source;
        }
}