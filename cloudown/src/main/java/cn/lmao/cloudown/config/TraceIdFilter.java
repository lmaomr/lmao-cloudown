package cn.lmao.cloudown.config;

import cn.lmao.cloudown.util.LogUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TraceID过滤器
 * 为每个请求生成唯一的TraceID，用于请求追踪
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 从请求头中获取TraceID，如果没有则生成新的
            String traceId = request.getHeader("X-Trace-ID");
            LogUtil.initRequestContext(traceId);
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束时清除TraceID
            LogUtil.clearRequestContext();
        }
    }
} 