package com.jimi.controller;import com.alibaba.fastjson.JSON;import com.jimi.common.ApiResult;import com.jimi.entity.*;import com.jimi.service.IPaypalService;import com.jimi.utils.DateUtils;import com.jimi.utils.jwt.TokenUtils;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.beans.factory.annotation.Value;import org.springframework.web.bind.annotation.*;import java.util.ArrayList;import java.util.List;@RestController@RequestMapping("/paypal/payment")public class PaymentController {    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);    @Value("#{'${paypal.busi.app.usernameList:}'.split(',')}")    private List<String> userList;    @Value("#{'${paypal.busi.app.passwordList:}'.split(',')}")    private List<String> pswList;    @Value("#{'${paypal.busi.app.signatureList:}'.split(',')}")    private List<String> signatureList;    @Autowired    private IPaypalService paypalService;    // 查询数据记录--从表中查    @PostMapping("/queryAllPayment")    public ApiResult queryAllPayment(@RequestBody PaymentVo paymentVo) {        ApiResult apiResult = null;        try {            PaymentDtoPageInfo result = paypalService.queryAllPayment(paymentVo);            logger.info("-PaymentController-queryAllPayment查询数据为", JSON.toJSONString(result));            if (result.getList().size() > 0) {                apiResult = ApiResult.success(result);            } else {                apiResult = ApiResult.success("所筛选数据为空", result);            }        } catch (Exception e) {            logger.error("PaymentController-getPayment is error:{}", e);            return ApiResult.error(e.getMessage());        }        return apiResult;    }    @PostMapping("/pushPaymentList")    public ApiResult pushPaymentList(@RequestBody List<String> paymentIdList) {        ApiResult apiResult = null;        try {            boolean result = paypalService.pushPaymentList(paymentIdList);            logger.info("-PaymentController-queryAllPayment查询数据为", JSON.toJSONString(result));            if (result) {                apiResult = ApiResult.success("推送成功");            } else {                apiResult = ApiResult.success("推送失败");            }        } catch (Exception e) {            logger.error("PaymentController-getPayment is error:{}", e);            return ApiResult.error(e.getMessage());        }        return apiResult;    }    // 查询Paypal的数据--直查PayPal的api接口    @PostMapping("/queryFromPaypal")    public ApiResult queryFromPaypal(@RequestBody PaymentParamVo paramVo) {        ApiResult apiResult = null;        logger.info("-PaymentController-queryFromPaypal.paramVo:{}", JSON.toJSONString(paramVo));        try {            List<PaypalPaymentDto> result = new ArrayList<>();            if (StringUtils.isAnyBlank(paramVo.getUsername(), paramVo.getPassword(), paramVo.getSignature())) {                logger.info("PaymentController-queryFromPaypal-PayPal账号认证信息确实");            } else {                result.addAll(paypalService.queryFromPaypal(paramVo));            }            logger.info("PaymentController-queryFromPaypal-result:{}", JSON.toJSONString(result));            if (result.size() > 0) {                apiResult = ApiResult.success(result);            } else {                apiResult = ApiResult.success("没有查询到信息", result);            }        } catch (Exception e) {            logger.error("PaymentController-getPayment is error:{}", e);            return ApiResult.error(e.getMessage());        }        return apiResult;    }    // 查询关联的PayPal账号列表    @PostMapping("/accountList")    public ApiResult accountList() {        try {            List<PaypalAccount> accountList = paypalService.queryAccountList();            return ApiResult.success(accountList);        } catch (Exception e) {            logger.error("PaymentController-getPayment is error:{}", e);            return ApiResult.error(e.getMessage());        }    }}