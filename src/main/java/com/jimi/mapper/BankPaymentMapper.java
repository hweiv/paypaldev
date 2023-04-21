package com.jimi.mapper;

import com.jimi.entity.BankPaymentInfo;
import com.jimi.entity.PaymentVo;
import com.jimi.entity.PaypalPaymentInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BankPaymentMapper {
    int insertBatchPayment(List<BankPaymentInfo> list);

    List<BankPaymentInfo> selectByTransactionId(String transactionID);

    void updateDelFlatByTransactionId(String transactionID);

    int queryAllPayment(PaymentVo paymentVo);

    List<BankPaymentInfo> queryAllPaymentPage(PaymentVo paymentVo);

    int updatePushFlatByIds(List<BankPaymentInfo> paymentInfos);

    List<BankPaymentInfo> selectByIdList(List<String> paymentIdList);
}
