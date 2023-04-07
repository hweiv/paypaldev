import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

public class PayPalTransaction {
 
    public static void main(String[] args) {
 
        // Get current date
        Date currentDate = new Date();
 
        // Set date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
 
        // Get date one year ago
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.YEAR, -1);
        Date oneYearAgo = calendar.getTime();
 
        // Print date range
        System.out.println("Transaction history from " + dateFormat.format(oneYearAgo) + " to " + dateFormat.format(currentDate));
 
        // Query PayPal API for transactions within date range
        // INSERT CODE HERE
        // Set up PayPal API credentials
        String clientId = "Ach8iml2nSC9fha8K-6QIs51r51WJWMES59HqMqhb75BszO37bd2D4OmOi9AByAkPe9cDRCbc0McSWLm";
        String clientSecret = "EDdOEVGnu7EDbJ5VLYi5uZcGDN_1hRrapREf9fMErimIkc8H_R6O1AeNLMpyFNTQ3hSdbcDeCbrk6GBz";
        String accessToken = "";

// Get access token
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
            JSONObject json = new JSONObject(response.toString());
            accessToken = json.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace();
        }

// Query PayPal API for transactions within date range
        try {
            String startDate = "2023-03-29T00:00:00Z";
            String endDate = "2023-03-29T23:59:59Z";
//            URL url = new URL("https://api.sandbox.paypal.com/v1/reporting/transactions?start_date=" + dateFormat.format(oneYearAgo) + "&end_date=" + dateFormat.format(currentDate));
            URL url = new URL("https://api.sandbox.paypal.com/v1/reporting/transactions?start_date=" + startDate + "&end_date=" + endDate);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
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
            JSONObject json = new JSONObject(response.toString());
            JSONArray transactions = json.getJSONArray("transaction_details");
            for (int i = 0; i < transactions.length(); i++) {
                JSONObject transaction = transactions.getJSONObject(i);
                String transactionInfo = transaction.getString("transaction_info");
                JSONObject obj = new JSONObject(transactionInfo);
                String transactionId = obj.getString("transaction_id");
                String transactionDate = obj.getString("transaction_date");
                String transactionAmount = obj.getString("transaction_amount");
                System.out.println("Transaction ID: " + transactionId);
                System.out.println("Transaction Date: " + transactionDate);
                System.out.println("Transaction Amount: " + transactionAmount);
                System.out.println("------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
 
}