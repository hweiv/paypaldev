package com.jimi.controller;

import com.alibaba.fastjson.JSON;
import com.jimi.common.ApiResult;
import com.jimi.entity.*;
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
            logger.info("-BankController-sendBankMsg 执行结果为", JSON.toJSONString(apiResult));
        } catch (Exception e) {
            logger.error("BankController-sendBankMsg is error:{}", e);
            return ApiResult.error(e.getMessage());
        }
        return apiResult;
    }

    @PostMapping("/bankList")
    public ApiResult bankList() {
        ApiResult apiResult = null;
        try {
            apiResult = ApiResult.success(bankService.bankList());
            logger.info("-BankController-bankList 执行结果为", JSON.toJSONString(apiResult));
        } catch (Exception e) {
            logger.error("BankController-bankList is error:{}", e);
            return ApiResult.error(e.getMessage());
        }
        return apiResult;
    }

    // 查询银行账号
    @PostMapping("/bankAccountList")
    public ApiResult bankAccountList() {
        try {
            List<BankAccountDto> bankAccountList = bankService.bankAccountList();
            logger.info("-BankController-bankAccountList 执行结果为", JSON.toJSONString(bankAccountList));
            return ApiResult.success(bankAccountList);
        } catch (Exception e) {
            logger.error("BankController-bankAccountList is error:{}", e);
            return ApiResult.error(e.getMessage());
        }
    }

    // 查询银行数据列表
    @PostMapping("/queryBankData")
    public ApiResult queryBankData(@RequestBody BankReqVo bankReqVo) {
        try {
            BankPaymentDtoPageInfo bankPaymentDtoPageInfo = bankService.queryBankData(bankReqVo);
            logger.info("-BankController-queryBankData 执行结果为", JSON.toJSONString(bankPaymentDtoPageInfo));
            return ApiResult.success(bankPaymentDtoPageInfo);
        } catch (Exception e) {
            logger.error("BankController-queryBankData is error:{}", e);
            return ApiResult.error(e.getMessage());
        }
    }

    // 重新一键推送
    @PostMapping("/pushBankPaymentList")
    public ApiResult pushBankPaymentList(@RequestBody List<String> paymentIdList) {
        try {
            boolean result = bankService.pushBankPaymentList(paymentIdList);
            logger.info("-BankController-queryBankData 执行结果为", result);
            if (result) {
                return ApiResult.success("推送成功");
            } else {
                return ApiResult.success("推送失败");
            }
        } catch (Exception e) {
            logger.error("BankController-queryBankData is error:{}", e);
            return ApiResult.error(e.getMessage());
        }
    }
}
