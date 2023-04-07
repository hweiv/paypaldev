package com.jimi.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jimi.common.constant.PaypalTransConstant;
import com.jimi.common.dingding.DingDingPush;
import com.jimi.common.redis.RedisCacheUtil;
import com.jimi.entity.PaymentVo;
import com.jimi.entity.PaypalPaymentDto;
import com.jimi.entity.PaypalPaymentInfo;
import com.jimi.exception.BusinessException;
import com.jimi.mapper.PaypalPaymentMapper;
import com.jimi.service.IPaypalService;
import com.jimi.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.TransactionSearchReq;
import urn.ebay.api.PayPalAPI.TransactionSearchRequestType;
import urn.ebay.api.PayPalAPI.TransactionSearchResponseType;
import urn.ebay.apis.eBLBaseComponents.PaymentTransactionClassCodeType;
import urn.ebay.apis.eBLBaseComponents.PaymentTransactionSearchResultType;

@Service
@Slf4j
public class PaypalService implements IPaypalService {

    @Value("${paypal.mode}")
    private String mode;

    @Value("${paypal.busi.username}")
    private String username;

    @Value("${paypal.busi.password}")
    private String password;

    @Value("${paypal.busi.signature}")
    private String signature;

    @Value("${dingding.webhook}")
    private String webHook;

    private final static String PAYMENT_KEY = "PAYPALPAYMENT_";

//    @Autowired
//    private APIContext apiContext;

    @Autowired
    private PaypalPaymentMapper mapper;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    /**
     * 获取全量的支付记录
     *
     * @return
     */
    @Override
    @Transactional
    public List<PaypalPaymentDto> pushPayment(String startTime, String endTime) throws BusinessException{
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.put("mode", mode);

        // Account Credential
        configMap.put("acct1.UserName", username);
        configMap.put("acct1.Password", password);
        configMap.put("acct1.Signature", signature);
        // Subject is optional, only required in case of third party permission
        //configMap.put("acct1.Subject", "");
        // Sample Certificate credential
        // configMap.put("acct2.UserName", "certuser_biz_api1.paypal.com");
        // configMap.put("acct2.Password", "D6JNKKULHN3G5B8A");
        // configMap.put("acct2.CertKey", "password");
        // configMap.put("acct2.CertPath", "resource/sdk-cert.p12");
        // configMap.put("acct2.AppId", "APP-80W284485P519543T");

        TransactionSearchReq txnreq = new TransactionSearchReq();
        TransactionSearchRequestType requestType = new TransactionSearchRequestType();
        requestType.setStartDate(startTime);
        requestType.setEndDate(endTime);
        requestType.setVersion("95.0");
        requestType.setTransactionID("");
        txnreq.setTransactionSearchRequest(requestType);
        PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(configMap);
        try {
            log.info("-PaypalService-pushPayment.transactions:{}", JSON.toJSONString(txnreq));
            TransactionSearchResponseType txnresponse = service.transactionSearch(txnreq, configMap.get("acct1.UserName"));
            List<PaymentTransactionSearchResultType> transactionList = txnresponse.getPaymentTransactions();
            // 缓存进行校验得到新的交易单信息
            List<PaymentTransactionSearchResultType> transactions = checkRedisCache(transactionList);

            log.info("-PaypalService-pushPayment.transactions:{}", JSON.toJSONString(transactions));
            List<PaypalPaymentDto> addPaymentDtoList = new ArrayList<>();
            for (PaymentTransactionSearchResultType transaction : transactions) {
                PaypalPaymentDto paymentDto = new PaypalPaymentDto();
                BeanUtils.copyProperties(transaction, paymentDto);
                paymentDto.setGrossValue(transaction.getGrossAmount().getValue());
                paymentDto.setGrossCurrency(transaction.getGrossAmount().getCurrencyID().toString());
                paymentDto.setFeeValue(transaction.getFeeAmount().getValue());
                paymentDto.setFeeCurrency(transaction.getFeeAmount().getCurrencyID().toString());
                paymentDto.setNetValue(transaction.getNetAmount().getValue());
                paymentDto.setNetCurrency(transaction.getNetAmount().getCurrencyID().toString());
                // 根据交易单号查询表中数据是否存在，不存在直接插入，存在则把原来的单号数据失效，再插入
                List<PaypalPaymentInfo> paymentInfos = mapper.selectByTransactionId(transaction.getTransactionID());
                if (paymentInfos.size() > 0) {
                    log.info("PaypalService.pushPayment 单号为:{}, 已存在数据:{}",transaction.getTransactionID(), JSON.toJSONString(paymentDto));
                    mapper.updateDelFlatByTransactionId(transaction.getTransactionID());
                }
                addPaymentDtoList.add(paymentDto);
            }
            log.info("-PaypalService-pushPayment.addPaymentDtoList:{}", JSON.toJSONString(addPaymentDtoList));
            sendDingMsg(addPaymentDtoList);
            List<PaypalPaymentInfo> addPaymentInfos = new ArrayList<>();
            for (PaypalPaymentDto paymentDto : addPaymentDtoList) {
                PaypalPaymentInfo paymentInfo = paymentDto.toInfo();
                paymentInfo.setNativeTime(DateUtils.timeStrToDateGMT8(paymentDto.getTimestamp(), DateUtils.TIME_STR_T_Z));
                paymentInfo.setPushFlat("0");
                addPaymentInfos.add(paymentInfo);
            }
            int count = 0;
            if (addPaymentInfos.size() > 0) {
                count = mapper.insertBatchPayment(addPaymentInfos);
            }
            log.info("成功插入数据-PaypalService-pushPayment.count:{}", JSON.toJSONString(count));
            if (count > 0) {
                return addPaymentDtoList;
            } else {
                log.info("-PaypalService-pushPayment 没有新数据");
            }
        } catch (Exception e) {
            log.error("-PaypalService-getPayment is error:{}", e);
            throw new BusinessException(e.getMessage());
        }
        return new ArrayList<>();
    }

