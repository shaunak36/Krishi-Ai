package com.krishiai.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        checkModelStatus();
        loadLanguage();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        ((RadioButton)findViewById(R.id.radioEnglish)).setOnClickListener(v -> saveLanguage("EN"));
        ((RadioButton)findViewById(R.id.radioHindi)).setOnClickListener(v -> saveLanguage("HI"));
        ((RadioButton)findViewById(R.id.radioMarathi)).setOnClickListener(v -> saveLanguage("MR"));
        // Add logout button click listener
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkModelStatus() {
        TextView tv = findViewById(R.id.tvOfflineStatus);
        try {
            getAssets().open("krishi_model.tflite").close();
            tv.setText("✅  AI Model Loaded — Fully Offline Ready");
            tv.setTextColor(0xFF2D6A4F);
        } catch (Exception e) {
            tv.setText("⚠️  Model not found — add krishi_model.tflite to assets/");
            tv.setTextColor(0xFFE53935);
        }
    }

    private void loadLanguage() {
        String lang = getSharedPreferences("KrishiAI_Prefs", MODE_PRIVATE)
                .getString("language", "EN");
        if (lang.equals("HI"))
            ((RadioButton)findViewById(R.id.radioHindi)).setChecked(true);
        else if (lang.equals("MR"))
            ((RadioButton)findViewById(R.id.radioMarathi)).setChecked(true);
        else
            ((RadioButton)findViewById(R.id.radioEnglish)).setChecked(true);
    }

    private void saveLanguage(String lang) {
        getSharedPreferences("KrishiAI_Prefs", MODE_PRIVATE)
                .edit().putString("language", lang).apply();
        Toast.makeText(this, "Language saved!", Toast.LENGTH_SHORT).show();
    }
}