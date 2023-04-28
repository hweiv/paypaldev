package com.jimi.mapper;

import com.jimi.entity.BankInfo;
import com.jimi.entity.BankPaymentInfo;
import com.jimi.entity.ConfigBankRobotInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConfigBankRobotMapper {
    // 批量插入数据
    int insertBatch(List<ConfigBankRobotInfo> list);

    // 批量插入数据
    int insert(ConfigBankRobotInfo info);

}
