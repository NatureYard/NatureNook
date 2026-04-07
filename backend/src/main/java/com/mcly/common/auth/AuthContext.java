package com.mcly.common.auth;

/**
 * 线程级认证上下文，存储当前请求的已认证会员 ID。
 * 由 {@link MiniAppAuthInterceptor} 在请求进入时写入，请求结束后清除。
 */
public final class AuthContext {

    private static final ThreadLocal<Long> MEMBER_ID = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void setMemberId(Long memberId) {
        MEMBER_ID.set(memberId);
    }

    public static Long getMemberId() {
        return MEMBER_ID.get();
    }

    public static void clear() {
        MEMBER_ID.remove();
    }
}
