package com.jimi.common.constant;

import com.google.common.collect.ImmutableMap;

public class PaypalTransConstant {
    public final static ImmutableMap<String, String> PAY_TYPE = ImmutableMap.<String, String>builder()
            .put("Fee Reversal", "费用撤销")
            .put("Refund", "退款")
            .put("Payment", "付款")
            .put("Transfer", "资金转移")
            .put("Under Review", "处理中")
            .build();

    public final static ImmutableMap<String, String> PAY_STATUS = ImmutableMap.<String, String>builder()
            .put("Completed", "完成")
            .put("Refunded", "已退款")
            .put("Under Review", "处理中")
            .build();

    public final static ImmutableMap<String, String> PUSH_FLAT = ImmutableMap.<String, String>builder()
            .put("0", "已推送")
            .put("1", "未推送")
            .put("", "未知")
//            .put(null, "未知")
            .build();
}
