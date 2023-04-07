package com.jimi.service;

import com.jimi.entity.PaymentVo;
import com.jimi.entity.PaypalPaymentDto;
import com.jimi.entity.PaypalPaymentInfo;
import com.jimi.exception.BusinessException;

import java.text.ParseException;
import java.util.List;

public interface IPaypalService {
    /**
     * 获取支付记录
     * @return
     */
    List<PaypalPaymentDto> pushPayment(String startTime, String endTime) throws BusinessException;

    List<PaypalPaymentInfo> queryAllPayment(PaymentVo paymentVo) throws BusinessException, ParseException;

    List<PaypalPaymentDto> queryFromPaypal(PaymentVo paymentVo) throws BusinessException, ParseException;

    /*
    Payment createPayment(Double v, String usd, PaypalPaymentMethod paypal, PaypalPaymentIntent sale, String payment_description, String cancelUrl, String successUrl) throws PayPalRESTException;

    Payment executePayment(String paymentId, String payerId) throws PayPalRESTException;

     */
}
