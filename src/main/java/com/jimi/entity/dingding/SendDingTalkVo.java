package com.jimi.entity.dingding;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class SendDingTalkVo {
    // 钉钉群里面的机器人webHook地址
    @NotBlank(message = "群机器人地址不能为空")
    private String webHookUrl;
    // 是否推送给所有人（在群里@所有人）
    private Boolean isAll;
    // 推送人员电话名单（在群里@这些人）
    private List<String> mobileList;
    // 发送内容
    @NotBlank(message = "发送消息不能为空")
    private String content;
}
