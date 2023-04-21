package com.jimi.controller;

import com.alibaba.fastjson.JSON;
import com.jimi.common.ApiResult;
import com.jimi.entity.dingding.DingDingMsgData;
import com.jimi.entity.dingding.DingMsgExternalInfo;
import com.jimi.entity.dingding.SendDingTalkVo;
import com.jimi.service.IDingDingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dingding")
public class DingDingController {
    private static final Logger logger = LoggerFactory.getLogger(DingDingController.class);
    @Autowired
    private IDingDingService dingDingService;

    /**
     * 群聊机器人发送消息
     *
     * @param sendDingTalkVo
     * @return
     */
    @PostMapping("/sendMsgInGroupChat")
    public ApiResult sendMsgInGroupChat(@RequestBody @Validated SendDingTalkVo sendDingTalkVo) {
        ApiResult apiResult = null;
        try {
            apiResult = dingDingService.sendMsgInGroupChat(sendDingTalkVo);
            logger.info("-DingRobotController-sendMsgInGroupChat 执行结果为", JSON.toJSONString(apiResult));
        } catch (Exception e) {
            logger.error("DingRobotController-sendMsgInGroupChat is error:{}", e);
            return ApiResult.error(e.getMessage());
        }
        return apiResult;
    }

    /**
     * 钉钉发送消息通知
     *
     * @param list
     * @return
     */
    @PostMapping("/sendNoticeMsg")
    public ApiResult sendNoticeMsg(@RequestBody List<DingDingMsgData> list) {
        ApiResult apiResult = null;
        try {
            apiResult = dingDingService.sendNoticeMsg(list);
            logger.info("-DingRobotController-sendMsgInGroupChat 执行结果为", JSON.toJSONString(apiResult));
        } catch (Exception e) {
            logger.error("DingRobotController-sendMsgInGroupChat is error:{}", e);
            return ApiResult.error(e.getMessage());
        }
        return apiResult;
    }


    /**
     * 钉钉发送消息通知给外部
     *
     * @param info
     * @return
     */
    @PostMapping("/sendNoticeMsgExternal")
    public ApiResult sendNoticeMsgExternal(@RequestBody @Validated DingMsgExternalInfo info) {
        ApiResult apiResult = null;
        try {
            apiResult = dingDingService.sendNoticeMsgExternal(info);
            logger.info("-DingRobotController-sendMsgInGroupChat 执行结果为", JSON.toJSONString(apiResult));
        } catch (Exception e) {
            logger.error("DingRobotController-sendMsgInGroupChat is error:{}", e);
            return ApiResult.error(e.getMessage());
        }
        return apiResult;
    }

}
