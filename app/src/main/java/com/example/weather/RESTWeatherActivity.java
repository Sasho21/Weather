package com.example.weather;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

public abstract class RESTWeatherActivity extends AppCompatActivity {
    protected static final String urlWeather = "https://api.openweathermap.org/data/2.5/weather?";
    protected static final String urlForecast = "https://api.openweathermap.org/data/2.5/forecast?";
    private String responseData = null;

    protected TextView dateValue, weatherValue, tempValue, feelsValue, sunriseValue, sunsetValue;

    protected String buildRequest(HashMap<String, String> params, String urlAddress) throws Exception {
        StringBuffer feedback = new StringBuffer();

        boolean first = true;

        for(Map.Entry<String, String> entry: params.entrySet()) {
            if (first)
                first = false;
            else
                feedback.append("&");

            feedback.append(URLEncoder.encode(entry.getKey(), "utf-8"));
            feedback.append("=");
            feedback.append(URLEncoder.encode(entry.getValue(), "utf-8"));
        }
        feedback.append("&appId=").append(getResources().getString(R.string.open_weather_api_key));

        return urlAddress + feedback.toString();
    }

    protected String responseData(String requestUrl) throws Exception {
        AsyncHttpClient client = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder getRequest = client.prepareGet(requestUrl);

        Thread weatherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final ListenableFuture<Response> listenableFuture = client.executeRequest(getRequest.build());
                Response response = null;
                try {
                    response = listenableFuture.get();
                    if (response.hasResponseBody()) {
                        InputStream responseBody = response.getResponseBodyAsStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseBody, "UTF-8"));

                        responseData = bufferedReader.readLine();
                    }
                } catch (ExecutionException | InterruptedException | IOException e) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
                    e.printStackTrace();
                }
            }
        });
        weatherThread.start();
        weatherThread.join();

        return responseData.toString();
    }

    protected void setWeatherText(String response, String date) {
        JSONObject resultSet = null;
        try {
            resultSet = new JSONObject(response);
            JSONArray weatherArray = resultSet.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);
            JSONObject temperature = resultSet.getJSONObject("main");
            JSONObject dayLength = resultSet.getJSONObject("sys");
            Date sunriseTime = new Date(Long.parseLong(dayLength.getString("sunrise") + "000"));
            Date sunsetTime = new Date(Long.parseLong(dayLength.getString("sunset") + "000"));

            dateValue.setText(date);
            weatherValue.setText(weather.getString("description"));
            tempValue.setText(temperature.getString("temp") + "\u2103");
            feelsValue.setText(temperature.getString("feels_like") + "\u2103");
            sunriseValue.setText(sunriseTime.getHours() + ":" + sunriseTime.getMinutes());
            sunsetValue.setText(sunsetTime.getHours() + ":" + sunsetTime.getMinutes());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
