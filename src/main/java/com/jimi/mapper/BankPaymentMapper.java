package com.jimi.mapper;

import com.jimi.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface BankPaymentMapper {
    // 批量插入数据
    int insertBatchPayment(List<BankPaymentInfo> list);

    // 通过交易单号查询数据
    List<BankPaymentInfo> selectByTransactionId(String transactionID);

    // 通过交易单号失效数据
    void updateDelFlatByTransactionId(String transactionID);

    // 通过交易单号更新推送数据状态
    void updatePushFlatByTransactionId(String transactionID);

    // 查询时间段内新增的数据
    List<BankPaymentInfo> selectNewData(@Param("pushFlat") String pushFlat, @Param("type") String type);

    // 查询时间段内新增的数据--国际业务（外币，除CNY）
    List<BankPaymentInfo> selectInterNewData(@Param("pushFlat") String pushFlat, @Param("type") String type);

    // 查询时间段内新增的数据--人民币结算数据
    List<BankPaymentInfo> selectDomesticNewData(@Param("pushFlat") String pushFlat, @Param("type") String type);

    // 查询银行账户列表
    List<Map<String, Object>> selectBankList();

    // 通过银行账号查询支付数据
    List<BankPaymentInfo> queryPaymentsByBankAccounts(@Param("list")List<String> bankAccounts, @Param("pushFlat")String pushFlat, @Param("type")String type);

    // 查询银行账号的银行编码
    List<BankAccountDto> queryBankCodeList();

    // 根据银行编码查询银行账号
    List<String> queryBankAccountListByBankCode(@Param("bankCode") String bankCode);

    // 根据条件查询总数
    int queryBankDataCount(BankReqVo bankReqVo);

    // 分页查询数据
    List<BankPaymentInfo> queryBankDataPage(BankReqVo bankReqVo);

    // 根据id查询
    List<BankPaymentInfo> selectByIdList(List<String> paymentIdList);
}
