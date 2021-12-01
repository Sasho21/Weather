package com.example.weather;

import android.annotation.SuppressLint;
import android.os.Bundle;



import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

public abstract class DBActivity extends RESTWeatherActivity {
    //table name
    public static final String TABLE_NAME = "weather";

    //column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_ZIP = "zip";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_REQUEST = "request";
    public static final String COLUMN_RESPONSE = "response";


    protected interface OnQuerySuccess{
        public void OnSuccess();
    }
    protected interface OnSelectSuccess{
        public void OnElementSelected(
                String id, String city, String zip, String country, String date, String request, String response
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    protected void SelectSQL(String SelectQ,
                             String[] args,
                             OnSelectSuccess success
    )
            throws Exception
    {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(getFilesDir().getPath()+"/weather.db", null);
        Cursor cursor = db.rawQuery(SelectQ, args);

        while (cursor.moveToNext()){
            @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
            @SuppressLint("Range") String city = cursor.getString(cursor.getColumnIndex(COLUMN_CITY));
            @SuppressLint("Range") String zip = cursor.getString(cursor.getColumnIndex(COLUMN_ZIP));
            @SuppressLint("Range") String country = cursor.getString(cursor.getColumnIndex(COLUMN_COUNTRY));
            @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
            @SuppressLint("Range") String request = cursor.getString(cursor.getColumnIndex(COLUMN_REQUEST));
            @SuppressLint("Range") String response = cursor.getString(cursor.getColumnIndex(COLUMN_RESPONSE));
            success.OnElementSelected(id, city, zip, country, date, request, response);
        }

        db.close();
    }

    protected void ExecSQL(String SQL, Object[] args, OnQuerySuccess success)
            throws Exception
    {
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(getFilesDir().getPath()+"/" + TABLE_NAME + ".db", null);
        Log.w("DB PATH", getFilesDir().getPath());

        if(args!=null)
            db.execSQL(SQL, args);
        else
            db.execSQL(SQL);

        db.close();
        success.OnSuccess();
    }

    protected void initDB() throws  Exception{
        ExecSQL(
                "create table if not exists " + TABLE_NAME + "( " +
                        COLUMN_ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_CITY + " text not null, " +
                        COLUMN_ZIP + " text, " +
                        COLUMN_COUNTRY + " text, " +
                        COLUMN_DATE + " text not null, " +
                        COLUMN_REQUEST + " text not null, " +
                        COLUMN_RESPONSE + " text not null" +
                        ")",
                null,
                ()-> Toast.makeText(getApplicationContext(),
                            "DB Init Successful", Toast.LENGTH_LONG).show()
        );
    }


}
