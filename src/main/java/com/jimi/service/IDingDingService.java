package com.jimi.service;

import com.jimi.common.ApiResult;
import com.jimi.entity.dingding.DingDingMsgData;
import com.jimi.entity.dingding.DingMsgExternalInfo;
import com.jimi.entity.dingding.SendDingTalkVo;

import java.util.List;

public interface IDingDingService {

    /**
     * 用钉钉群机器人发送消息
     * @param sendDingTalkVo
     * @return
     */
    ApiResult sendMsgInGroupChat(SendDingTalkVo sendDingTalkVo);

    /**
     * 发送钉钉消息通知列表
     * @param list
     * @return
     */
    ApiResult sendNoticeMsg(List<DingDingMsgData> list);


    ApiResult sendNoticeMsgExternal(DingMsgExternalInfo info);
}
