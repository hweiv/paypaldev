package com.jimi.service;

import com.jimi.common.ApiResult;
import com.jimi.entity.*;

import java.util.List;
import java.util.Map;

public interface IBankService {
    ApiResult gainBankData(List<BankMsgVo> bankMsgVoList) throws Exception;

    List<Map<String, Object>> bankList();

    List<BankAccountDto> bankAccountList();

    BankPaymentDtoPageInfo queryBankData(BankReqVo bankReqVo);

    boolean pushBankPaymentList(List<String> paymentIdList);

    boolean pushBankDataDingGroup(List<BankPaymentInfo> bankPaymentInfos);
}
