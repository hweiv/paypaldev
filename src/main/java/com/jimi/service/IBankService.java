package com.jimi.service;

import com.jimi.common.ApiResult;
import com.jimi.entity.BankMsgVo;
import com.jimi.entity.BankPaymentInfo;

import java.util.List;
import java.util.Map;

public interface IBankService {
    ApiResult gainBankData(List<BankMsgVo> bankMsgVoList) throws Exception;

    void pushNewDataDingTalk(List<BankPaymentInfo> bankPaymentInfos) throws Exception;

    List<Map<String, Object>> bankList();
}