    @Transactional
    public void sendDingMsg(List<PaypalPaymentDto> resultList) {
        if (resultList.size() > 0) {
            String content = getContent(resultList);
            ArrayList<String> mobileList = Lists.newArrayList();
            DingDingPush.sendMsgToGroupChat(webHook, false, mobileList, content);
        }
    }

    /**
     * 获取消息文案
     *
     * @param paypalPaymentDtos
     * @return
     */
    private static String getContent(List<PaypalPaymentDto> paypalPaymentDtos) {
        StringBuffer sb = new StringBuffer();
        sb.append("PayPal消息通知：").append("\n");

        for (PaypalPaymentDto paymentDto : paypalPaymentDtos) {
            String timeStamp = DateUtils.transUTCToStrGMT8(paymentDto.getTimestamp());
            sb.append("交易单号：").append(paymentDto.getTransactionID()).append("\n")
                    .append("付款账户：").append(paymentDto.getPayer()).append("\n")
                    .append("付款来源：").append(paymentDto.getPayerDisplayName()).append("\n")
                    .append("交易金额：").append(paymentDto.getGrossValue()).append("  ").append(paymentDto.getGrossCurrency()).append("\n")
                    .append("手续费用：").append(paymentDto.getFeeValue()).append("  ").append(paymentDto.getFeeCurrency()).append("\n")
                    .append("实际到账：").append(paymentDto.getNetValue()).append("  ").append(paymentDto.getNetCurrency()).append("\n")
                    .append("交易时间：").append(timeStamp).append("\n")
                    .append("交易状态：").append(PaypalTransConstant.PAY_STATUS.get(paymentDto.getStatus())).append("\n")
                    .append("交易类型：").append(PaypalTransConstant.PAY_TYPE.get(paymentDto.getType())).append("\n").append("\n");
        }
        log.info("PaypalService-getContent result:{}", sb.toString());
        return sb.toString();
    }

    @Override
    public List<PaypalPaymentInfo> queryAllPayment(PaymentVo paymentVo) {
        log.info("PaypalService-queryAllPayment.paymentVo:{}", JSON.toJSONString(paymentVo));
        List<PaypalPaymentInfo> resultList = mapper.queryAllPayment(paymentVo);
        log.info("PaypalService-queryAllPayment.resultList:{}", JSON.toJSONString(resultList));
        return resultList;
    }

