package com.sky.utils;

public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /** 可能为 null：用于需要“可选登录”的地方 */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /** 必须登录：未登录就抛异常，避免 NPE */
    public static Long requireUserId() {
        Long id = USER_ID.get();
        if (id == null) {
            throw new IllegalStateException("UserContext 未设置 userId（未登录或拦截器未生效）");
        }
        return id;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
