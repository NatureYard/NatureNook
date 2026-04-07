package com.mcly.customer.api;

/**
 * 微信预支付参数，透传给小程序端 wx.requestPayment() 调用。
 * 开发模式下返回模拟数据，正式环境由微信支付统一下单接口生成。
 */
public record PrepayResponse(
        String timeStamp,
        String nonceStr,
        String packageValue,
        String signType,
        String paySign,
        String orderNo,
        String amount
) {
}
