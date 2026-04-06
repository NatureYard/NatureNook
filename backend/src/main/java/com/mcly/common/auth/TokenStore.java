package com.mcly.common.auth;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 简易 token 存储。
 * <p>
 * 当前使用内存 ConcurrentHashMap 实现，适合开发和单实例部署。
 * 正式上线后应替换为 Redis 或数据库实现以支持多实例和过期策略。
 */
@Component
public class TokenStore {

    private final ConcurrentHashMap<String, Long> tokenToMember = new ConcurrentHashMap<>();

    /**
     * 为指定会员生成一个新 token。
     */
    public String issue(Long memberId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenToMember.put(token, memberId);
        return token;
    }

    /**
     * 根据 token 获取会员 ID，若 token 无效返回 null。
     */
    public Long resolve(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return tokenToMember.get(token);
    }

    /**
     * 使指定 token 失效。
     */
    public void revoke(String token) {
        if (token != null) {
            tokenToMember.remove(token);
        }
    }
}
