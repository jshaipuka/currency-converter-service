package com.serverless;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class AlphaVantageClient {
    private static final String API_KEY = System.getenv("ALPHAVANTAGE_API_KEY");
    private static final String BASE_URL = "https://www.alphavantage.co";

    private String fetch(String query) {
        String url = BASE_URL + "/query?" + query;

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(new HttpGet(url));) {

            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    String getRates(String from, String to) {
        String query = String.format("function=CURRENCY_EXCHANGE_RATE&from_currency=%s&to_currency=%s&apikey=%s", from, to, API_KEY);
        return fetch(query);
    }

    String getRatesHistory(String from, String to, String interval) {
        String query = "";

        if ("monthly".equals(interval)) {
            query = String.format("function=FX_DAILY&from_symbol=%s&to_symbol=%s&apikey=%s", from, to, API_KEY);
        } else if ("daily".equals(interval)) {
            query = String.format("function=FX_INTRADAY&from_symbol=%s&to_symbol=%s&interval=60min&outputsize=full&apikey=%s", from, to, API_KEY);
        } else if ("hourly".equals(interval)) {
            query = String.format("function=FX_INTRADAY&from_symbol=%s&to_symbol=%s&interval=5min&outputsize=full&apikey=%s", from, to, API_KEY);
        }

        return fetch(query);
    }

}
