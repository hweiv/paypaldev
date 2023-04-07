package com.jimi.entity;

import lombok.Data;

import java.util.Date;

@Data
public class PaymentVo {
    // 开始时间 yyyy-MM-dd HH:mm:ss
    private Date startTime;
    // 结束时间
    private Date endTime;
    // 发送消息标识：0-已发送；1-未发送
    private String pushFlat;
}
