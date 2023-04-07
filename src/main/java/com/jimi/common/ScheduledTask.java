package com.jimi.common;

import cn.hutool.json.JSONObject;
import com.google.common.collect.Lists;
import com.jimi.common.constant.PaypalTransConstant;
import com.jimi.common.dingding.DingDingPush;
import com.jimi.entity.PaypalPaymentDto;
import com.jimi.service.IPaypalService;
import com.jimi.utils.DateUtils;
import com.jimi.utils.RestUtil;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ScheduledTask {

    @Autowired
    private IPaypalService paypalService;

    @Value("${dingding.webhook}")
    private String webHook;

    /**
     * 线程池
     */
    static ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 2, TimeUnit.MINUTES, new LinkedBlockingDeque<>());

    /**
     * 推送到钉钉群
     * 60秒后执行，每隔10*60秒执行, 单位：ms。
     */
    @Scheduled(initialDelay = 1000L, fixedRate = 10 * 60 * 1000)
    public void executor() {
        log.info("ScheduledTask-executor start run");
        try {
            // 开始时间 本地时间今日零点
            String startTime = DateUtils.getTodayFormat();
            // 现在的本地时间
            String endTime = DateUtils.getNowFormat();
            // 定时任务，扫描执行接口更新数据
            List<PaypalPaymentDto> resultList = paypalService.pushPayment(startTime, endTime);
            log.info("ScheduledTask executor data:{}", resultList);
        } catch (Exception e) {
            log.error("-ScheduledTask-executor is error:{}", e);
        }
    }

    /**
     * 定时器请求交易单数据，存入表中
     * 60秒后执行，每隔 1小时 执行, 单位：ms。
     */
    /*
    @Scheduled(initialDelay = 1000L, fixedRate = 1 * 60 * 60 * 1000)
    public void runPayment() {
        log.info("ScheduledTask-runPayment start run");
        try {
            Calendar cal = Calendar.getInstance();
            Date currentTime = cal.getTime();
            cal.setTime(currentTime);
            cal.add(Calendar.HOUR_OF_DAY, -2);
            Date twoHoursAgo = cal.getTime();
            // 两个小时以前--开始时间
            String startTime = DateUtils.dateToStringGMT(twoHoursAgo, DateUtils.TIME_STR_T_Z);
            // 现在--结束时间
            String endTime = DateUtils.dateToStringGMT(currentTime, DateUtils.TIME_STR_T_Z);
            // 定时任务，扫描执行接口更新数据
            List<PaypalPaymentDto> resultList = paypalService.queryAllPayment(startTime, endTime);
//            if (resultList.size() > 0) {
//                String content = getContent(resultList);
//                ArrayList<String> mobileList = Lists.newArrayList();
//                DingDingPush.sendMsgToGroupChat(webHook, false, mobileList, content);
//            }
            log.info("ScheduledTask executor data:{}", resultList);
        } catch (Exception e) {
            log.error("-ScheduledTask-executor is error:{}", e);
        }
    }

     */
}
