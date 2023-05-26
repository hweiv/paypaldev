package com.jimi.entity;

import lombok.Data;

import java.util.List;

@Data
public class BankAccountDto {
    // 银行类型编码
    private String bankCode;
    // 银行类型名称
    private String bankName;
    // 银行账号
    private List<String> accountList;
}
