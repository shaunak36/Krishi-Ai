package com.krishiai.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private int             selectedRating = 0;
    private boolean         isHelpful      = true;
    private ImageButton[]   stars;
    private DatabaseHelper  db;
    private FirebaseFirestore firestore;
    private FirebaseAuth    mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        db        = new DatabaseHelper(this);
        firestore = FirebaseFirestore.getInstance();
        mAuth     = FirebaseAuth.getInstance();

        String diseaseName = getIntent().getStringExtra("disease_name");
        if (diseaseName == null) diseaseName = "Unknown";
        final String finalDisease = diseaseName;

        stars = new ImageButton[]{
                findViewById(R.id.star1), findViewById(R.id.star2),
                findViewById(R.id.star3), findViewById(R.id.star4),
                findViewById(R.id.star5)
        };

        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnClickListener(v -> {
                selectedRating = index + 1;
                for (int j = 0; j < stars.length; j++) {
                    stars[j].setImageResource(j < selectedRating
                            ? android.R.drawable.btn_star_big_on
                            : android.R.drawable.btn_star_big_off);
                }
            });
        }

        Button btnYes = findViewById(R.id.btnYes);
        Button btnNo  = findViewById(R.id.btnNo);

        btnYes.setOnClickListener(v -> {
            isHelpful = true;
            btnYes.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#2D6A4F")));
            btnYes.setTextColor(Color.WHITE);
            btnNo.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#D8F3DC")));
            btnNo.setTextColor(Color.parseColor("#2D6A4F"));
        });

        btnNo.setOnClickListener(v -> {
            isHelpful = false;
            btnNo.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#E53935")));
            btnNo.setTextColor(Color.WHITE);
            btnYes.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#D8F3DC")));
            btnYes.setTextColor(Color.parseColor("#2D6A4F"));
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            if (selectedRating == 0) {
                Toast.makeText(this,
                        "Please give a star rating ⭐",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            EditText etComment = findViewById(R.id.etComment);
            String   comment   = etComment.getText().toString().trim();
            String   timestamp = new java.text.SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a",
                    java.util.Locale.getDefault())
                    .format(new java.util.Date());

            // Save to local SQLite
            db.saveFeedback(finalDisease, selectedRating, isHelpful, comment);

            // Save to Firestore
            saveFeedbackToFirestore(
                    finalDisease, selectedRating, isHelpful, comment, timestamp);

            Toast.makeText(this,
                    "Feedback saved! Thank you 🙏",
                    Toast.LENGTH_SHORT).show();

            findViewById(R.id.btnSubmit).setVisibility(View.GONE);
            findViewById(R.id.layoutThankYou).setVisibility(View.VISIBLE);

            new Handler(Looper.getMainLooper())
                    .postDelayed(this::finish, 2000);
        });
    }

    private void saveFeedbackToFirestore(String disease, int rating,
                                         boolean helpful, String comment,
                                         String timestamp) {
        FirebaseUser user = mAuth.getCurrentUser();

        Map<String, Object> feedback = new HashMap<>();
        feedback.put("diseaseName", disease);
        feedback.put("rating",      rating);
        feedback.put("helpful",     helpful);
        feedback.put("comment",     comment);
        feedback.put("timestamp",   timestamp);
        feedback.put("userId",      user != null ? user.getUid() : "anonymous");
        feedback.put("userEmail",   user != null && user.getEmail() != null
                ? user.getEmail() : "anonymous");

        firestore.collection("feedback")
                .add(feedback)
                .addOnSuccessListener(ref ->
                        android.util.Log.d("KrishiAI",
                                "Feedback saved to Firestore: " + ref.getId()))
                .addOnFailureListener(e ->
                        android.util.Log.e("KrishiAI",
                                "Firestore feedback error: " + e.getMessage()));
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}