package com.weihung.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.weihung.weather.bean.Records;
import com.weihung.weather.bean.Result;
import com.weihung.weather.bean.WeatherRequest;
import com.weihung.weather.bean.WeatherResponse;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;

    private String cityName;
    private String stateName;

    private double maxT;
    private double minT;


    TextView text_latitude;
    TextView text_longitude;
    TextView text_local_area;

    TextView text_minT;
    TextView text_maxT;
    Button btn_query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main_weather);


        text_latitude = (TextView) findViewById(R.id.t_latitude);
        text_longitude = (TextView) findViewById(R.id.t_longitude);
        text_local_area = (TextView) findViewById(R.id.text_local_city);
        text_minT = (TextView) findViewById(R.id.t_minT);
        text_maxT = (TextView) findViewById(R.id.t_maxT);

        btn_query = (Button) findViewById(R.id.btn_query);

        //取得定位服務
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // 檢查權限並請求定位
        checkLocationPermission();

        initListener();


    }

    private void queryWeather(){

        Log.d("Query", "Query 開始");

        //發動 API 查詢 天氣資料
        //https://opendata.cwa.gov.tw/dist/opendata-swagger.html#/%E9%A0%90%E5%A0%B1/get_v1_rest_datastore_F_C0032_001

        // 1. 建立 WeatherRequest 物件
        WeatherRequest weatherRequest = new WeatherRequest();
        weatherRequest.setAuthorization("CWA-5172D0EA-F69E-4A47-9BB2-07F7E8836911");
        weatherRequest.setFormat("JSON");
        if(stateName != null){
            weatherRequest.setLocationName(stateName);
        }else {
            Toast.makeText(MainActivity.this, "無法取得縣市名稱，預設查詢臺北市天氣", Toast.LENGTH_LONG).show();
            weatherRequest.setLocationName("臺北市");
        }
//                weatherRequest.setElementName("Wx,PoP,CI,MaxT,MinT");
        weatherRequest.setElementName("MaxT,MinT");
        weatherRequest.setSort("time");

        // 2. 將 WeatherRequest 物件使用 json HTTP GET 方式傳送至 API
        WeatherApiTask weatherApiTask = new WeatherApiTask(weatherRequest, new WeatherApiTask.WeatherApiListener() {
            @Override
            public void onWeatherApiSuccess(String result) {
                if (result != null) {
                    Log.d("Query", "Query 成功");
                    Log.d("Query", result);
                    // 在你的API請求成功的回調中使用以下代碼
                    Gson gson = new Gson();
//                            WeatherResponse weatherResponse = gson.fromJson(result, WeatherResponse.class);
//                            Result weatherResult = weatherResponse.getResult();
//                            Records records = weatherResult.getRecords();

                    JsonObject jsonObject = gson.fromJson(result, JsonObject.class);

// 獲取records對象
                    JsonObject records = jsonObject.getAsJsonObject("records");
                    if(records.getAsJsonArray("location") == null){
                        Toast.makeText(MainActivity.this, "查無資料", Toast.LENGTH_LONG).show();
                        return;
                    }
// 獲取MinT陣列
                    JsonArray minTArray = records.getAsJsonArray("location")
                            .get(0) // 假設只有一個location元素
                            .getAsJsonObject()
                            .getAsJsonArray("weatherElement")
                            .get(0) // 假設MinT是第一個weatherElement元素
                            .getAsJsonObject()
                            .getAsJsonArray("time");

// 獲取MaxT陣列
                    JsonArray maxTArray = records.getAsJsonArray("location")
                            .get(0) // 假設只有一個location元素
                            .getAsJsonObject()
                            .getAsJsonArray("weatherElement")
                            .get(1) // 假設MaxT是第二個weatherElement元素
                            .getAsJsonObject()
                            .getAsJsonArray("time");

// 遍歷MinT和MaxT陣列，並取出最低溫度和最高溫度值
                    for (int i = 0; i < minTArray.size(); i++) {
                        JsonObject minTObject = minTArray.get(i).getAsJsonObject();
                        String startTime = minTObject.get("startTime").getAsString();
                        String endTime = minTObject.get("endTime").getAsString();
                        JsonObject parameter = minTObject.getAsJsonObject("parameter");
                        String minTemperature = parameter.get("parameterName").getAsString();
                        String minTemperatureUnit = parameter.get("parameterUnit").getAsString();

                        // 在這裡處理最低溫度數據
                        System.out.println("MinT - Start Time: " + startTime);
                        System.out.println("MinT - End Time: " + endTime);
                        System.out.println("MinT - Temperature: " + minTemperature + " " + minTemperatureUnit);

                        //存入最低溫度
                        if(minT > Double.parseDouble(minTemperature)){
                            minT = Double.parseDouble(minTemperature);
                        }else if(minT == 0){
                            minT = Double.parseDouble(minTemperature);
                        }


                    }

                    for (int i = 0; i < maxTArray.size(); i++) {
                        JsonObject maxTObject = maxTArray.get(i).getAsJsonObject();
                        String startTime = maxTObject.get("startTime").getAsString();
                        String endTime = maxTObject.get("endTime").getAsString();
                        JsonObject parameter = maxTObject.getAsJsonObject("parameter");
                        String maxTemperature = parameter.get("parameterName").getAsString();
                        String maxTemperatureUnit = parameter.get("parameterUnit").getAsString();

                        // 在這裡處理最高溫度數據
                        System.out.println("MaxT - Start Time: " + startTime);
                        System.out.println("MaxT - End Time: " + endTime);
                        System.out.println("MaxT - Temperature: " + maxTemperature + " " + maxTemperatureUnit);


                        //存入最高溫度
                        if(maxT < Double.parseDouble(maxTemperature)){
                            maxT = Double.parseDouble(maxTemperature);
                        }else if(maxT == 0){
                            maxT = Double.parseDouble(maxTemperature);
                        }

                    }

                    //更新最低及最高溫度
                    text_minT.setText(String.format("%.1f", minT));
                    text_maxT.setText(String.format("%.1f", maxT));


                } else {
                    Log.d("Query", "Query 失敗");
                }
            }

            @Override
            public void onWeatherApiError(String errorMessage) {
                Log.d("Query", "Query 失敗");
            }
        });
        // 開始執行WeatherApiTask
        weatherApiTask.execute();
    }


    private void initListener() {
        btn_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                queryWeather();

            }
        });
    }


    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            //getLastLocation();
            createLocationRequest();
        }
    }


    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            text_latitude.setText(String.format("%.1f", latitude));
                            text_longitude.setText(String.format("%.1f", longitude));

                            // 逆地理編碼以獲取縣市名稱

                            updateUI(location);
                        }
                    }
                });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    createLocationRequest();
                }
            } else {
                // 權限被拒絕
                Toast.makeText(this, "需要定位權限", Toast.LENGTH_LONG).show();
            }
        }
    }




    private void createLocationRequest() {
        // 建立LocationRequest物件 每10秒更新一次
        LocationRequest locationRequest = new LocationRequest.Builder(10000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        // 開始更新位置
        startLocationUpdates(locationRequest);
        queryWeather();
    }

    private void startLocationUpdates(LocationRequest locationRequest) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            return;
                        }
                        for (Location location : locationResult.getLocations()) {
                            // 更新 UI
                            updateUI(location);
                        }
                    }
                },
                null /* Looper */);
    }

    private void updateUI(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            text_latitude.setText(String.format("%.1f", latitude));
            text_longitude.setText(String.format("%.1f", longitude));
            // 逆地理編碼等後續操作
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.CHINESE);
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    cityName = addresses.get(0).getLocality();
                    stateName = addresses.get(0).getAdminArea().replace('台','臺');
                    text_local_area.setText(cityName + ", " + stateName);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





}