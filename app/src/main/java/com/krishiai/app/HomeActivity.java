package com.krishiai.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout newsContainer;
    private TextView     tvTemp, tvWeatherDesc, tvWeatherEmoji;
    private String       currentLang = "EN";
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        currentLang = getSharedPreferences("KrishiAI_Prefs", MODE_PRIVATE)
                .getString("language", "EN");

        newsContainer  = findViewById(R.id.newsContainer);
        tvTemp         = findViewById(R.id.tvTemp);
        tvWeatherDesc  = findViewById(R.id.tvWeatherDesc);
        tvWeatherEmoji = findViewById(R.id.tvWeatherEmoji);

        loadNews();
        loadWeather();

        findViewById(R.id.btnScan).setOnClickListener(v ->
                startActivity(new Intent(this, ScanActivity.class)));
        findViewById(R.id.btnChat).setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));
        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        String newLang = getSharedPreferences("KrishiAI_Prefs", MODE_PRIVATE)
                .getString("language", "EN");
        if (!newLang.equals(currentLang)) {
            currentLang = newLang;
            loadNews();
        }
    }

    // ── LIVE WEATHER ──────────────────────────────────────
    private void loadWeather() {
        // Request location permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }

        // Show loading state
        if (tvTemp != null) tvTemp.setText("...");
        if (tvWeatherDesc != null) tvWeatherDesc.setText("Loading weather...");

        WeatherHelper.getWeather(this, new WeatherHelper.WeatherCallback() {
            @Override
            public void onWeatherLoaded(String temp, String desc,
                                        String emoji, String advice) {
                runOnUiThread(() -> {
                    if (tvTemp        != null) tvTemp.setText(temp);
                    if (tvWeatherDesc != null) tvWeatherDesc.setText(desc + " — " + advice);
                    if (tvWeatherEmoji != null) tvWeatherEmoji.setText(emoji);
                });
            }

            @Override
            public void onError() {
                runOnUiThread(() -> {
                    if (tvTemp        != null) tvTemp.setText("32°C");
                    if (tvWeatherDesc != null) tvWeatherDesc.setText("Sunny — Good for spraying");
                    if (tvWeatherEmoji != null) tvWeatherEmoji.setText("☀️");
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            loadWeather();
        }
    }

    // ── NEWS WITH IMAGES ──────────────────────────────────
    private void loadNews() {
        newsContainer.removeAllViews();
        List<NewsHelper.NewsItem> newsList = NewsHelper.getNews(currentLang);

        for (NewsHelper.NewsItem item : newsList) {
            View card = LayoutInflater.from(this)
                    .inflate(R.layout.item_news_card, newsContainer, false);

            TextView  tvTitle   = card.findViewById(R.id.tvNewsTitle);
            TextView  tvSummary = card.findViewById(R.id.tvNewsSummary);
            TextView  tvMeta    = card.findViewById(R.id.tvNewsMeta);
            ImageView ivNews    = card.findViewById(R.id.ivNewsImage);

            tvTitle.setText(item.title);
            tvSummary.setText(item.summary);
            tvMeta.setText(item.date + " \u00B7 " + item.source);

            if (item.isAlert) {
                tvTitle.setTextColor(0xFFE53935);
            } else {
                tvTitle.setTextColor(0xFF1A2E1F);
            }

            // Load news image from URL using Glide
            if (item.imageUrl != null && !item.imageUrl.isEmpty()
                    && ivNews != null) {
                ivNews.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(item.imageUrl)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(ivNews);
            } else if (ivNews != null) {
                ivNews.setVisibility(View.GONE);
            }

            newsContainer.addView(card);
        }
    }
}