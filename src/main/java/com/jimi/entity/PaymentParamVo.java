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
    private String startTime;
    private String endTime;
    private String username;
    private String password;
    private String signature;
}
