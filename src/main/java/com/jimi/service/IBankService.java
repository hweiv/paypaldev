package com.jimi.service;

import com.jimi.common.ApiResult;
import com.jimi.entity.BankMsgVo;

import java.util.List;

public interface IBankService {
    ApiResult sendBankMsg(List<BankMsgVo> bankMsgVoList) throws Exception;

}
