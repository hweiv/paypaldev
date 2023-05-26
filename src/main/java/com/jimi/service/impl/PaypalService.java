package com.jimi.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jimi.common.constant.PaypalTransConstant;
import com.jimi.common.dingding.DingDingPush;
import com.jimi.common.redis.RedisCacheUtil;
import com.jimi.entity.*;
import com.jimi.exception.BusinessException;
import com.jimi.mapper.PaypalPaymentMapper;
import com.jimi.service.IPaypalService;
import com.jimi.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.TransactionSearchReq;
import urn.ebay.api.PayPalAPI.TransactionSearchRequestType;
import urn.ebay.api.PayPalAPI.TransactionSearchResponseType;
import urn.ebay.apis.eBLBaseComponents.PaymentTransactionSearchResultType;

@Service
public class PaypalService implements IPaypalService {
    private static final Logger logger = LoggerFactory.getLogger(PaypalService.class);

    @Value("${paypal.mode}")
    private String mode;

    @Value("${paypal.tob.dingding.webhook}")
    private String tobPayPalRobotUrl;

    @Value("${paypal.toc.dingding.webhook}")
    private String tocPayPalRobotUrl;

    @Value("#{'${paypal.busi.tob.app.usernameList:}'.split(',')}")
    private List<String> userListTob;

    @Value("#{'${paypal.busi.toc.app.usernameList:}'.split(',')}")
    private List<String> userListToc;

