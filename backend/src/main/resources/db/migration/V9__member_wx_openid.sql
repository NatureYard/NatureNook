-- 为会员表添加微信 openid 字段，用于小程序登录身份绑定
alter table member add column wx_openid varchar(64);
create unique index idx_member_wx_openid on member(wx_openid) where wx_openid is not null;

-- 为订单表补充待支付状态相关字段
alter table customer_order add column wx_prepay_id varchar(128);
alter table customer_order add column paid_at timestamp;
