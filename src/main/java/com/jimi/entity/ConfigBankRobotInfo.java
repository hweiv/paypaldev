package com.jimi.entity;

import com.jimi.utils.UUIDUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigBankRobotInfo {
    private String id;
    private String bankId;
    private String dingRobotId;
    // 是否有效：0-有效，1-删除
    private String delFlat;
    private Date createTime;
    private Date updateTime;
    private String createBy;
    private String updateBy;

    public void add(ConfigBankRobotInfo info) {
        info.setId(UUIDUtil.getUUID());
        info.setCreateBy("SYSTEM");
        info.setUpdateBy("SYSTEM");
        info.setCreateTime(new Date());
        info.setUpdateTime(new Date());
    }
}
