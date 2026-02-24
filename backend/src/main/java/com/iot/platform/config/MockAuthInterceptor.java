package com.iot.platform.config;

import com.iot.platform.common.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 模拟认证拦截器 — 开发/测试环境使用
 * 默认以平台管理员身份访问，可通过 Header 切换：
 *   X-User-Id, X-Username, X-Tenant-Id, X-Role
 */
@Component
public class MockAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long userId = parseLong(request.getHeader("X-User-Id"), 1L);
        String username = request.getHeader("X-Username") != null ? request.getHeader("X-Username") : "admin";
        Long tenantId = parseLong(request.getHeader("X-Tenant-Id"), 0L);
        String role = request.getHeader("X-Role") != null ? request.getHeader("X-Role") : SecurityContextHolder.ROLE_PLATFORM_ADMIN;

        SecurityContextHolder.set(userId, username, tenantId, role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SecurityContextHolder.clear();
    }

    private Long parseLong(String value, Long defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        try { return Long.parseLong(value); } catch (NumberFormatException e) { return defaultValue; }
    }
}
