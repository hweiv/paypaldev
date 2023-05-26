package com.jimi.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jimi.common.ApiResult;
import com.jimi.common.dingding.DingDingPush;
import com.jimi.common.redis.RedisCacheUtil;
import com.jimi.entity.*;
import com.jimi.exception.BusinessException;
import com.jimi.mapper.BankPaymentMapper;
//import com.jimi.mapper.CustomerAccountMapper;
import com.jimi.service.IBankService;
import com.jimi.utils.DateUtils;
import com.jimi.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BankService implements IBankService {
    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private BankPaymentMapper bankPaymentMapper;

//    @Autowired
//    private CustomerAccountMapper customerAccountMapper;

    // 国际群-ccb建行通知
    @Value("${inter.ccbbank.robot.webhook}")
    private String interCcbBankRobotUrl;

    // 国际群-spdb浦发银行通知
    @Value("${inter.spdbbank.robot.webhook}")
    private String interSpdbBankRobotUrl;

    // 国内群-ccb建行通知
    @Value("${domestic.ccbbank.robot.webhook}")
    private String domesticCcbBankRobotUrl;

    // 国内群-spdb浦发银行通知
    @Value("${domestic.spdbbank.robot.webhook}")
    private String domesticSpdbBankRobotUrl;

    // 财务群-ccb建行通知
    @Value("${finance.ccbbank.robot.webhook}")
    private String financeCcbBankRobotUrl;

    // 财务群-spdb浦发银行通知
    @Value("${finance.spdbbank.robot.webhook}")
    private String financeSpdbBankRobotUrl;

    private static final String BANK_PAYMENT_NEW_DATA_FLAG_KEY = "BANK_PAYMENT_NEW_DATA_FLAG";
    private static final String BANK_ACCOUNT_LIST_KEY = "BANK_ACCOUNT_LIST";

    private static final String YES_FLAG = "Y";

    @Override
    public ApiResult gainBankData(List<BankMsgVo> bankMsgVoList) throws Exception{
        logger.info("-BankService-gainBankData.bankMsgVoList:{}", bankMsgVoList);
        if (Objects.isNull(bankMsgVoList) || bankMsgVoList.size() < 1) {
            logger.info("-BankService-gainBankData 空数据");
            return new ApiResult().error("数据有误，列表为空");
        }
        // 入库操作
        List<BankPaymentInfo> bankInfoList = new ArrayList<>();
        try {
            for (BankMsgVo bankMsgVo : bankMsgVoList) {
                BankPaymentInfo info = bankMsgVo.toInfo();
                info.setTransactionTime(DateUtils.timeStrToDateGMT8(bankMsgVo.getTransactionTime(), DateUtils.TIME_STR));
                info.setTransactionDate(DateUtils.dateStrToDateGMT8(bankMsgVo.getTransactionDate()));
                info.setBindAccount(bankMsgVo.getAccount());
                info.setBindAccountName(bankMsgVo.getAccountName());
                info.setPushFlat("1");
                info.setBankCode(bankMsgVo.getBankCode().toUpperCase());
                bankInfoList.add(info);
                // 根据交易单号查询表中数据是否存在，不存在直接插入，存在则把原来的单号数据失效，再插入
                List<BankPaymentInfo> paymentInfos = bankPaymentMapper.selectByTransactionId(bankMsgVo.getTransactionID());
                if (paymentInfos.size() > 0) {
                    logger.info("BankService-gainBankData 单号为:{}, 已存在数据:{}",bankMsgVo.getTransactionID(), JSON.toJSONString(paymentInfos));
                    bankPaymentMapper.updateDelFlatByTransactionId(bankMsgVo.getTransactionID());
                }
            }
            if (bankInfoList.size() > 0) {
                int count = bankPaymentMapper.insertBatchPayment(bankInfoList);
                logger.info("-BankService-gainBankData 成功插入数据:{}", count);
                if (count > 0) { // 插入数据成功，写入缓存表示有新数据
                    redisCacheUtil.setCacheObject(BANK_PAYMENT_NEW_DATA_FLAG_KEY, YES_FLAG);
                }
            }
        } catch (ParseException e) {
            logger.error("BankService-gainBankData 转换异常:{}", e);
            throw new BusinessException(e.getMessage());
        }
        return ApiResult.success("发送数据成功");
    }

    @Override
    public List<Map<String, Object>> bankList() {
        List<Map<String, Object>> bankList = bankPaymentMapper.selectBankList();
        return bankList;
    }

    @Override
    public List<BankAccountDto> bankAccountList() {
        List<BankAccountDto> list = new ArrayList<>();
        String cacheObject = redisCacheUtil.getCacheObject(BANK_ACCOUNT_LIST_KEY);
        logger.info("PaypalService-queryAccountList.getCacheObject:{}", cacheObject);
        if (cacheObject != null) {
            list = JSONObject.parseArray(cacheObject).toJavaList(BankAccountDto.class);
            return list;
        }
        list = bankPaymentMapper.queryBankCodeList();
        if (list.size() < 1) {
            return list;
        }
        list.forEach(bankAccount -> {
            List<String> accountList = bankPaymentMapper.queryBankAccountListByBankCode(bankAccount.getBankCode());
            List<String> newAccountList = accountList.stream().map(s -> s.substring(s.length() - 4)).collect(Collectors.toList());
            bankAccount.setAccountList(newAccountList);
        });
        logger.info("BankService-bankAccountList.list:{}", list);
        redisCacheUtil.setCacheObject(BANK_ACCOUNT_LIST_KEY, JSON.toJSONString(list), 1, TimeUnit.DAYS);
        return list;
    }

    private String getBankName(String bankCode) {
        switch (bankCode) {
            case "CCB" : return "中国建设银行";
            case "SPDB" : return "浦发银行";
            default: return "其他银行";
        }
    }

    @Override
    public BankPaymentDtoPageInfo queryBankData(BankReqVo bankReqVo) {
        logger.info("BankService-queryBankData.bankReqVo:{}", JSON.toJSONString(bankReqVo));
        if (Objects.isNull(bankReqVo)) {
            bankReqVo.setPage(1);
            bankReqVo.setPageSize(10);
        }
        BankPaymentDtoPageInfo pageInfo = new BankPaymentDtoPageInfo();
        pageInfo.setPage(bankReqVo.getPage());
        pageInfo.setPageSize(bankReqVo.getPageSize());
        int total = bankPaymentMapper.queryBankDataCount(bankReqVo);
        List<BankPaymentInfo> resultList = bankPaymentMapper.queryBankDataPage(bankReqVo);
        logger.info("BankService-queryBankData.resultList:{}", JSON.toJSONString(resultList));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<BankPaymentRespVo> paymentRespVos = new ArrayList<>();
        for (BankPaymentInfo info : resultList) {
            BankPaymentRespVo respVo = new BankPaymentRespVo();
            BeanUtils.copyProperties(info, respVo);
            respVo.setType(getType(info.getType()));
            if (!Objects.isNull(info.getTransactionDate())) {
                respVo.setTransactionDate(dateFormat.format(info.getTransactionDate()));
            }
            paymentRespVos.add(respVo);
        }
        pageInfo.setList(paymentRespVos);
        pageInfo.setTotal(total);
        logger.info("BankService-queryBankData.paymentRespVos:{}", JSON.toJSONString(paymentRespVos));
        return pageInfo;
    }

    @Override
    public boolean pushBankPaymentList(List<String> paymentIdList) {
        List<BankPaymentInfo> resultList = bankPaymentMapper.selectByIdList(paymentIdList);
        return pushBankDataDingGroup(resultList);
    }

    @Override
    @Transactional
    public boolean pushBankDataDingGroup(List<BankPaymentInfo> bankPaymentInfos) {
        List<BankPaymentInfo> ccbPaymentList = new ArrayList<>();
        List<BankPaymentInfo> spdbPaymentList = new ArrayList<>();
        List<BankPaymentInfo> domesticCcbPaymentList = new ArrayList<>();
        List<BankPaymentInfo> domesticSpdbPaymentList = new ArrayList<>();
        List<BankPaymentInfo> interCcbPaymentList = new ArrayList<>();
        List<BankPaymentInfo> interSpdbPaymentList = new ArrayList<>();
        for (BankPaymentInfo info : bankPaymentInfos) {
            if (StringUtils.equalsIgnoreCase("CCB", info.getBankCode())) {
                ccbPaymentList.add(info);
                // todo 校对对方账户名单，在名单列表中才推送消息，不在名单列表中则不推送
                if (StringUtils.equalsIgnoreCase("CNY", info.getGrossCurrency())) {
                    domesticCcbPaymentList.add(info);
                } else {
                    interCcbPaymentList.add(info);
                }
            }
            if (StringUtils.equalsIgnoreCase("SPDB", info.getBankCode())) {
                spdbPaymentList.add(info);
                if (StringUtils.equalsIgnoreCase("CNY", info.getGrossCurrency())) {
                    domesticSpdbPaymentList.add(info);
                } else {
                    interSpdbPaymentList.add(info);
                }
            }
        }
        pushBankDataDing(ccbPaymentList, financeCcbBankRobotUrl, false);
        pushBankDataDing(spdbPaymentList, financeSpdbBankRobotUrl, false);
        pushBankDataDing(domesticCcbPaymentList, domesticCcbBankRobotUrl, true);
        pushBankDataDing(domesticSpdbPaymentList, domesticSpdbBankRobotUrl, true);
        pushBankDataDing(interCcbPaymentList, interCcbBankRobotUrl, true);
        pushBankDataDing(interSpdbPaymentList, interSpdbBankRobotUrl, true);
        // 根据交易单号修改推送状态为已推送
        bankPaymentInfos.forEach(payment -> bankPaymentMapper.updatePushFlatByTransactionId(payment.getTransactionID()));
        return true;
    }

    // 查询客户是否在名单中
//    private boolean isIncluded(String customerAccountName) {
//        int count = customerAccountMapper.selectByAccountName(customerAccountName);
//        return count > 0 ? true : false;
//    }

    // 推送消息到钉钉群
    private void pushBankDataDing(List<BankPaymentInfo> bankPaymentInfos, String robotUrl, boolean needMask) {
        if (Objects.isNull(bankPaymentInfos) || bankPaymentInfos.size() < 1) {
            return;
        }
        String content = getContent(bankPaymentInfos, needMask);
        ArrayList<String> mobileList = Lists.newArrayList();
        DingDingPush.sendMsgToGroupChat(robotUrl, false, mobileList, content);
    }

    /**
     * 获取消息文案
     *
     * @param bankPaymentInfos
     * @return
     */
    private String getContent(List<BankPaymentInfo> bankPaymentInfos, boolean needMask) {
        StringBuffer sb = new StringBuffer();
        sb.append("银行账户动账通知：").append("\n");
        for (BankPaymentInfo msgVo : bankPaymentInfos) {
            String reciprocalAccountName = "";
            if (needMask) {
                reciprocalAccountName = StringUtils.isBlank(msgVo.getReciprocalAccountName()) ? "" : StringUtil.maskStr(msgVo.getReciprocalAccountName());
            } else {
                reciprocalAccountName = StringUtils.isBlank(msgVo.getReciprocalAccountName()) ? "" : msgVo.getReciprocalAccountName();
            }
            String bankEndNum = msgVo.getBindAccount().substring(msgVo.getBindAccount().length() - 4);
            sb.append("银行名称：").append(msgVo.getBankName()).append("\n")
                    .append("交易单号：").append(msgVo.getTransactionID()).append("\n")
                    .append("账户名称：").append(msgVo.getBindAccountName()).append("\n")
                    .append("关联账户：").append("尾号").append(bankEndNum).append("\n")
                    .append("交易类型：").append(getType(msgVo.getType())).append("\n")
                    .append("对方户名：").append(reciprocalAccountName).append("\n")
                    .append("交易金额：").append(msgVo.getGrossValue()).append("\n")
                    .append("交易币种：").append(msgVo.getGrossCurrency()).append("\n");
            if (Objects.isNull(msgVo.getTransactionTime())) {
                String dateStr = DateUtils.dateToStringGMT8(msgVo.getTransactionDate(), "yyyy-MM-dd");
                sb.append("交易日期：").append(dateStr).append("\n").append("\n");
            } else {
                String dateStr = DateUtils.dateToStringGMT8(msgVo.getTransactionTime(), DateUtils.TIME_STR);
                sb.append("交易时间：").append(dateStr).append("\n").append("\n");
            }
        }
        logger.info("BankService-getContent result:{}", sb.toString());
        return sb.toString();
    }

    private String getType(String type){
        switch (type) {
            case "1" : return "收款";
            case "2" : return "付款";
            default: return "其他";
        }
    }
}
