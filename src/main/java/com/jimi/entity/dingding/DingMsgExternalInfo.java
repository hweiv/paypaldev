package com.jimi.entity.dingding;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class DingMsgExternalInfo {
    // 系统编码
    private String systemCode;
    // 钉钉推送内容
    private List<DingDingMsgData> list;
}
