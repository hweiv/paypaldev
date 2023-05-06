package com.jimi.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jimi.common.ApiResult;
import com.jimi.common.dingding.DingDingPush;
import com.jimi.common.redis.RedisCacheUtil;
import com.jimi.entity.BankMsgVo;
import com.jimi.entity.BankPaymentInfo;
import com.jimi.exception.BusinessException;
import com.jimi.mapper.BankPaymentMapper;
import com.jimi.service.IBankService;
import com.jimi.utils.DateUtils;
import com.jimi.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BankService implements IBankService {
    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private BankPaymentMapper bankPaymentMapper;

    private static final String BANK_PAYMENT_NEW_DATA_FLAG_KEY = "BANK_PAYMENT_NEW_DATA_FLAG";

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
//        List<BankPaymentInfo> bankPaymentInfos = bankPaymentMapper.selectNewData("1", "1"); // 未推送，收款类型
//        pushNewDataDingTalk(bankPaymentInfos);
        return ApiResult.success("发送数据成功");
    }

    @Override
    public void pushNewDataDingTalk(List<BankPaymentInfo> bankPaymentInfos, String robotUrl) throws Exception{
        try {
            if (bankPaymentInfos.size() < 1) {
                return;
            }
            String content = getContent(bankPaymentInfos);
            ArrayList<String> mobileList = Lists.newArrayList();
            DingDingPush.sendMsgToGroupChat(robotUrl, false, mobileList, content);
            // 根据交易单号修改推送状态为已推送
            bankPaymentInfos.forEach(payment -> bankPaymentMapper.updatePushFlatByTransactionId(payment.getTransactionID()));
        } catch (Exception e) {
            logger.error("BankService pushNewDataDingTalk:{}", e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 获取消息文案
     *
     * @param bankPaymentInfos
     * @return
     */
    private String getContent(List<BankPaymentInfo> bankPaymentInfos) {
        StringBuffer sb = new StringBuffer();
        sb.append("银行账户动账通知：").append("\n");
        for (BankPaymentInfo msgVo : bankPaymentInfos) {
            String reciprocalAccountName = StringUtils.isBlank(msgVo.getReciprocalAccountName()) ? "" : StringUtil.maskStr(msgVo.getReciprocalAccountName());
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

    @Override
    public List<Map<String, Object>> bankList() {
        List<Map<String, Object>> bankList = bankPaymentMapper.selectBankList();
        return bankList;
    }
}
