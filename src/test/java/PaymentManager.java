import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.jimi.utils.DateUtils;
import urn.ebay.api.PayPalAPI.*;
import urn.ebay.apis.eBLBaseComponents.PaymentTransactionSearchResultType;

public class PaymentManager {

public static void main(String[] args) {

    Map<String,String> configMap = new HashMap<String,String>();

    configMap.put("mode", "sandbox");

    // Account Credential
    configMap.put("acct1.UserName", "sb-liqjq15336561_api1.business.example.com");
    configMap.put("acct1.Password", "D3K2EWYGS99WTR3E");
    configMap.put("acct1.Signature", "A9Nn.fxtMXsdN.fswm3EwL8T5ws-AwkJDTSeszpcIET-Hk.MxbJ8Jh6S");
    // Subject is optional, only required in case of third party permission
    //configMap.put("acct1.Subject", "");

    // Sample Certificate credential
    // configMap.put("acct2.UserName", "certuser_biz_api1.paypal.com");
    // configMap.put("acct2.Password", "D6JNKKULHN3G5B8A");
    // configMap.put("acct2.CertKey", "password");
    // configMap.put("acct2.CertPath", "resource/sdk-cert.p12");
    // configMap.put("acct2.AppId", "APP-80W284485P519543T");


    TransactionSearchReq txnreq = new TransactionSearchReq();
    TransactionSearchRequestType requestType = new TransactionSearchRequestType();

//    requestType.setStartDate("2023-03-29T00:00:00.000Z");
    requestType.setStartDate(DateUtils.getTodayFormat());
//    requestType.setEndDate("2023-03-30T23:59:59.000Z");
    requestType.setEndDate(DateUtils.getNowFormat());
    requestType.setVersion("95.0");
    requestType.setTransactionID("");
    txnreq.setTransactionSearchRequest(requestType);

    PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(configMap);

    try {
        TransactionSearchResponseType txnresponse = service.transactionSearch(txnreq, configMap.get("acct1.UserName"));

        List<PaymentTransactionSearchResultType> transactions = txnresponse.getPaymentTransactions();
        System.out.println(JSON.toJSONString(transactions));
        //
        for (PaymentTransactionSearchResultType transaction : transactions) {

        }

        for (int i = 0; i < transactions.size(); i++) {
            System.out.println(transactions.get(i).getPayer());
        }

    } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
}
}