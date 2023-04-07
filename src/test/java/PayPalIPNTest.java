import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class PayPalIPNTest {
    public static void main(String[] args) throws IOException {
        // Create a map of key-value pairs to send to PayPal
        Map<String, String> params = new HashMap<String, String>();
        params.put("cmd", "_notify-validate");
        params.put("txn_id", "1234567890");
        params.put("payment_status", "Completed");

        // Encode the map as a URL-encoded string
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        // Send the request to PayPal
        URL url = new URL("https://www.paypal.com/cgi-bin/webscr");
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.getOutputStream().write(postDataBytes);

        // Read the response from PayPal
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String res = in.readLine();
        in.close();

        // Check if the response is "VERIFIED"
        if (res.equals("VERIFIED")) {
            // Payment is verified, do something
            System.out.println("Payment verified");
        } else {
            // Payment is not verified, do something else
            System.out.println("Payment not verified");
        }
    }
}