    // 缓存校验得到交易单的增量信息
    private List<PaymentTransactionSearchResultType> checkRedisCache(List<PaymentTransactionSearchResultType> transactionList) {
        List<PaymentTransactionSearchResultType> newPaymentList = new ArrayList<>();
        // 获取今天的日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        Date now = new Date();
        String todayDate = sdf.format(now);
        String key = PAYMENT_KEY + todayDate;
        Set<String> cacheSet = redisCacheUtil.getCacheSet(key);
        log.info("-PaypalService-checkRedisCache key:{}, 原缓存数据为:{}", key, JSON.toJSONString(cacheSet));
        Set<String> set = new HashSet<>();
        for (PaymentTransactionSearchResultType type : transactionList) {
            String transactionID = type.getTransactionID(); // 事件id
            String timestamp = type.getTimestamp(); // 时间戳
            String timezone = type.getTimezone();
            String status = type.getStatus(); // 状态
            String typeType = type.getType(); // 类型
            String value = String.join("_", transactionID, timestamp, timezone, status, typeType);
            if (!cacheSet.contains(value)) {
                newPaymentList.add(type);
                set.add(value);
            }
        }
        if (set.size() > 0) {
            set.addAll(cacheSet);
            log.info("-PaypalService-checkRedisCache key:{}, 新的缓存数据为:{}", key, JSON.toJSONString(set));
            // 设置新的缓存，并且同时设置过期时间为7天
            redisCacheUtil.setCacheSet(key, set);
            redisCacheUtil.expire(key, 7, TimeUnit.DAYS);
        }
        log.info("-PaypalService-checkRedisCache 新数据有:{}条, 新增数据为:{}", newPaymentList.size(), JSON.toJSONString(newPaymentList));
        return newPaymentList;
    }

    /**
     * 查询Paypal的数据
     * @param paymentVo
     * @return
     * @throws BusinessException
     */
    @Override
    public List<PaypalPaymentDto> queryFromPaypal(PaymentVo paymentVo) throws BusinessException, ParseException {
        if (Objects.isNull(paymentVo) || paymentVo.getStartTime() == null) {
            throw new BusinessException("参数错误或者缺失");
        }
        log.info("PaypalService-queryFromPaypal.paymentVo:{}", JSON.toJSONString(paymentVo));
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.put("mode", mode);
        configMap.put("acct1.UserName", username);
        configMap.put("acct1.Password", password);
        configMap.put("acct1.Signature", signature);
        String startTime = DateUtils.dateToStringGMT(paymentVo.getStartTime(), DateUtils.TIME_STR_T_Z);
//        String startTime = DateUtils.transStrToUTCGMT(paymentVo.getStartTime());
        String endTime = null;
        if (paymentVo.getEndTime() != null) {
            endTime = DateUtils.dateToStringGMT(paymentVo.getEndTime(), DateUtils.TIME_STR_T_Z);
//            endTime = DateUtils.transStrToUTCGMT(paymentVo.getEndTime());
        }
        TransactionSearchReq txnreq = new TransactionSearchReq();
        TransactionSearchRequestType requestType = new TransactionSearchRequestType();
        requestType.setStartDate(startTime);
        requestType.setEndDate(endTime);
        requestType.setVersion("95.0");
        requestType.setTransactionID("");
        txnreq.setTransactionSearchRequest(requestType);
        PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(configMap);
        try {
            log.info("-PaypalService-pushPayment.transactions:{}", JSON.toJSONString(txnreq));
            TransactionSearchResponseType txnresponse = service.transactionSearch(txnreq, configMap.get("acct1.UserName"));
            List<PaymentTransactionSearchResultType> transactionList = txnresponse.getPaymentTransactions();

            log.info("-PaypalService-pushPayment.transactions:{}", JSON.toJSONString(transactionList));
            List<PaypalPaymentDto> addPaymentDtoList = new ArrayList<>();
            for (PaymentTransactionSearchResultType transaction : transactionList) {
                PaypalPaymentDto paymentDto = new PaypalPaymentDto();
                BeanUtils.copyProperties(transaction, paymentDto);
                paymentDto.setGrossValue(transaction.getGrossAmount().getValue());
                paymentDto.setGrossCurrency(transaction.getGrossAmount().getCurrencyID().toString());
                paymentDto.setFeeValue(transaction.getFeeAmount().getValue());
                paymentDto.setFeeCurrency(transaction.getFeeAmount().getCurrencyID().toString());
                paymentDto.setNetValue(transaction.getNetAmount().getValue());
                paymentDto.setNetCurrency(transaction.getNetAmount().getCurrencyID().toString());
                addPaymentDtoList.add(paymentDto);
            }
            log.info("-PaypalService-pushPayment.addPaymentDtoList:{}", JSON.toJSONString(addPaymentDtoList));
            return addPaymentDtoList;
        } catch (Exception e) {
            log.error("-PaypalService-getPayment is error:{}", e);
            throw new BusinessException(e.getMessage());
        }
    }
}
