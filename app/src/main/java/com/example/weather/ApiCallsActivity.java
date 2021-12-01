package com.example.weather;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class ApiCallsActivity extends DBActivity {

    protected ListView historyList;
    protected EditText editCity, editZip, editCountry;
    protected Button btnUpdate, btnDelete, btnHomepage;
    private int currentId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_api_calls);

        //buttons
        btnHomepage = findViewById(R.id.back_to_main);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);

        //edit fields
        editCity = findViewById(R.id.edit_city);
        editZip = findViewById(R.id.edit_zip);
        editCountry = findViewById(R.id.edit_country);

        //text views
        dateValue = findViewById(R.id.date_value);
        weatherValue = findViewById(R.id.weather_value);
        tempValue = findViewById(R.id.temp_value);
        feelsValue = findViewById(R.id.feels_value);
        sunriseValue = findViewById(R.id.sunrise_value);
        sunsetValue = findViewById(R.id.sunset_value);

        historyList = findViewById(R.id.history_list);

        try {
            populateHistory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        historyList.setOnItemClickListener((adapterView, view, i, l) -> {
            TextView clickedText=view.findViewById(R.id.data_text);
            String selected = clickedText.getText().toString();
            String[] elements=selected.split("\t");
            currentId = Integer.parseInt(elements[0]);
            editCity.setText(elements[1]);
            editZip.setText(elements[2]);
            editCountry.setText(elements[3]);

            try {
                SelectSQL(
                        "select * from " + DBActivity.TABLE_NAME + " where " + DBActivity.COLUMN_ID + "=" + currentId + " order by " + DBActivity.COLUMN_CITY,
                        null,
                        (id, city, zip, country, date, request, response)->{
                            setWeatherText(response, date);
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnDelete.setOnClickListener(view -> {
            try{
                ExecSQL("delete from " + DBActivity.TABLE_NAME + " where " +
                                DBActivity.COLUMN_ID + " = ?",
                        new Object[]{currentId},
                        ()-> Toast.makeText(getApplicationContext(),
                                "Delete Successful", Toast.LENGTH_LONG).show()
                );

                populateHistory();
            }catch (Exception exception){
                Toast.makeText(getApplicationContext(),
                        "Delete Error: "+exception.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        btnUpdate.setOnClickListener(view -> {
            try{
                ExecSQL("update " + DBActivity.TABLE_NAME + " set " +
                                DBActivity.COLUMN_CITY + " = ?, " +
                                DBActivity.COLUMN_ZIP + " = ?, " +
                                DBActivity.COLUMN_COUNTRY + " = ? " +
                                "where " + DBActivity.COLUMN_ID + " = ?",
                        new Object[]{
                                editCity.getText().toString(),
                                editZip.getText().toString(),
                                editCountry.getText().toString(),
                                currentId},
                        ()-> Toast.makeText(getApplicationContext(),
                                "Update Successful", Toast.LENGTH_LONG).show()
                );

                populateHistory();
            }catch (Exception exception){
                Toast.makeText(getApplicationContext(),
                        "Update Error: "+exception.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        btnHomepage.setOnClickListener(view -> {
            finishActivity(200);
            Intent intent = new Intent(ApiCallsActivity.this, MainActivity.class);
            Bundle b=new Bundle();
            b.putBoolean("dbExists", true);
            intent.putExtras(b);
            startActivityForResult(intent, 200, b);
        });
    }

    protected void populateHistory() throws Exception{
        final ArrayList<String> listResults=
                new ArrayList<>();
        SelectSQL(
                "select * from " + DBActivity.TABLE_NAME + " order by " + DBActivity.COLUMN_CITY,
                null,
                (id, city, zip, country, date, request, response)->{
                    listResults.add(id+"\t"+city+"\t"+zip+"\t"+country+"\t"+date+"\n");
                }
        );
        historyList.clearChoices();
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.activity_list_view,
                R.id.data_text,
                listResults

        );
        historyList.setAdapter(arrayAdapter);
    }
}
