package com.jimi.mapper;

import com.jimi.entity.BankInfo;
import com.jimi.entity.BankPaymentInfo;
import com.jimi.entity.DingRobotInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DingRobotMapper {
    // 批量插入数据
    int insertBatchDingRobotInfo(List<DingRobotInfo> list);

    // 批量插入数据
    int insertDingRobotInfo(DingRobotInfo info);

}
