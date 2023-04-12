package com.jimi.entity;

import lombok.Data;

import java.util.List;

@Data
public class PaymentDtoPageInfo {
    List<PaymentRespVo> list;
    private Integer page;
    private Integer total;
    private Integer pageSize;
}
