package com.jimi.controller;

import com.alibaba.fastjson.JSON;
import com.jimi.common.ApiResult;
import com.jimi.entity.BankMsgVo;
import com.jimi.entity.dingding.DingDingMsgData;
import com.jimi.entity.dingding.DingMsgExternalInfo;
import com.jimi.entity.dingding.SendDingTalkVo;
import com.jimi.service.IBankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bank")
public class BankController {
    private static final Logger logger = LoggerFactory.getLogger(BankController.class);
    @Autowired
    private IBankService bankService;

    // erp系统调用，接收银行交易数据
    @PostMapping("/sendBankMsg")
    public ApiResult gainBankData(@RequestBody List<BankMsgVo> bankMsgVoList) {
        ApiResult apiResult = null;
        try {
            apiResult = bankService.gainBankData(bankMsgVoList);
            logger.info("-DingRobotController-sendBankMsg 执行结果为", JSON.toJSONString(apiResult));
        } catch (Exception e) {
            logger.error("DingRobotController-sendBankMsg is error:{}", e);
            return ApiResult.error(e.getMessage());
        }
        return apiResult;
    }

    @PostMapping("/bankList")
    public ApiResult bankList() {
        ApiResult apiResult = null;
        try {
            apiResult = ApiResult.success(bankService.bankList());
            logger.info("-DingRobotController-bankList 执行结果为", JSON.toJSONString(apiResult));
        } catch (Exception e) {
            logger.error("DingRobotController-bankList is error:{}", e);
            return ApiResult.error(e.getMessage());
        }
        return apiResult;
    }
}