    private static final String PAYMENT_KEY = "PAYPALPAYMENT_";
    private static final String PAYMENT_SET_KEY = "PAYPALPAYMENT_SET_KEY";
    private static final String PAYPALPAYMENT_ACCOUNT_LIST_KEY = "PAYPALPAYMENT_ACCOUNT_LIST";

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
    public List<PaypalPaymentDto> pushPayment(PaymentParamVo paramVo) throws BusinessException{
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.put("mode", mode);

        // Account Credential
        configMap.put("acct1.UserName", paramVo.getUsername());
        configMap.put("acct1.Password", paramVo.getPassword());
        configMap.put("acct1.Signature", paramVo.getSignature());

        TransactionSearchReq txnreq = new TransactionSearchReq();
        TransactionSearchRequestType requestType = new TransactionSearchRequestType();
        requestType.setStartDate(paramVo.getStartTime());
        requestType.setEndDate(paramVo.getEndTime());
        requestType.setVersion("95.0");
        requestType.setTransactionID("");
        txnreq.setTransactionSearchRequest(requestType);
        PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(configMap);
        try {
            logger.info("-PaypalService-pushPayment.transactions:{}", JSON.toJSONString(txnreq));
            TransactionSearchResponseType txnresponse = service.transactionSearch(txnreq, configMap.get("acct1.UserName"));
            List<PaymentTransactionSearchResultType> transactionList = txnresponse.getPaymentTransactions();
            // 缓存进行校验得到新的交易单信息
            List<PaymentTransactionSearchResultType> transactions = checkRedisCache(transactionList);

            logger.info("-PaypalService-pushPayment.transactions:{}", JSON.toJSONString(transactions));
            List<PaypalPaymentDto> addPaymentDtoList = new ArrayList<>();
            List<PaypalPaymentDto> pushPaymentDtoList = new ArrayList<>();
            // 获取今天的日期
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
            Date now = new Date();
            String todayDate = sdf.format(now);
            String key = PAYMENT_SET_KEY + todayDate;
            Set<String> cacheSet = redisCacheUtil.getCacheSet(key);
            Set<String> set = new HashSet<>(); // 新的交易单号
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
                    logger.info("PaypalService.pushPayment 单号为:{}, 已存在数据:{}",transaction.getTransactionID(), JSON.toJSONString(paymentDto));
                    mapper.updateDelFlatByTransactionId(transaction.getTransactionID());
                }
                // grossAmount数值小于0 或者 付款账户为空则不推送
                if (Double.parseDouble(transaction.getGrossAmount().getValue()) >= 0 && StringUtils.isNotBlank(transaction.getPayer()) &&
                        !cacheSet.contains(paymentDto.getTransactionID())) { // 今天推送信息缓存不包含该单号
                    paymentDto.setPushFlat("0");
                    pushPaymentDtoList.add(paymentDto);
                    set.add(paymentDto.getTransactionID());
                } else {
                    paymentDto.setPushFlat("1");
                }
                addPaymentDtoList.add(paymentDto);
            }
            logger.info("-PaypalService-pushPayment.addPaymentDtoList:{}", JSON.toJSONString(addPaymentDtoList));
            // 绑定账户
            String bindAccount = paramVo.getUsername().replace("_api1.", "@");
            sendDingMsg(pushPaymentDtoList, bindAccount, paramVo.getWebHookUrl());
            if (set.size() > 0) {
                set.addAll(cacheSet);
                logger.info("-PaypalService-checkRedisCache key:{}, 新的缓存数据为:{}", key, JSON.toJSONString(set));
                // 设置新的缓存，并且同时设置过期时间为7天
                redisCacheUtil.setCacheSet(key, set);
                redisCacheUtil.expire(key, 7, TimeUnit.DAYS);
            }
            List<PaypalPaymentInfo> addPaymentInfos = new ArrayList<>();
            for (PaypalPaymentDto paymentDto : addPaymentDtoList) {
                PaypalPaymentInfo paymentInfo = paymentDto.toInfo();
                paymentInfo.setNativeTime(DateUtils.timeStrToDateGMT(paymentDto.getTimestamp(), DateUtils.TIME_STR_T_Z));
                paymentInfo.setBindAccount(bindAccount);
                String bindAccountName = PaypalTransConstant.BIND_ACCOUNT_NAME.get(bindAccount);
                paymentInfo.setBindAccountName(bindAccountName);
                addPaymentInfos.add(paymentInfo);
            }
            int count = 0;
            if (addPaymentInfos.size() > 0) {
                count = mapper.insertBatchPayment(addPaymentInfos);
            }
            logger.info("成功插入数据-PaypalService-pushPayment.count:{}", JSON.toJSONString(count));
            if (count > 0) {
                return addPaymentDtoList;
            } else {
                logger.info("-PaypalService-pushPayment 没有新数据");
            }
        } catch (Exception e) {
            logger.error("-PaypalService-getPayment is error:{}", e);
            throw new BusinessException(e.getMessage());
        }
        return new ArrayList<>();
    }

    @Transactional
    public void sendDingMsg(List<PaypalPaymentDto> resultList, String bindAccount, String webHook) {
        if (resultList.size() > 0) {
            String content = getContent(resultList, bindAccount);
            ArrayList<String> mobileList = Lists.newArrayList();
            DingDingPush.sendMsgToGroupChat(webHook, false, mobileList, content);
        }
    }

    /**
     * 获取消息文案
     *
     * @param paypalPaymentDtos
     * @param bindAccount
     * @return
     */
    private String getContent(List<PaypalPaymentDto> paypalPaymentDtos, String bindAccount) {
        StringBuffer sb = new StringBuffer();
        sb.append("PayPal消息通知：").append("\n");
        String bindAccountName = PaypalTransConstant.BIND_ACCOUNT_NAME.get(bindAccount);

        for (PaypalPaymentDto paymentDto : paypalPaymentDtos) {
            String timeStamp = DateUtils.transUTCToStrGMT8(paymentDto.getTimestamp());
            sb.append("交易单号：").append(paymentDto.getTransactionID()).append("\n")
                    .append("账户名称：").append(bindAccountName).append("\n")
                    .append("关联账户：").append(bindAccount).append("\n")
                    .append("付款来源：").append(paymentDto.getPayerDisplayName()).append("\n")
                    .append("交易金额：").append(paymentDto.getGrossValue()).append("  ").append(paymentDto.getGrossCurrency()).append("\n")
                    .append("手续费用：").append(paymentDto.getFeeValue()).append("  ").append(paymentDto.getFeeCurrency()).append("\n")
                    .append("实际到账：").append(paymentDto.getNetValue()).append("  ").append(paymentDto.getNetCurrency()).append("\n")
                    .append("交易时间：").append(timeStamp).append("\n")
                    .append("交易状态：").append(paymentDto.getStatus()).append("\n")
                    .append("交易类型：").append(paymentDto.getType()).append("\n").append("\n");
        }
        logger.info("PaypalService-getContent result:{}", sb.toString());
        return sb.toString();
    }

    @Override
    public PaymentDtoPageInfo queryAllPayment(PaymentVo paymentVo) {
        logger.info("PaypalService-queryAllPayment.paymentVo:{}", JSON.toJSONString(paymentVo));
        if (Objects.isNull(paymentVo)) {
            paymentVo.setPage(1);
            paymentVo.setPageSize(10);
        }
        PaymentDtoPageInfo pageInfo = new PaymentDtoPageInfo();
        pageInfo.setPage(paymentVo.getPage());
        pageInfo.setPageSize(paymentVo.getPageSize());
        int total = mapper.queryAllPayment(paymentVo);
        List<PaypalPaymentInfo> resultList = mapper.queryAllPaymentPage(paymentVo);
        logger.info("PaypalService-queryAllPayment.resultList:{}", JSON.toJSONString(resultList));
        List<PaymentRespVo> paymentRespVos = new ArrayList<>();
        for (PaypalPaymentInfo info : resultList) {
            PaymentRespVo respVo = new PaymentRespVo();
            BeanUtils.copyProperties(info, respVo);
            paymentRespVos.add(respVo);
        }
        pageInfo.setList(paymentRespVos);
        pageInfo.setTotal(total);
        logger.info("PaypalService-queryAllPayment.paymentRespVos:{}", JSON.toJSONString(paymentRespVos));
        return pageInfo;
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
        logger.info("-PaypalService-checkRedisCache key:{}, 原缓存数据为:{}", key, JSON.toJSONString(cacheSet));
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
            logger.info("-PaypalService-checkRedisCache key:{}, 新的缓存数据为:{}", key, JSON.toJSONString(set));
            // 设置新的缓存，并且同时设置过期时间为7天
            redisCacheUtil.setCacheSet(key, set);
            redisCacheUtil.expire(key, 7, TimeUnit.DAYS);
        }
        logger.info("-PaypalService-checkRedisCache 新数据有:{}条, 新增数据为:{}", newPaymentList.size(), JSON.toJSONString(newPaymentList));
        return newPaymentList;
    }

    /**
     * 查询Paypal的数据
     * @param paramVo
     * @return
     * @throws BusinessException
     */
    @Override
    public List<PaypalPaymentDto> queryFromPaypal(PaymentParamVo paramVo) throws BusinessException, ParseException {
        if (Objects.isNull(paramVo) || paramVo.getStartTime() == null) {
            throw new BusinessException("参数错误或者缺失");
        }
        logger.info("PaypalService-queryFromPaypal.paymentVo:{}", JSON.toJSONString(paramVo));
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.put("mode", mode);
        configMap.put("acct1.UserName", paramVo.getUsername());
        configMap.put("acct1.Password", paramVo.getPassword());
        configMap.put("acct1.Signature", paramVo.getSignature());
        String startTime = DateUtils.transStrToUTCGMT(paramVo.getStartTime());
        String endTime = null;
        if (StringUtils.isNotBlank(paramVo.getEndTime())) {
            endTime = DateUtils.transStrToUTCGMT(paramVo.getEndTime());
        }
        TransactionSearchReq txnreq = new TransactionSearchReq();
        TransactionSearchRequestType requestType = new TransactionSearchRequestType();
        requestType.setStartDate(startTime);
        requestType.setEndDate(endTime);
        requestType.setVersion("95.0");
        requestType.setTransactionID("");
        txnreq.setTransactionSearchRequest(requestType);
        PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(configMap);
        logger.info("-PaypalService-queryFromPaypal.configMap:{}", JSON.toJSONString(configMap));
        try {
            TransactionSearchResponseType txnresponse = service.transactionSearch(txnreq, configMap.get("acct1.UserName"));
            List<PaymentTransactionSearchResultType> transactionList = txnresponse.getPaymentTransactions();

            logger.info("-PaypalService-queryFromPaypal.transactions:{}", JSON.toJSONString(transactionList));
            List<PaypalPaymentDto> addPaymentDtoList = new ArrayList<>();
            getPaymentDtoList(transactionList, addPaymentDtoList);
            logger.info("-PaypalService-queryFromPaypal.addPaymentDtoList:{}", JSON.toJSONString(addPaymentDtoList));
            return addPaymentDtoList;
        } catch (Exception e) {
            logger.error("-PaypalService-queryFromPaypal.getPayment is error:{}", e);
            throw new BusinessException(e.getMessage());
        }
    }

    private void getPaymentDtoList(List<PaymentTransactionSearchResultType> transactionList, List<PaypalPaymentDto> addPaymentDtoList) {
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
    }

    @Override
    @Transactional
    public boolean pushPaymentList(List<String> paymentIdList) {
        logger.info("-PaypalService-pushPaymentList.paymentIdList:{}", JSON.toJSONString(paymentIdList));
        Set<String> setB = new HashSet<>();
        userListTob.forEach(account -> setB.add(account.replace("_api1.", "@")));
        Set<String> setC = new HashSet<>();
        userListToc.forEach(account -> setC.add(account.replace("_api1.", "@")));
        try {
            if (paymentIdList.size() < 1) {
                return true;
            } else {
                List<PaypalPaymentInfo> paymentInfos = mapper.selectByIdList(paymentIdList);
                logger.info("-PaypalService-pushPaymentList.paymentRespVos:{}", JSON.toJSONString(paymentInfos));
                List<PaypalPaymentInfo> tobPaymentInfos = new ArrayList<>();
                List<PaypalPaymentInfo> tocPaymentInfos = new ArrayList<>();
                for (PaypalPaymentInfo paymentInfo : paymentInfos) {
                    if (setB.contains(paymentInfo.getBindAccount())) {
                        tobPaymentInfos.add(paymentInfo);
                    }
                    if (setC.contains(paymentInfo.getBindAccount())) {
                        tocPaymentInfos.add(paymentInfo);
                    }
                }
                String contentB = getContent(tobPaymentInfos);
                ArrayList<String> mobileListB = Lists.newArrayList();
                DingDingPush.sendMsgToGroupChat(tobPayPalRobotUrl, false, mobileListB, contentB);
                String contentC = getContent(tocPaymentInfos);
                ArrayList<String> mobileListC = Lists.newArrayList();
                DingDingPush.sendMsgToGroupChat(tocPayPalRobotUrl, false, mobileListC, contentC);
                int count = mapper.updatePushFlatByIds(paymentInfos);
                logger.info("-PaypalService-pushPaymentList.count:{}", JSON.toJSONString(count));
                return true;
            }
        } catch (Exception e) {
            logger.error("-PaypalService-pushPaymentList error", e);
            return false;
        }
    }

    private String getContent(List<PaypalPaymentInfo> paymentInfos) {
        StringBuffer sb = new StringBuffer();
        sb.append("PayPal消息通知：").append("\n");
        for (PaypalPaymentInfo info : paymentInfos) {
            sb.append("交易单号：").append(info.getTransactionID()).append("\n")
                    .append("账户名称：").append(info.getBindAccountName()).append("\n")
                    .append("关联账户：").append(info.getBindAccount()).append("\n")
                    .append("付款来源：").append(info.getPayerDisplayName()).append("\n")
                    .append("交易金额：").append(info.getGrossValue()).append("  ").append(info.getGrossCurrency()).append("\n")
                    .append("手续费用：").append(info.getFeeValue()).append("  ").append(info.getFeeCurrency()).append("\n")
                    .append("实际到账：").append(info.getNetValue()).append("  ").append(info.getNetCurrency()).append("\n")
                    .append("交易时间：").append(info.getNativeTime()).append("\n")
                    .append("交易状态：").append(info.getStatus()).append("\n")
                    .append("交易类型：").append(info.getType()).append("\n").append("\n");
        }
        logger.info("PaypalService-getContent2 result:{}", sb.toString());
        return sb.toString();
    }

    @Override
    public List<PaypalAccount> queryAccountList() {
        List<PaypalAccount> resultList = new ArrayList<>();
        String cacheObject = redisCacheUtil.getCacheObject(PAYPALPAYMENT_ACCOUNT_LIST_KEY);
        logger.info("PaypalService-queryAccountList.getCacheObject:{}", cacheObject);
        if (cacheObject != null) {
            resultList = JSONObject.parseArray(cacheObject).toJavaList(PaypalAccount.class);
            return resultList;
        }
        List<String> accountList = mapper.queryAccountList();
        for (String s : accountList) {
            if (StringUtils.isBlank(s)) continue;
            PaypalAccount paypalAccount = new PaypalAccount();
            paypalAccount.setBindAccount(s);
            paypalAccount.setBindAccountName(PaypalTransConstant.BIND_ACCOUNT_NAME.get(s));
            resultList.add(paypalAccount);
        }
        logger.info("PaypalService-queryAccountList:{}", resultList);
        redisCacheUtil.setCacheObject(PAYPALPAYMENT_ACCOUNT_LIST_KEY, JSON.toJSONString(resultList), 1, TimeUnit.DAYS);
        return resultList;
    }
}
