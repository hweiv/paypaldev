package com.jimi.mapper;

import com.jimi.entity.BankInfo;
import com.jimi.entity.BankPaymentInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BankMapper {
    // 批量插入数据
    int insertBatchBankInfo(List<BankInfo> list);

    // 批量插入数据
    int insertBankInfo(BankInfo info);

    // 通过银行账号查询数据
    List<BankInfo> selectByBindAccount(String bindAccount);

    // 通过银行账号查询数据
    List<String> selectRobotByBindAccount(String bindAccount);

}
