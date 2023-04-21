package com.jimi.utils.dingding;

import com.alibaba.fastjson.JSON;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.request.OapiUserGetByMobileRequest;
import com.dingtalk.api.request.OapiUserGetRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.dingtalk.api.response.OapiUserGetByMobileResponse;
import com.dingtalk.api.response.OapiUserGetResponse;
import com.jimi.entity.dingding.DingDingAuthorization;
import com.jimi.entity.dingding.DingDingMsgData;
import com.taobao.api.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DingDingUtil {
    private static final Logger logger = LoggerFactory.getLogger(DingDingUtil.class);
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");

    public static String getAccessToken(String appKey, String appSecret) throws ApiException {
        DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
        OapiGettokenRequest request = new OapiGettokenRequest();
        request.setAppkey(appKey);
        request.setAppsecret(appSecret);
        // /*请求方式*/
        request.setHttpMethod("GET");
        OapiGettokenResponse response = client.execute(request);
        return response.getAccessToken();
    }

    public static boolean sendDingDingMessage(DingDingAuthorization dingAuthorization, DingDingMsgData dingDingMsgData) {
        try {
            String accessToken = getAccessToken(dingAuthorization.getAppKey(), dingAuthorization.getAppSecret());
            if (StringUtils.isBlank(dingDingMsgData.getMobile())) {
                return false;
            }
            DingTalkClient client2 = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get_by_mobile");
            OapiUserGetByMobileRequest req = new OapiUserGetByMobileRequest();
            req.setMobile(dingDingMsgData.getMobile());
            req.setHttpMethod("GET");
            OapiUserGetByMobileResponse rsp = client2.execute(req, accessToken);
            //判断不是ok 代表找不到该用户
            if (!rsp.getErrmsg().equals("ok")) {
                return false;
            } else {
                // 获取到Urid就是在公司里要发送到那个人的id
                String urid = rsp.getUserid();
                // 根据用户id获取用户详情
                DingTalkClient userDetail = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get");
                OapiUserGetRequest userReq = new OapiUserGetRequest();
                userReq.setUserid(urid);
                userReq.setHttpMethod("GET");
                OapiUserGetResponse userRsp = userDetail.execute(userReq, accessToken);
                String userName = userRsp.getName();
                DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
                OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
                request.setUseridList(urid);
                request.setAgentId(Long.parseLong(dingAuthorization.getAgentId()));
                request.setToAllUser(false);
                OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
                msg.setOa(new OapiMessageCorpconversationAsyncsendV2Request.OA());
                // 跳转链接
                //例如https://www.baidu.com
                //设置手机端可以打开链接
                msg.getOa().setMessageUrl(dingDingMsgData.getUrl());
                //设置PC端可以打开链接
                msg.getOa().setPcMessageUrl(dingDingMsgData.getUrl());
                // 设置head
                msg.getOa().setHead(new OapiMessageCorpconversationAsyncsendV2Request.Head());
                msg.getOa().getHead().setText("待办事宜");
                msg.getOa().getHead().setBgcolor("00409eff");
                // 设置body
                msg.getOa().setBody(new OapiMessageCorpconversationAsyncsendV2Request.Body());
                msg.getOa().getBody().setTitle(dingDingMsgData.getTitle());
                msg.getOa().getBody().setContent(dingDingMsgData.getContent() + "\n时间：" + sdf.format(new Date()));
                // 消息类型
                msg.setMsgtype("oa");
                request.setMsg(msg);
                OapiMessageCorpconversationAsyncsendV2Response response = client.execute(request, accessToken);
                logger.info("发送消息是否成功" + response.isSuccess());
                System.out.println(response.isSuccess());
                logger.info("消息任务ID" + response.getTaskId());
                System.out.println(response.getTaskId());
                return response.isSuccess();
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 根据电话获取员工id信息
    private static OapiUserGetByMobileResponse getUserByMobile(String mobile, String accessToken) throws ApiException {
        DingTalkClient client2 = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get_by_mobile");
        OapiUserGetByMobileRequest req = new OapiUserGetByMobileRequest();
        req.setMobile(mobile);
        req.setHttpMethod("GET");
        return client2.execute(req, accessToken);
    }

    // 根据用户id获取用户详情
    private static OapiUserGetResponse getUserInfoByUserId(String accessToken, String urid) throws ApiException {
        DingTalkClient userDetail = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get");
        OapiUserGetRequest userReq = new OapiUserGetRequest();
        userReq.setUserid(urid);
        userReq.setHttpMethod("GET");
        return userDetail.execute(userReq, accessToken);
    }
}
