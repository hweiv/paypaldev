package com.jimi.utils.dingding;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
 
/**
 *@author: Chris.Mont
 *@date: 2021-06-07 10:53
 *@desc: 钉钉发送工作通知
 */
@Slf4j
@Service
public class DingWorkBiz {
 
    private String APP_KEY = "dingecf5x7vxxxxxxxxxx";
    private String APP_SECRET = "04EaZggxxxxxx0-T3E66Y27b9FRxxxxxxxxxxxxx6kydsDw";
    private Long AGENT_ID = 1210000099997L;
 
    private String MESSAGE_URL = "https://www.baidu.com";
    private String PC_MESSAGE_URL = "https://www.baidu.com";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
 
    /**
     * 获取AccessToken
     * @return  AccessToken
     * @throws ApiException
     */
    private String getAccessToken() throws ApiException {
        DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
        OapiGettokenRequest request = new OapiGettokenRequest();
        //Appkey
        request.setAppkey(APP_KEY);
        //Appsecret
        request.setAppsecret(APP_SECRET);
        /*请求方式*/
        request.setHttpMethod("GET");
        OapiGettokenResponse response = client.execute(request);
        return response.getAccessToken();
    }
 
    /**
     * 发送OA消息
     * @param mobile 发送消息人的电话，多个英文逗号拼接
     * @throws ApiException
     */
    public void sendOA(String mobile) throws ApiException {
        log.info("发送钉钉通知");
        String accessToken = getAccessToken();
        if(StringUtils.isBlank(mobile)){
            return;
        }
        //电话号码数组
        String[] split = mobile.split(",");
        for (String s : split) {
            DingTalkClient client2 = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get_by_mobile");
            OapiUserGetByMobileRequest req = new OapiUserGetByMobileRequest();
            req.setMobile(s);
            req.setHttpMethod("GET");
            OapiUserGetByMobileResponse rsp = client2.execute(req, accessToken);
            //获取到Urid就是在公司里要发送到那个人的id
            String urid = rsp.getUserid();
            //根据用户id获取用户详情
            DingTalkClient userDetail = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get");
            OapiUserGetRequest userReq = new OapiUserGetRequest();
            userReq.setUserid(urid);
            userReq.setHttpMethod("GET");
            OapiUserGetResponse userRsp = userDetail.execute(userReq, accessToken);
            String userName = userRsp.getName();
 
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
            OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
            request.setUseridList(urid);
            request.setAgentId(AGENT_ID);
            request.setToAllUser(false);
 
            OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
            msg.setOa(new OapiMessageCorpconversationAsyncsendV2Request.OA());
            //跳转链接
            msg.getOa().setMessageUrl(MESSAGE_URL);
            msg.getOa().setPcMessageUrl(PC_MESSAGE_URL);
            //设置head
            msg.getOa().setHead(new OapiMessageCorpconversationAsyncsendV2Request.Head());
            msg.getOa().getHead().setText("待办事宜");
            msg.getOa().getHead().setBgcolor("00409eff");
            //设置body
            msg.getOa().setBody(new OapiMessageCorpconversationAsyncsendV2Request.Body());
            msg.getOa().getBody().setTitle("邮件现有功能已完成开发，抄送、密送以及发送异常处理暂未开始~！");
            msg.getOa().getBody().setContent("创建人：" + userName + "\n创建时间：" + sdf.format(new Date()));
            //消息类型
            msg.setMsgtype("oa");
            request.setMsg(msg);
            log.info("获取发送通知消息体和获取发送通知人完成");
            OapiMessageCorpconversationAsyncsendV2Response response = client.execute(request,accessToken);
            log.info("发送消息是否成功"+response.isSuccess());
            System.out.println(response.isSuccess());
            log.info("消息任务ID"+response.getTaskId());
            System.out.println(response.getTaskId());
        }
    }
 
 
}