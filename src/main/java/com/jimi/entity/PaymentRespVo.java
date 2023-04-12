package com.jimi.entity;

import lombok.Data;

import java.util.Date;

@Data
public class PaymentRespVo {
    // id
    private String id;
    // 交易单号
    private String transactionID;
    // 类型：Refund-退款；Fee Reversal-费用回退；Payment-支付
    private String type;
    // 付款人账户
    private String payer;
    // 付款人名称
    private String payerDisplayName;
    // 状态：Under Review-审查中；Completed-完成；refund-退款
    private String status;
    // 总金额
    private String grossValue;
    // 总金额币种
    private String grossCurrency;
    // 手续费
    private String feeValue;
    // 手续费币种
    private String feeCurrency;
    // 最后金额（扣除手续费后）
    private String netValue;
    // 最后金额币种
    private String netCurrency;
    // 转换成本地时间
    private Date nativeTime;
    // 是否推送
    private String pushFlat;
    // 关联账户
    private String bindAccount;
    // 关联账号名称
    private String bindAccountName;
}
