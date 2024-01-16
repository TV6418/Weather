package com.weihung.weather;

import android.os.AsyncTask;
import android.util.Log;

import com.weihung.weather.bean.WeatherRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherApiTask extends AsyncTask<Void, Void, String> {

    private WeatherRequest weatherRequest;
    private WeatherApiListener listener;

    public WeatherApiTask(WeatherRequest weatherRequest, WeatherApiListener listener) {
        this.weatherRequest = weatherRequest;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String TAG = "WeatherApi";
        String result = null;

        String apiUrl = "https://opendata.cwa.gov.tw/api/v1/rest/datastore/F-C0032-001";

        try {
            Log.d(TAG, "Query URL: " + apiUrl+ "?" + weatherRequest.toString());
            URL url = new URL(apiUrl + "?" +  weatherRequest.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                result = response.toString();


            } else {
                result = "HTTP error code: " + responseCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error making HTTP request: " + e.getMessage());
            result = "Error making HTTP request: " + e.getMessage();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (listener != null) {
            if (result != null) {
                listener.onWeatherApiSuccess(result);
            } else {
                listener.onWeatherApiError("Weather API request failed");
            }
        }
    }

    public interface WeatherApiListener {
        void onWeatherApiSuccess(String result);
        void onWeatherApiError(String errorMessage);
    }
}
