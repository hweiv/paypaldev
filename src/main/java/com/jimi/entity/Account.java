package com.jimi.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class Account {
    @NotBlank(message = "账号不能为空")
    private String account;
    @NotBlank(message = "密码不能为空")
    private String password;
}
