package com.jimi.common.dingding;
 
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jimi.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Maps;

@Slf4j
public class DingDingPush {
    public static void main(String[] args) throws BusinessException {
        //把webhook设置成对应群的即可
        String webhook ="https://oapi.dingtalk.com/robot/send?access_token=ebd303976fa69afb9a1d2e593f5511eb8238f793bdff6ec1689ffe071341f9a7";
        String content = getContent();
        ArrayList<String> mobileList = Lists.newArrayList();
        sendMsgToGroupChat(webhook,false,mobileList,content);
    }

    /**
     * 通知消息发送到群聊
     * @param webHook 钉钉机器人地址(配置机器人的webHook)
     * @param isAtAll 是否通知所有人
     * @param mobileList 通知具体人的手机号码列表
     * @param content 消息内容
     */
    public static void sendMsgToGroupChat(String webHook, boolean isAtAll, List<String> mobileList, String content){
        try {
            //组装请求内容
            String reqStr = buildReqStr(content, isAtAll, mobileList);
            //推送消息(http请求)
            String result = HttpUtil.post(webHook, reqStr);
            log.info("通知响应结果：{}",result);
        }catch (Exception e){
            log.error("webhook通知失败",e);
        }
    }
 
    /**
     * 组装请求报文（Map封装）
     * @param content 通知内容
     * @param isAtAll 是否@所有人
     * @param mobileList 通知具体人的手机号码
     * @return
     */
    private static String buildReqStr(String content, boolean isAtAll, List mobileList) {
        //消息内容
        Map<String, String> contentMap = Maps.newHashMap();
        contentMap.put("content", content);
        //通知人
        Map atMap = Maps.newHashMap();
        //1.是否通知所有人
        atMap.put("isAtAll", isAtAll);
        //2.通知具体人的手机号码列表
        atMap.put("atMobiles", mobileList);
 
        Map reqMap = Maps.newHashMap();
        reqMap.put("msgtype", "text");
        reqMap.put("text", contentMap);
        reqMap.put("at", atMap);
 
        return JSON.toJSONString(reqMap);
 
    }
 
    /**
     * 获取通知消息
     * @return
     */
    private static String getContent() {
        //钉钉机器人消息内容
        String content;
        //通过转码网站http://tool.chinaz.com/Tools/unicode.aspx
        // 选择中文转Unicode把钉钉表情转换成unicode编码，也可以直接用表情对应的中文设置
        String milkyTea = "晚上请你喝奶茶[奶茶][流鼻血][流鼻血]\u005b\u6d41\u9f3b\u8840\u005d";
        String NEWLINE = "\n";
        StringBuffer sb = new StringBuffer();
        sb.append("小姐姐，你好！")
                .append(NEWLINE)
                .append(milkyTea);
        content = sb.toString();
        return content;
    }
}