package com.mcly.customer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 微信订阅消息服务。
 * TODO: 正式上线时替换为真实的微信订阅消息 API 调用。
 * 当前为开发模式存根，仅记录日志。
 */
@Service
public class WxSubscribeMessageService {

    private static final Logger log = LoggerFactory.getLogger(WxSubscribeMessageService.class);

    private final JdbcTemplate jdbcTemplate;

    public WxSubscribeMessageService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 发送入园通知给会员。
     * TODO: 调用微信 POST /cgi-bin/message/subscribe/send API
     */
    public void sendEntryNotification(Long memberId, String storeName, String gateName, String occurredAt) {
        String openid = findOpenid(memberId);
        if (openid == null) {
            log.debug("会员 {} 无 openid，跳过入园通知", memberId);
            return;
        }

        // 开发模式：仅记录日志
        log.info("[开发模式] 入园通知: memberId={}, openid={}, store={}, gate={}, time={}",
                memberId, openid, storeName, gateName, occurredAt);
    }

    private String findOpenid(Long memberId) {
        var rows = jdbcTemplate.queryForList(
                "select wx_openid from member where id = ?", memberId);
        if (rows.isEmpty()) return null;
        Object openid = rows.get(0).get("wx_openid");
        return openid != null ? openid.toString() : null;
    }
}
