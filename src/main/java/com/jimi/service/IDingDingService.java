package com.jimi.service;

import com.jimi.common.ApiResult;
import com.jimi.entity.dingding.DingDingMsgData;
import com.jimi.entity.dingding.DingMsgExternalInfo;
import com.jimi.entity.dingding.SendDingTalkVo;

import java.util.List;

public interface IDingDingService {

    ApiResult sendMsgInGroupChat(SendDingTalkVo sendDingTalkVo);


    ApiResult sendNoticeMsg(List<DingDingMsgData> list);

    ApiResult sendNoticeMsgExternal(DingMsgExternalInfo info);



}
