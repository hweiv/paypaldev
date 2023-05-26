package com.jimi.entity;

import lombok.Data;

import java.util.Date;

@Data
public class BankReqVo {
    // 数据记录开始时间 yyyy-MM-dd HH:mm:ss
    private Date startTime;
    // 数据记录结束时间
    private Date endTime;
    // 发送消息标识：0-已发送；1-未发送
    private String pushFlat;
    // 页面
    private Integer page;
    // 页面大小
    private Integer pageSize;
    // 银行类型
    private String bankCode;
    // 关联账号尾号
    private String accountEndNum;
}
