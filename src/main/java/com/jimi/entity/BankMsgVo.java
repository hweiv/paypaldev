package com.jimi.entity;

import com.jimi.utils.UUIDUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
public class BankMsgVo {
    // 银行编码
    private String bankCode;
    // 银行名称
    private String bankName;
    // 交易单号
    private String transactionID;
    // 关联账户名称
    private String accountName;
    // 关联账户
    private String account;
    // 交易类型：1-收款；2-付款
    private String type;
    // 交易状态
    private String status;
    // 付款账户名称
    private String reciprocalAccountName;
    // 付款账户
    private String reciprocalAccount;
    // 交易金额
    private String grossValue;
    // 交易币种
    private String grossCurrency;
    // 交易时间时间戳
    private String transactionTime;
    // 交易日期
    private String transactionDate;

    public BankPaymentInfo toInfo() {
        BankPaymentInfo info = new BankPaymentInfo();
        BeanUtils.copyProperties(this, info);
        info.setId(UUIDUtil.getUUID());
        add(info);
        return info;
    }

    public void add(BankPaymentInfo info) {
        info.setCreateBy("SYSTEM");
        info.setUpdateBy("SYSTEM");
        info.setCreateTime(new Date());
        info.setUpdateTime(new Date());
    }

}
