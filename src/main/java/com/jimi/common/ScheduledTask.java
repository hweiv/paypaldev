package com.jimi.common;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jimi.common.constant.PaypalTransConstant;
import com.jimi.common.dingding.DingDingPush;
import com.jimi.entity.PaymentParamVo;
import com.jimi.entity.PaypalPaymentDto;
import com.jimi.service.IPaypalService;
import com.jimi.service.impl.PaypalService;
import com.jimi.utils.DateUtils;
import com.jimi.utils.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @version V2.3
 * @ClassName:ScheduledTask.java
 * @author: wgcloud
 * @date: 2019年11月16日
 * @Description: ScheduledTask.java
 * @Copyright: 2017-2022 www.wgstart.com. All rights reserved.
 */
@Component
public class ScheduledTask {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTask.class);
    @Autowired
    private IPaypalService paypalService;

    @Value("${paypal.tob.dingding.webhook}")
    private String tobPayPalRobotUrl;

    @Value("${paypal.toc.dingding.webhook}")
    private String tocPayPalRobotUrl;

    @Value("#{'${paypal.busi.tob.app.usernameList:}'.split(',')}")
    private List<String> userListTob;

    @Value("#{'${paypal.busi.tob.app.passwordList:}'.split(',')}")
    private List<String> pswListTob;

    @Value("#{'${paypal.busi.tob.app.signatureList:}'.split(',')}")
    private List<String> signatureListTob;

    @Value("#{'${paypal.busi.toc.app.usernameList:}'.split(',')}")
    private List<String> userListToc;

    @Value("#{'${paypal.busi.toc.app.passwordList:}'.split(',')}")
    private List<String> pswListToc;

    @Value("#{'${paypal.busi.toc.app.signatureList:}'.split(',')}")
    private List<String> signatureListToc;

    /**
     * 线程池
     */
    static ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 2, TimeUnit.MINUTES, new LinkedBlockingDeque<>());

    /*
     * 推送到钉钉群
     * 60秒后执行，每隔1*60*60秒（1小时）执行, 单位：ms。
     * @Scheduled(initialDelay = 1000L, fixedRate = 1* 60 * 60 * 1000)
     */
    /**
     * ToB端账户（公司PayPal、康凯斯）
     * 定时任务扫描PayPal账户，推送到钉钉群
     * 表示每天的 0:59:10 执行，每隔1h执行一次
     * @Scheduled(cron = "30 59 0/1 * * ?")
     * 秒、分、时、日、月、周
     */
    @Scheduled(cron = "10 59 0/1 * * ?")
    public void executor() {
        logger.info("ScheduledTask-executor start run");
        logger.info("ScheduledTask-executor start run 总共读取到账户数:{}, 对应账户为:{}", userListTob.size(), JSON.toJSONString(userListTob));
        try {
            // 开始时间 本地时间今日零点
            String startTime = DateUtils.getTodayFormat();
            // 现在的本地时间
            String endTime = DateUtils.getNowFormat();
            if (userListTob.size() < 1) {
                return;
            }
            for (int i = 0; i < userListTob.size(); i++) {
                if (StringUtils.isNotBlank(userListTob.get(i))) {
                    PaymentParamVo paramVo = new PaymentParamVo();
                    paramVo.setStartTime(startTime);
                    paramVo.setEndTime(endTime);
                    paramVo.setUsername(userListTob.get(i));
                    paramVo.setPassword(pswListTob.get(i));
                    paramVo.setSignature(signatureListTob.get(i));
                    paramVo.setWebHookUrl(tobPayPalRobotUrl);
                    logger.info("ScheduledTask executor data-paramVo:{}", JSON.toJSONString(paramVo));
                    List<PaypalPaymentDto> resultList = paypalService.pushPayment(paramVo);
                    logger.info("ScheduledTask executor data-resultList:{}", resultList);
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            logger.error("-ScheduledTask-executor is error:{}", e);
        }
    }

    /**
     *  电商（ToC）端账户
     * 定时任务扫描PayPal账户，推送到钉钉群
     * 表示每天的 0:29:40 执行，每隔30min执行一次
     * @Scheduled(cron = "30 59 0/1 * * ?")
     * 秒、分、时、日、月、周
     */
    @Scheduled(cron = "40 29/30 * * * ?")
    public void onlineToCPush() {
        logger.info("ScheduledTask-onlineToCPush start run");
        logger.info("ScheduledTask-onlineToCPush start run 总共读取到账户数:{}, 对应账户为:{}", userListToc.size(), JSON.toJSONString(userListToc));
        try {
            // 开始时间 本地时间今日零点
            String startTime = DateUtils.getTodayFormat();
            // 现在的本地时间
            String endTime = DateUtils.getNowFormat();
            if (userListToc.size() < 1) {
                return;
            }
            for (int i = 0; i < userListToc.size(); i++) {
                if (StringUtils.isNotBlank(userListToc.get(i))) {
                    PaymentParamVo paramVo = new PaymentParamVo();
                    paramVo.setStartTime(startTime);
                    paramVo.setEndTime(endTime);
                    paramVo.setUsername(userListToc.get(i));
                    paramVo.setPassword(pswListToc.get(i));
                    paramVo.setSignature(signatureListToc.get(i));
                    paramVo.setWebHookUrl(tocPayPalRobotUrl);
                    logger.info("ScheduledTask onlineToCPush data-paramVo:{}", JSON.toJSONString(paramVo));
                    List<PaypalPaymentDto> resultList = paypalService.pushPayment(paramVo);
                    logger.info("ScheduledTask onlineToCPush data-resultList:{}", resultList);
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            logger.error("-ScheduledTask-executor is error:{}", e);
        }
    }
}
