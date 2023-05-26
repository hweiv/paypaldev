package com.jimi.mapper;

import com.jimi.entity.PaymentVo;
import com.jimi.entity.PaypalPaymentInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaypalPaymentMapper {
    int insertBatchPayment(List<PaypalPaymentInfo> list);

    List<PaypalPaymentInfo> selectByTransactionId(String transactionID);

    void updateDelFlatByTransactionId(String transactionID);

    int queryAllPayment(PaymentVo paymentVo);

    List<PaypalPaymentInfo> queryAllPaymentPage(PaymentVo paymentVo);

    int updatePushFlatByIds(List<PaypalPaymentInfo> paymentInfos);

    List<PaypalPaymentInfo> selectByIdList(List<String> paymentIdList);

    List<String> queryAccountList();
}
