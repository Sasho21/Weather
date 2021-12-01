package com.example.weather;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends DBActivity implements View.OnClickListener {

    protected EditText txtCity, txtZip, txtCountry;
    protected Button btnWeather, btnHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b=getIntent().getExtras();
        if(!(b!=null && b.getBoolean("dbExists"))){
            try {
                initDB();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setContentView(R.layout.activity_main);

        //buttons
        btnWeather = findViewById(R.id.weather_button);
        btnHistory = findViewById(R.id.previous_calls);

        //edit fields
        txtCity = findViewById(R.id.city_field);
        txtCountry = findViewById(R.id.country_field);
        txtZip = findViewById(R.id.zip_field);

        //text views
        dateValue = findViewById(R.id.date_value);
        weatherValue = findViewById(R.id.weather_value);
        tempValue = findViewById(R.id.temp_value);
        feelsValue = findViewById(R.id.feels_value);
        sunriseValue = findViewById(R.id.sunrise_value);
        sunsetValue = findViewById(R.id.sunset_value);

        btnWeather.setOnClickListener(this::onClick);
        btnHistory.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ApiCallsActivity.class);
            startActivity(intent);
        });
    }

    protected void updateRecords(String city, String zip, String country, String date, String request, String response) {
        if (request != null && response != null) {
            try {
                ExecSQL(
                        "INSERT INTO " + DBActivity.TABLE_NAME + "(" +
                                DBActivity.COLUMN_CITY + ", " +
                                DBActivity.COLUMN_ZIP + ", " +
                                DBActivity.COLUMN_COUNTRY + ", " +
                                DBActivity.COLUMN_DATE + ", " +
                                DBActivity.COLUMN_REQUEST + ", " +
                                DBActivity.COLUMN_RESPONSE +
                                ") " + "VALUES(?, ?, ?, ?, ?, ?) ",
                        new Object[]{
                                city,
                                zip,
                                country,
                                date,
                                request,
                                response
                        },
                        ()-> Toast.makeText(getApplicationContext(),
                                    "Record Inserted", Toast.LENGTH_LONG).show()

                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        String city = txtCity.getText().toString();
        String zip = txtZip.getText().toString();
        String country = txtCountry.getText().toString().toLowerCase(Locale.ROOT);
        Date date = new Date();
        String currentDate = date.toString().substring(0, date.toString().length() - 8);

        HashMap<String, String> requestData = new HashMap<>();
        if (!zip.equals("") && !country.equals("")) {
            requestData.put("zip", zip + "," + country);
        } else {
            requestData.put("q", city + ",," + country);
        }
        requestData.put("units", "metric");

        String request = null;
        String response = null;

        try {
            request = buildRequest(requestData, RESTWeatherActivity.urlWeather);
            response = responseData(request);

        } catch (Exception e) {
            e.printStackTrace();
        }

        updateRecords(city, zip, country, currentDate, request, response);

        setWeatherText(response, currentDate);
    }
}