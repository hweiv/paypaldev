import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalAuthExample {

    public static void main(String[] args) {
        // 我的
//        String clientId = "Ach8iml2nSC9fha8K-6QIs51r51WJWMES59HqMqhb75BszO37bd2D4OmOi9AByAkPe9cDRCbc0McSWLm";
//        String clientSecret = "EDdOEVGnu7EDbJ5VLYi5uZcGDN_1hRrapREf9fMErimIkc8H_R6O1AeNLMpyFNTQ3hSdbcDeCbrk6GBz";
        // 公司的沙箱账号
        String clientId = "ASEJdmaf38uFmRfsvAiwnSKsgeAMrpBvY79IjtScNrQMEiXVWo2p8yhNvSyrwOUMFNyE5jWJC3uXAvAH";
        String clientSecret = "EBDDEVjWXrP_reqN-vCRdE930_2xq8KJzA2TeRQcBdVMoUyZ_Uha8CM2mWJIa_lweAmcmSzbSw21HKTB";


        String mode = "sandbox"; // or "live" for production

        APIContext context = new APIContext(clientId, clientSecret, mode);

        String accessToken = context.getAccessToken();
        System.out.println("Access Token: " + accessToken);
    }
}