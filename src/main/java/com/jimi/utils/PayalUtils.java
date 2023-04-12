package com.jimi.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jimi.common.ScheduledTask;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

/**
 * Paypal工具类
 */
public class PayalUtils {
    private static final Logger logger = LoggerFactory.getLogger(PayalUtils.class);
    public static String getAccessToken(String clientId, String clientSecret) {
        logger.info("=PaypalUtils-getAccessToken.clientId:{}, clientSecret:{}", clientId, clientSecret);
        String accessToken = "";
        try {
            String authString = clientId + ":" + clientSecret;
            byte[] authBytes = authString.getBytes();
            String encodedAuthString = Base64.getEncoder().encodeToString(authBytes);
            URL url = new URL("https://api.sandbox.paypal.com/v1/oauth2/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Basic " + encodedAuthString);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write("grant_type=client_credentials");
            writer.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            JSONObject json = JSONObject.parseObject(response.toString());
            accessToken = json.getString("access_token");
        } catch (Exception e) {
            logger.error("=PaypalUtils-getAccessToken is error:{}", e);
        }
        return accessToken;
    }

    /**
     * 获取PayPal历史付费数据信息
     *
     * @param paramMap
     * @return
     */
    public static String getHistoryItem(Map<String, Object> paramMap) {
        // todo PayPal支付获取历史记录
        try {
            String startDate = "2023-03-29T00:00:00Z";
            String endDate = "2023-03-29T23:59:59Z";
//            URL url = new URL("https://api.sandbox.paypal.com/v1/reporting/transactions?start_date=" + dateFormat.format(oneYearAgo) + "&end_date=" + dateFormat.format(currentDate));
            URL url = new URL("https://api.sandbox.paypal.com/v1/reporting/transactions?start_date=" + startDate + "&end_date=" + endDate);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
//            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
//            writer.write("grant_type=client_credentials");
//            writer.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            JSONObject json = JSONObject.parseObject(response.toString());
            String transactionDetails = json.getString("transaction_details");
            JSONArray transactions = JSON.parseArray(transactionDetails);
            for (int i = 0; i < transactions.size(); i++) {
                JSONObject transaction = transactions.getJSONObject(i);
                String transactionInfo = transaction.getString("transaction_info");
                JSONObject obj = JSONObject.parseObject(transactionInfo);
                String transactionId = obj.getString("transaction_id");
                String transactionDate = obj.getString("transaction_date");
                String transactionAmount = obj.getString("transaction_amount");
                System.out.println("Transaction ID: " + transactionId);
                System.out.println("Transaction Date: " + transactionDate);
                System.out.println("Transaction Amount: " + transactionAmount);
                System.out.println("------------------------------");
            }
        } catch (Exception e) {
            logger.error("=PaypalUtils-getAccessToken is error:{}", e);
        }
        return null;
    }
}
