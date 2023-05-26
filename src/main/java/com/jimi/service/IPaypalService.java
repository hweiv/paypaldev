package com.jimi.service;

import com.jimi.entity.*;
import com.jimi.exception.BusinessException;

import java.text.ParseException;
import java.util.List;

public interface IPaypalService {
    /**
     * 获取支付记录
     * @return
     */
    List<PaypalPaymentDto> pushPayment(PaymentParamVo paramVo) throws BusinessException;

    PaymentDtoPageInfo queryAllPayment(PaymentVo paymentVo) throws BusinessException, ParseException;

    List<PaypalPaymentDto> queryFromPaypal(PaymentParamVo paramVo) throws BusinessException, ParseException;

    boolean pushPaymentList(List<String> paymentIdList);

    List<PaypalAccount> queryAccountList();

    /*
    Payment createPayment(Double v, String usd, PaypalPaymentMethod paypal, PaypalPaymentIntent sale, String payment_description, String cancelUrl, String successUrl) throws PayPalRESTException;

    Payment executePayment(String paymentId, String payerId) throws PayPalRESTException;

     */
}
