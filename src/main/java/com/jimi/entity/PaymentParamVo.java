package com.jimi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询PayPal的api入参vo
 */
@Data
public class PaymentParamVo {
    // 开始时间
    private String startTime;
    // 结束时间
    private String endTime;
    // PayPal-app账户
    private String username;
    // PayPal-app密码
    private String password;
    // PayPal-app签名
    private String signature;
    // 所要推送的钉钉机器人地址
    private String webHookUrl;
}
