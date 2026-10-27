package com.krishiai.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherHelper {

    public interface WeatherCallback {
        void onWeatherLoaded(String temp, String desc,
                             String emoji, String advice);
        void onError();
    }

    // Free OpenWeatherMap API key — works for 60 calls/min
    private static final String API_KEY = "bd5e378503939ddaee76f12ad7a97608";

    @SuppressLint("MissingPermission")
    public static void getWeather(Context context, WeatherCallback callback) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // No permission — use Mumbai as default
            fetchWeather(19.0760, 72.8777, callback);
            return;
        }

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(context);

        client.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            fetchWeather(location.getLatitude(),
                                    location.getLongitude(), callback);
                        } else {
                            // Default to Mumbai
                            fetchWeather(19.0760, 72.8777, callback);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        fetchWeather(19.0760, 72.8777, callback);
                    }
                });
    }

    private static void fetchWeather(double lat, double lon,
                                     WeatherCallback callback) {
        new Thread(() -> {
            try {
                String urlStr =
                        "https://api.openweathermap.org/data/2.5/weather?" +
                                "lat=" + lat + "&lon=" + lon +
                                "&appid=" + API_KEY +
                                "&units=metric";

                URL url = new URL(urlStr);
                HttpURLConnection conn =
                        (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                String tempStr = extractTemperature(json);
                String descStr = extractDescription(json);
                int weatherId = extractWeatherId(json);

                String emoji  = getWeatherEmoji(weatherId);
                String advice = getFarmingAdvice(weatherId,
                        Double.parseDouble(tempStr.replace("°C", "")));

                callback.onWeatherLoaded(tempStr, descStr, emoji, advice);

            } catch (Exception e) {
                callback.onError();
            }
        }).start();
    }

    private static String extractTemperature(JSONObject json) throws Exception {
        double temp = json.getJSONObject("main").getDouble("temp");
        return (int) temp + "°C";
    }

    private static String extractDescription(JSONObject json) throws Exception {
        String desc = json.getJSONArray("weather")
                .getJSONObject(0).getString("description");
        return capitalizeFirst(desc);
    }

    private static int extractWeatherId(JSONObject json) throws Exception {
        return json.getJSONArray("weather")
                .getJSONObject(0).getInt("id");
    }

    private static String getWeatherEmoji(int id) {
        if (id >= 200 && id < 300) return "⛈️";
        if (id >= 300 && id < 400) return "🌦️";
        if (id >= 500 && id < 600) return "🌧️";
        if (id >= 600 && id < 700) return "❄️";
        if (id >= 700 && id < 800) return "🌫️";
        if (id == 800)             return "☀️";
        if (id == 801)             return "🌤️";
        if (id <= 804)             return "⛅";
        return "🌡️";
    }

    private static String getFarmingAdvice(int id, double temp) {
        if (id >= 200 && id < 300)
            return "Avoid spraying — thunderstorm";
        if (id >= 500 && id < 600)
            return "Good for irrigation — avoid fungicides";
        if (id == 800 && temp > 35)
            return "Hot & dry — water crops early morning";
        if (id == 800)
            return "Good for spraying pesticides";
        if (id >= 801 && id <= 804)
            return "Partly cloudy — ideal for farm work";
        if (temp < 15)
            return "Cold weather — watch for frost damage";
        return "Monitor crops regularly";
    }

    private static String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}