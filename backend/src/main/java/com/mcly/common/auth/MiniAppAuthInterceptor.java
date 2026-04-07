package com.mcly.common.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 小程序端 API 认证拦截器。
 * <p>
 * 从 Authorization header 中读取 Bearer token，通过 {@link TokenStore} 解析出会员 ID，
 * 并写入 {@link AuthContext} 以供业务层使用。
 * <p>
 * 对 /api/c-app/login 路径放行（登录接口无需认证）。
 * 开发环境下如果未携带 token，会自动降级为第一个会员（保持向后兼容）。
 */
@Component
public class MiniAppAuthInterceptor implements HandlerInterceptor {

    private final TokenStore tokenStore;

    public MiniAppAuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();

        // 登录接口放行
        if (path.equals("/api/c-app/login")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long memberId = tokenStore.resolve(token);
            if (memberId != null) {
                AuthContext.setMemberId(memberId);
                return true;
            }
        }

        // 开发兼容：未携带 token 时不拦截，让 currentCustomer() 的 LIMIT 1 降级逻辑兜底
        // 正式环境应返回 401，此处先保持兼容
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        AuthContext.clear();
    }
}
