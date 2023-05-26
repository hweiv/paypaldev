package com.jimi.entity;

import lombok.Data;

import java.util.List;

@Data
public class BankPaymentDtoPageInfo {
    List<BankPaymentRespVo> list;
    private Integer page;
    private Integer total;
    private Integer pageSize;
}
