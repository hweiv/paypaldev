package com.jimi.entity;

import com.jimi.utils.UUIDUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;

import java.util.Date;

@Data
public class PaypalPaymentDto {
    // 事件id
    private String transactionID;
    // 时间戳
    private String timestamp;
    // 时区
    private String timezone;
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
    // 是否推送
    private String pushFlat;

    public PaypalPaymentInfo toInfo() {
        PaypalPaymentInfo info = new PaypalPaymentInfo();
        BeanUtils.copyProperties(this, info);
        info.setId(UUIDUtil.getUUID());
        add(info);
        return info;
    }

    public void add(PaypalPaymentInfo info) {
        info.setCreateBy("SYSTEM");
        info.setUpdateBy("SYSTEM");
        info.setCreateTime(new Date());
        info.setUpdateTime(new Date());
    }
}
