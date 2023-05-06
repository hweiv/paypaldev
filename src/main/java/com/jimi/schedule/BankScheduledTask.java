package com.jimi.schedule;

import com.google.common.collect.Lists;
import com.jimi.common.dingding.DingDingPush;
import com.jimi.common.redis.RedisCacheUtil;
import com.jimi.entity.BankPaymentInfo;
import com.jimi.entity.DingRobotInfo;
import com.jimi.mapper.BankPaymentMapper;
import com.jimi.service.IBankService;
import com.jimi.utils.DateUtils;
import com.jimi.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class BankScheduledTask {
    private static final Logger logger = LoggerFactory.getLogger(BankScheduledTask.class);

    @Autowired
    private BankPaymentMapper bankPaymentMapper;

    @Autowired
    private IBankService bankService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    private static final String BANK_PAYMENT_NEW_DATA_FLAG_KEY = "BANK_PAYMENT_NEW_DATA_FLAG";

    @Value("${ccbbank.robot.webhook}")
    private String ccbBankRobotUrl;

    @Value("${spdbbank.robot.webhook}")
    private String spdbBankRobotUrl;

    /*
     * 推送到钉钉群
     * 60秒后执行，每隔1*60*60秒（1小时）执行, 单位：ms。
     * @Scheduled(initialDelay = 1000L, fixedRate = 1* 60 * 60 * 1000)
     */
    /**
     * 执行银行记录的数据，每点0分0秒 执行，每隔 10 分钟执行一次
     *
     * @Scheduled(cron = "30 59 0/1 * * ?")
     * 秒、分、时、日、月、周
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void pushBankPayment() {
        logger.info("BankScheduledTask-pushBankPayment start run");
        try {
            // 查询缓存是否有新数据入库
            String hasNewData = redisCacheUtil.getCacheObject(BANK_PAYMENT_NEW_DATA_FLAG_KEY);
            if (StringUtils.equalsIgnoreCase("Y", hasNewData)) {
                List<BankPaymentInfo> bankPaymentInfos = bankPaymentMapper.selectNewData("1", "1");
                List<BankPaymentInfo> ccbPaymentList = new ArrayList<>();
                List<BankPaymentInfo> spdbPaymentList = new ArrayList<>();
                for (BankPaymentInfo info : bankPaymentInfos) {
                    if (StringUtils.equalsIgnoreCase("ccb", info.getBankCode())) {
                        ccbPaymentList.add(info);
                    }
                    if (StringUtils.equalsIgnoreCase("spdb", info.getBankCode())) {
                        spdbPaymentList.add(info);
                    }
                }
                bankService.pushNewDataDingTalk(ccbPaymentList, ccbBankRobotUrl);
                bankService.pushNewDataDingTalk(spdbPaymentList, spdbBankRobotUrl);
                // 推送到钉钉群删除缓存
                redisCacheUtil.deleteObject(BANK_PAYMENT_NEW_DATA_FLAG_KEY);
            } else {
                logger.info("BankScheduledTask-pushBankPayment 没有新数据可推送");
                return;
            }
        } catch (Exception e) {
            logger.error("-ScheduledTask-executor is error:{}", e);
        }
    }

    /**
     * 测试定时任务执行可配置化的钉钉机器人群消息操作
     *
     * 注：不同的机器人所属的群聊或者定时器执行的频率不一样，无法做到完全配置，除非做一个平台把定时器做成可配置的
     */
    /*
    @Scheduled(initialDelay = 1000L, fixedRate = 5 * 60 * 1000)
    public void executeConfigRobot() {
        logger.info("BankScheduledTask-executeConfigRobot start run");
        try {
            // 查询数据库 ding_robot_info 表机器人数据
            List<DingRobotInfo> robotInfoList = dingRobotMapper.queryAllDingRobot();
            for (DingRobotInfo robotInfo : robotInfoList) {
                String robotUrl = robotInfo.getRobotUrl();
                String dingRobotId = robotInfo.getId();
                List<String> bankAccounts = dingRobotMapper.queryBankAccountsByDingRobotId(dingRobotId);
                // 根据银行账号查询新增的交易记录
                List<BankPaymentInfo> bankPaymentInfos = bankPaymentMapper.queryPaymentsByBankAccounts(bankAccounts, "1", "1");
                String content = getContent(bankPaymentInfos);
                ArrayList<String> mobileList = Lists.newArrayList();
                DingDingPush.sendMsgToGroupChat(robotUrl, false, mobileList, content);
            }
            String hasNewData = redisCacheUtil.getCacheObject(BANK_PAYMENT_NEW_DATA_FLAG_KEY);
            if (StringUtils.equalsIgnoreCase("Y", hasNewData)) {
                List<BankPaymentInfo> bankPaymentInfos = bankPaymentMapper.selectNewData("1", "1");
                bankService.pushNewDataDingTalk(bankPaymentInfos);
            } else {
                logger.info("BankScheduledTask-pushBankPayment 没有新数据可推送");
                return;
            }
        } catch (Exception e) {
            logger.error("-ScheduledTask-executor is error:{}", e);
        }
    }

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
     */

}
