package com.krishiai.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanActivity extends AppCompatActivity {

    private Bitmap selectedBitmap;
    private Uri    cameraUri;

    private LinearLayout layoutPlaceholder, layoutAnalyzing;
    private ImageView    imgLeaf;
    private Button       btnAnalyze;

    // ── Camera launcher ───────────────────────────────────────
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(), success -> {
                        if (success && cameraUri != null) {
                            try {
                                InputStream is =
                                        getContentResolver().openInputStream(cameraUri);
                                selectedBitmap = BitmapFactory.decodeStream(is);
                                showPreview(cameraUri);
                            } catch (Exception e) {
                                Toast.makeText(this,
                                        "Error loading image: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    // ── Gallery launcher ──────────────────────────────────────
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(), uri -> {
                        if (uri != null) {
                            try {
                                InputStream is =
                                        getContentResolver().openInputStream(uri);
                                selectedBitmap = BitmapFactory.decodeStream(is);
                                showPreview(uri);
                            } catch (Exception e) {
                                Toast.makeText(this,
                                        "Error loading image: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    // ── Permission launcher ───────────────────────────────────
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(), granted -> {
                        if (granted) {
                            launchCamera();
                        } else {
                            Toast.makeText(this,
                                    "Camera permission is needed to take photos",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // Find views
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);
        layoutAnalyzing   = findViewById(R.id.layoutAnalyzing);
        imgLeaf           = findViewById(R.id.imgLeaf);
        btnAnalyze        = findViewById(R.id.btnAnalyze);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Camera button
        findViewById(R.id.btnCamera).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        // Gallery button
        findViewById(R.id.btnGallery).setOnClickListener(v ->
                galleryLauncher.launch("image/*"));

        // Detect button
        btnAnalyze.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                runDetection();
            } else {
                Toast.makeText(this,
                        "Please select an image first",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Launch camera ─────────────────────────────────────────
    private void launchCamera() {
        try {
            String timestamp = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File storageDir = getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES);
            File photoFile = File.createTempFile(
                    "KRISHI_" + timestamp, ".jpg", storageDir);
            cameraUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", photoFile);
            cameraLauncher.launch(cameraUri);
        } catch (Exception e) {
            Toast.makeText(this,
                    "Camera error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ── Show image preview ────────────────────────────────────
    private void showPreview(Uri uri) {
        layoutPlaceholder.setVisibility(View.GONE);
        imgLeaf.setVisibility(View.VISIBLE);
        btnAnalyze.setVisibility(View.VISIBLE);
        Glide.with(this).load(uri).centerCrop().into(imgLeaf);

        // Tip toast
        Toast.makeText(this,
                "💡 Best results with close-up leaf photos",
                Toast.LENGTH_LONG).show();
    }

    // ── Run AI detection ──────────────────────────────────────
    private void runDetection() {
        layoutAnalyzing.setVisibility(View.VISIBLE);
        btnAnalyze.setEnabled(false);

        new Thread(() -> {
            try {
                DiseaseClassifier classifier =
                        new DiseaseClassifier(this);
                DiseaseClassifier.Result result =
                        classifier.classify(selectedBitmap);
                classifier.close();

                runOnUiThread(() -> {
                    layoutAnalyzing.setVisibility(View.GONE);
                    btnAnalyze.setEnabled(true);

                    Intent intent = new Intent(
                            this, ResultActivity.class);
                    intent.putExtra("disease_name",
                            result.displayName);
                    intent.putExtra("confidence",
                            result.confidence);
                    intent.putExtra("plant_name",
                            result.plantName);
                    intent.putExtra("chemical_cure",
                            result.chemicalCure);
                    intent.putExtra("organic_cure",
                            result.organicCure);
                    intent.putExtra("prevention",
                            result.prevention);
                    startActivity(intent);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    layoutAnalyzing.setVisibility(View.GONE);
                    btnAnalyze.setEnabled(true);
                    Toast.makeText(this,
                            "Detection error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}