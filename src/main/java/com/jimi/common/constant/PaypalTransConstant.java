package com.jimi.common.constant;

import com.google.common.collect.ImmutableMap;

public class PaypalTransConstant {
    public static final ImmutableMap<String, String> PAY_TYPE = ImmutableMap.<String, String>builder()
            .put("Fee Reversal", "费用撤销")
            .put("Refund", "退款")
            .put("Payment", "付款")
            .put("Transfer", "资金转移")
            .put("Under Review", "处理中")
            .build();

    public static final ImmutableMap<String, String> PAY_STATUS = ImmutableMap.<String, String>builder()
            .put("Completed", "完成")
            .put("Refunded", "已退款")
            .put("Under Review", "处理中")
            .build();

    public static final ImmutableMap<String, String> PUSH_FLAT = ImmutableMap.<String, String>builder()
            .put("0", "已推送")
            .put("1", "未推送")
            .put("", "未知")
//            .put(null, "未知")
            .build();

    public static final ImmutableMap<String, String> BIND_ACCOUNT_NAME = ImmutableMap.<String, String>builder()
            .put("sb-e8ud221290071@business.example.com", "公司沙盒测试PayPal账号")
            .put("sb-liqjq15336561@business.example.com", "个人沙盒测试PayPal账号")
            .put("support76@jimilab.com", "香港康凯斯PayPal账号")
            .put("linjufen@jimilab.com", "几米PayPal账号")
            .put("503293113@qq.com", "富瑞（深圳）账号")
            .put("furuidexin_business@hotmail.com", "富瑞德信（香港）账号")
            .build();
}
