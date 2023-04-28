package com.jimi.schedule;

import com.jimi.common.redis.RedisCacheUtil;
import com.jimi.entity.BankPaymentInfo;
import com.jimi.mapper.BankPaymentMapper;
import com.jimi.mapper.DingRobotMapper;
import com.jimi.service.IBankService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BankScheduledTask {
    private static final Logger logger = LoggerFactory.getLogger(BankScheduledTask.class);

    @Autowired
    private BankPaymentMapper bankPaymentMapper;

    @Autowired
    private DingRobotMapper dingRobotMapper;

    @Autowired
    private IBankService bankService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    private static final String BANK_PAYMENT_NEW_DATA_FLAG_KEY = "BANK_PAYMENT_NEW_DATA_FLAG";

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
    @Scheduled(cron = "0/30 * * * * ?")
//    @Scheduled(initialDelay = 1000L, fixedRate = 5 * 60 * 1000)
    public void pushBankPayment() {
        logger.info("BankScheduledTask-pushBankPayment start run");
        try {
            // 查询缓存是否有新数据入库
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
}
