import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentHistory;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayPalTransactionHistory {

    public static void main(String[] args) {

        // Set up PayPal API credentials
        String clientId = "Ach8iml2nSC9fha8K-6QIs51r51WJWMES59HqMqhb75BszO37bd2D4OmOi9AByAkPe9cDRCbc0McSWLm";
        String clientSecret = "EDdOEVGnu7EDbJ5VLYi5uZcGDN_1hRrapREf9fMErimIkc8H_R6O1AeNLMpyFNTQ3hSdbcDeCbrk6GBz";
        String accessToken = "";
        // 环境
        String endpoint = "sandbox";
//        String accessToken = "Bearer A21AALBU0OYqlitJgV9XQ3Cgy7E-2PsLe_uhH5QQkjSwDgZJXd97-0mE6hyYeTno0x9B0I3rSfMFACA2VcNJovcuZLiSx_j9A";
        String startDate = "2023-03-29T00:00:00Z";
        String endDate = "2023-03-29T23:59:59Z";
        int count = 10;
        int startIndex = 0;

        try {
            APIContext apiContext = new APIContext(clientId, clientSecret, endpoint);
            // todo 这个Map不确定是什么东西
            Map<String, String> containerMap = new HashMap<>();
//            List<Payment> payments = Payment.list(apiContext, containerMap).getPayments();
            /**
             * Based on the provided code, the Payment.list() method call takes in a containerMap parameter, which is a Map object that contains the query parameters to be included in the API call.
             *
             * If you want to query PaymentHistory, the following fields can be included in the containerMap parameter:
             *
             * count: An Integer object that represents the maximum number of items to return in the API response. This parameter is optional and can be set to null if no maximum count is needed.
             * start_index: An Integer object that represents the index of the first item to return in the API response. This parameter is optional and can be set to null if no start index is needed.
             * start_date: A String object that represents the start date of the date range in ISO 8601 format.
             * end_date: A String object that represents the end date of the date range in ISO 8601 format.
             * sort_by: A String object that represents the field to sort the results by. This parameter is optional and can be set to null if no sorting is needed.
             * sort_order: A String object that represents the order to sort the results in. This parameter is optional and can be set to null if no sorting is needed.
             * Note that the specific query parameters that can be included in the containerMap parameter depend on the API endpoint being called. You can refer to the official PayPal documentation for the specific API endpoint that you are using to determine the available query parameters.
             *
             * I hope this helps! Let me know if you have any further questions.
             *
             * 这个containerMap可以为null，也可以包含以下字段
             * count：一个Integer对象，表示在API响应中返回的最大项数。此参数是可选的，如果不需要最大计数，则可以设置为null。
             * start_index：一个Integer对象，表示要在API响应中返回的第一个项的索引。此参数是可选的，如果不需要开始索引，则可以将其设置为null。
             * start_date：一个String对象，表示ISO 8601格式的日期范围的开始日期。
             * end_date：一个String对象，表示ISO 8601格式的日期范围的结束日期。
             * sort_by:String对象，表示要对结果进行排序的字段。此参数是可选的，如果不需要排序，则可以设置为null。
             * sort_order：一个String对象，表示对结果进行排序的顺序。此参数是可选的，如果不需要排序，则可以设置为null。
             * 请注意，containerMap参数中可以包含的特定查询参数取决于所调用的API端点。您可以参考PayPal官方文档，了解用于确定可用查询参数的特定API端点。
             */
            containerMap.put("start_date", startDate);
            containerMap.put("end_date", endDate);
            PaymentHistory paymentHistory = Payment.list(apiContext, containerMap);
            String lastResponse = Payment.getLastResponse();
            System.out.println("lastResponse" + lastResponse);
            List<Payment> payments = paymentHistory.getPayments();
            Payment payment1 = new Payment();
            Payment payment2 = payment1.create(apiContext);
            List<Transaction> transactions = payment2.getTransactions();

            for (Payment payment : payments) {
                System.out.println(payment.toJSON());
            }
        } catch (PayPalRESTException e) {
            System.err.println(e.getDetails());
        }
    }
}