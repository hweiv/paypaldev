package com.jimi.utils.dingding;

import com.jimi.entity.dingding.DingDingAuthorization;
import com.jimi.entity.dingding.DingDingMsgData;
import com.taobao.api.ApiException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DingDingSendMsgHandle {

    public boolean sendDingDingOneMessage(DingDingAuthorization dingAuthorization, DingDingMsgData dingDingMsgData) {
        return DingDingUtil.sendDingDingMessage(dingAuthorization, dingDingMsgData);
    }

    public void sendDingDingBatchMessage(DingDingAuthorization dingAuthorization, List<DingDingMsgData> dingDingMsgData) {
        dingDingMsgData.stream().forEach(s -> DingDingUtil.sendDingDingMessage(dingAuthorization, s));
    }
}