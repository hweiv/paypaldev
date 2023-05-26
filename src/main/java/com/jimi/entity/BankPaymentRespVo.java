package com.jimi.entity;

import lombok.Data;

import java.util.Date;

@Data
public class BankPaymentRespVo {
    private String id;
    private String bankCode;
    // 银行名称
    private String bankName;
    // 交易单号
    private String transactionID;
    // 关联账户名称
    private String bindAccountName;
    // 关联账户
    private String bindAccount;
    // 交易类型
    private String type;
    // 付款账户名称
    private String reciprocalAccountName;
    // 付款账户
//    private String reciprocalAccount;
    // 交易金额
    private String grossValue;
    // 交易币种
    private String grossCurrency;
    // 交易时间戳
    private Date transactionTime;
    // 是否有效：0-有效，1-删除
//    private String delFlat;
    // 是否推送
    private String pushFlat;
    // 数据记录时间
    private Date createTime;
    // 交易日期
    private String transactionDate;
}
