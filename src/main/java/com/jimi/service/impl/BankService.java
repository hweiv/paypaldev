package com.jimi.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jimi.common.ApiResult;
import com.jimi.common.dingding.DingDingPush;
import com.jimi.entity.BankMsgVo;
import com.jimi.entity.BankPaymentInfo;
import com.jimi.exception.BusinessException;
import com.jimi.mapper.BankPaymentMapper;
import com.jimi.service.IBankService;
import com.jimi.service.IDingDingService;
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
import java.util.Objects;

@Service
public class BankService implements IBankService {
    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    @Autowired
    private IDingDingService dingDingService;

    @Autowired
    private BankPaymentMapper bankPaymentMapper;

    @Value("${ccbbank.robot.webhook}")
    private String ccbBankRobotUrl;

    @Value("${spdbbank.robot.webhook}")
    private String spdbBankRobotUrl;

    @Override
    public ApiResult sendBankMsg(List<BankMsgVo> bankMsgVoList) throws Exception{
        logger.info("-BankService-sendBankMsg.bankMsgVoList:{}", bankMsgVoList);
        if (Objects.isNull(bankMsgVoList) || bankMsgVoList.size() < 1) {
            logger.info("-BankService-sendBankMsg 空数据");
            return new ApiResult().error("数据有误，列表为空");
        }
        String robotUrl = getRobotUrl(bankMsgVoList.get(0).getBankCode());
        // 入库操作
        List<BankPaymentInfo> bankInfoList = new ArrayList<>();
        List<BankMsgVo> pushMsgList = new ArrayList<>();
        try {
            for (BankMsgVo bankMsgVo : bankMsgVoList) {
                BankPaymentInfo info = bankMsgVo.toInfo();
                info.setTransactionTime(DateUtils.timeStrToDateGMT8(bankMsgVo.getTransactionTime(), DateUtils.TIME_STR));
                info.setBindAccount(bankMsgVo.getAccount());
                info.setBindAccountName(bankMsgVo.getAccountName());
                if (StringUtils.equalsIgnoreCase("1", bankMsgVo.getType())) { // 交易类型：1-收款；2-付款；其他
                    pushMsgList.add(bankMsgVo);
                    info.setPushFlat("0"); // 只推送收款
                } else {
                    info.setPushFlat("1"); // 不推送
                }
                bankInfoList.add(info);
                // 根据交易单号查询表中数据是否存在，不存在直接插入，存在则把原来的单号数据失效，再插入
                List<BankPaymentInfo> paymentInfos = bankPaymentMapper.selectByTransactionId(bankMsgVo.getTransactionID());
                if (paymentInfos.size() > 0) {
                    logger.info("PaypalService.pushPayment 单号为:{}, 已存在数据:{}",bankMsgVo.getTransactionID(), JSON.toJSONString(paymentInfos));
                    bankPaymentMapper.updateDelFlatByTransactionId(bankMsgVo.getTransactionID());
                }
            }
            if (bankInfoList.size() > 0) {
                int count = bankPaymentMapper.insertBatchPayment(bankInfoList);
                logger.info("-BankService-sendBankMsg 成功插入数据:{}", count);
            }
        } catch (ParseException e) {
            logger.error("BankService-sendBankMsg 转换异常:{}", e);
            throw new BusinessException("数据格式转换异常");
        }
        if (pushMsgList.size() < 1) {
            return ApiResult.success("无收款数据");
        }
        String content = getContent(pushMsgList);
        ArrayList<String> mobileList = Lists.newArrayList();
        DingDingPush.sendMsgToGroupChat(robotUrl, false, mobileList, content);
        return ApiResult.success("发送数据成功");
    }

    /**
     * 获取消息文案
     *
     * @param bankMsgVoList
     * @return
     */
    private String getContent(List<BankMsgVo> bankMsgVoList) {
        StringBuffer sb = new StringBuffer();
        sb.append("银行账户动账通知：").append("\n");
        for (BankMsgVo msgVo : bankMsgVoList) {
            String reciprocalAccountName = StringUtils.isBlank(msgVo.getReciprocalAccountName()) ? "" : StringUtil.maskStr(msgVo.getReciprocalAccountName());
            sb.append("银行名称：").append(msgVo.getBankName()).append("\n")
                    .append("交易单号：").append(msgVo.getTransactionID()).append("\n")
                    .append("账户名称：").append(msgVo.getAccountName()).append("\n")
                    .append("关联账户：").append(msgVo.getAccount()).append("\n")
                    .append("交易类型：").append(getType(msgVo.getType())).append("\n")
                    .append("对方户名：").append(reciprocalAccountName).append("\n")
                    .append("交易金额：").append(msgVo.getGrossValue()).append("\n")
                    .append("交易币种：").append(msgVo.getGrossCurrency()).append("\n")
                    .append("交易时间：").append(msgVo.getTransactionTime()).append("\n").append("\n");
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

    // 根据银行编码获取对应机器人
    private String getRobotUrl(String bankCode){
        switch (bankCode) {
            case "ccb" : return ccbBankRobotUrl;
            case "spdb" : return spdbBankRobotUrl;
            default: return ccbBankRobotUrl;
        }
    }
}
