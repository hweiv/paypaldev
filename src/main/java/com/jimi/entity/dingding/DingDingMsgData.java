package com.jimi.entity.dingding;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DingDingMsgData {
    // 通知对象电话号
    @NotBlank
    private String mobile;
    // 通知内容标题
    private String title;
    // 通知内容
    private String content;
    // 查看详情-跳转链接
    private String url;
}
