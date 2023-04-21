package com.jimi.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jimi.common.ApiResult;
import com.jimi.common.dingding.DingDingPush;
import com.jimi.entity.dingding.DingDingAuthorization;
import com.jimi.entity.dingding.DingDingMsgData;
import com.jimi.entity.dingding.DingMsgExternalInfo;
import com.jimi.entity.dingding.SendDingTalkVo;
import com.jimi.service.IDingDingService;
import com.jimi.utils.dingding.DingDingSendMsgHandle;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DingDingService implements IDingDingService {
    private static final Logger logger = LoggerFactory.getLogger(DingDingService.class);

    @Resource
    private DingDingSendMsgHandle dingSendMsgHandle;

    @Value("${hw.ding.talk.agentId}")
    private String hwDingAgentId;

    @Value("${hw.ding.talk.appKey}")
    private String hwDingAppKey;

    @Value("${hw.ding.talk.appSecret}")
    private String hwDingAppSecret;

    @Override
    public ApiResult sendMsgInGroupChat(SendDingTalkVo sendDingTalkVo) {
        logger.info("-DingRobotService-sendMsgInGroupChat.sendDingTalkVo:{}", JSON.toJSONString(sendDingTalkVo));
        boolean isAll = Objects.isNull(sendDingTalkVo.getIsAll()) ? false : sendDingTalkVo.getIsAll();
        List<String> mobileList = sendDingTalkVo.getMobileList();
        if (Objects.isNull(mobileList) || mobileList.size() < 1) {
            mobileList = Lists.newArrayList();
        }
        String content = sendDingTalkVo.getContent();
        String webHookUrl = sendDingTalkVo.getWebHookUrl();
        DingDingPush.sendMsgToGroupChat(webHookUrl, isAll, mobileList, content);
        return ApiResult.success("发送数据成功");
    }


    @Override
    public ApiResult sendNoticeMsg(List<DingDingMsgData> list) {
        DingDingAuthorization authorization = new DingDingAuthorization();
        authorization.setAgentId(hwDingAgentId);
        authorization.setAppKey(hwDingAppKey);
        authorization.setAppSecret(hwDingAppSecret);
        dingSendMsgHandle.sendDingDingBatchMessage(authorization, list);
        return ApiResult.success("信息发送成功");
    }

    @Override
    public ApiResult sendNoticeMsgExternal(DingMsgExternalInfo info) {
        logger.info("DingRobotService-sendNoticeMsgExternal.info:{}", JSON.toJSONString(info));
        List<DingDingMsgData> infoList = info.getList();
        if (Objects.isNull(infoList) || infoList.size() < 1) {
            return ApiResult.error("请检查请求参数");
        }
        DingDingAuthorization authorization = new DingDingAuthorization();
        if (StringUtils.equalsIgnoreCase(info.getSystemCode(), "hw")) {
            authorization.setAppSecret(hwDingAppSecret);
            authorization.setAppKey(hwDingAppKey);
            authorization.setAgentId(hwDingAgentId);
        }
        List<DingDingMsgData> list = new ArrayList<>();
        infoList.forEach(data -> {
            DingDingMsgData msgData = new DingDingMsgData();
            BeanUtils.copyProperties(data, msgData);
            list.add(msgData);
        });
        logger.info("DingRobotService-sendNoticeMsgExternal.authorization:{}, list:{}", JSON.toJSONString(authorization), JSON.toJSONString(list));
        if (list.size() < 1) {
            return ApiResult.error("请求转换有误");
        }
        dingSendMsgHandle.sendDingDingBatchMessage(authorization, list);
        return ApiResult.success("执行成功");
    }

}
