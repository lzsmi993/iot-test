package com.iot.platform.common;

/**
 * 安全上下文持有者 — 模拟当前登录用户信息
 * <p>
 * 实际项目中应从 JWT / Session / Spring Security 中获取。
 * 此处提供静态方法作为占位，方便业务层调用。
 * </p>
 */
public final class SecurityContextHolder {

    private SecurityContextHolder() {
    }

    // ---- ThreadLocal 占位实现 ----

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    /** 角色常量 */
    public static final String ROLE_PLATFORM_ADMIN = "PLATFORM_ADMIN";
    public static final String ROLE_TENANT_ADMIN = "TENANT_ADMIN";
    public static final String ROLE_USER = "USER";

    public static void set(Long userId, String username, Long tenantId, String role) {
        USER_ID.set(userId);
        USERNAME.set(username);
        TENANT_ID.set(tenantId);
        ROLE.set(role);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static String getRole() {
        return ROLE.get();
    }

    public static boolean isPlatformAdmin() {
        return ROLE_PLATFORM_ADMIN.equals(getRole());
    }

    public static boolean isTenantAdmin() {
        return ROLE_TENANT_ADMIN.equals(getRole());
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        TENANT_ID.remove();
        ROLE.remove();
    }
}
