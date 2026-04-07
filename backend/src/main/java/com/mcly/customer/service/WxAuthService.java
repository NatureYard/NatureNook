package com.mcly.customer.service;

import com.mcly.common.auth.TokenStore;
import com.mcly.customer.api.WxLoginRequest;
import com.mcly.customer.api.WxLoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 微信登录服务。
 * <p>
 * 标准流程：小程序 wx.login() 获取 code → 后端用 code 调用微信 code2Session 接口
 * 换取 openid → 查找或创建会员 → 签发 token。
 * <p>
 * 当前为开发阶段，code2Session 调用以存根实现，直接将 code 作为模拟 openid。
 * 正式上线时需替换 {@link #code2Session(String)} 为真实的微信 HTTP 调用。
 */
@Service
public class WxAuthService {

    private static final Logger log = LoggerFactory.getLogger(WxAuthService.class);

    // TODO: 正式上线前从配置文件读取（application.yml）
    // private static final String WX_APP_ID = "";
    // private static final String WX_APP_SECRET = "";

    private final JdbcTemplate jdbcTemplate;
    private final TokenStore tokenStore;

    public WxAuthService(JdbcTemplate jdbcTemplate, TokenStore tokenStore) {
        this.jdbcTemplate = jdbcTemplate;
        this.tokenStore = tokenStore;
    }

    @Transactional
    public WxLoginResponse login(WxLoginRequest request) {
        String openid = code2Session(request.code());
        if (openid == null || openid.isBlank()) {
            throw new IllegalArgumentException("微信登录失败：无法获取用户身份");
        }

        // 1. 尝试根据 openid 查找已有会员
        Long memberId = jdbcTemplate.query(
                "select id from member where wx_openid = ? limit 1",
                rs -> rs.next() ? rs.getLong("id") : null,
                openid
        );

        boolean isNewUser = false;
        String memberName;

        if (memberId != null) {
            // 已有会员，获取名称
            memberName = jdbcTemplate.query(
                    "select name from member where id = ?",
                    rs -> rs.next() ? rs.getString("name") : "会员",
                    memberId
            );
        } else {
            // 2. openid 未绑定任何会员 → 尝试绑定第一个未绑定 openid 的会员（开发兼容）
            //    正式环境应创建新会员或引导用户绑定手机号
            memberId = jdbcTemplate.query(
                    "select id from member where wx_openid is null order by id limit 1",
                    rs -> rs.next() ? rs.getLong("id") : null
            );

            if (memberId != null) {
                jdbcTemplate.update("update member set wx_openid = ? where id = ?", openid, memberId);
                memberName = jdbcTemplate.query(
                        "select name from member where id = ?",
                        rs -> rs.next() ? rs.getString("name") : "会员",
                        memberId
                );
                log.info("已将 openid {} 绑定到会员 {} (id={})", openid, memberName, memberId);
            } else {
                // 无可用会员，走新用户创建流程
                // TODO: 正式环境需要获取手机号后才创建会员
                throw new IllegalArgumentException("当前无可绑定的会员，请联系门店工作人员开通");
            }

            isNewUser = true;
        }

        // 3. 签发 token
        String token = tokenStore.issue(memberId);
        return new WxLoginResponse(token, memberId, memberName, isNewUser);
    }

    /**
     * 调用微信 code2Session 接口，用 code 换取 openid。
     * <p>
     * TODO: 正式上线时替换为真实 HTTP 调用：
     * GET https://api.weixin.qq.com/sns/jscode2session
     *     ?appid={APP_ID}&secret={APP_SECRET}&js_code={code}&grant_type=authorization_code
     * 返回 JSON: {"openid": "...", "session_key": "...", "unionid": "..."}
     */
    private String code2Session(String code) {
        // 开发模式存根：将 code 直接作为模拟 openid
        // 微信开发者工具中 wx.login() 返回的 code 是一个临时字符串
        log.info("[开发模式] code2Session 存根: code={}, 模拟 openid=dev_{}", code, code);
        return "dev_" + code;
    }
}
