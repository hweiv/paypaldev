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

    List<PaypalPaymentInfo> queryAllPayment(PaymentVo paymentVo);
}
