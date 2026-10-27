package com.krishiai.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private FirebaseAuth        mAuth;
    private FirebaseFirestore   db;
    private GoogleSignInClient  mGoogleSignInClient;

    // Phone auth
    private String verificationId;

    // UI
    private EditText   etEmail, etPassword, etName;
    private LinearLayout layoutName;
    private Button     btnEmailAuth, btnGoogle, btnPhone;
    private Button     btnTabLogin, btnTabRegister;
    private TextView   tvForgotPassword, tvSkip;

    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        // If already logged in → go to Home
        if (mAuth.getCurrentUser() != null) {
            goToHome();
            return;
        }

        // Google Sign-In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Find views
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        etName           = findViewById(R.id.etName);
        layoutName       = findViewById(R.id.layoutName);
        btnEmailAuth     = findViewById(R.id.btnEmailAuth);
        btnGoogle        = findViewById(R.id.btnGoogle);
        btnPhone         = findViewById(R.id.btnPhone);
        btnTabLogin      = findViewById(R.id.btnTabLogin);
        btnTabRegister   = findViewById(R.id.btnTabRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSkip           = findViewById(R.id.tvSkip);

        // Tab switches
        btnTabLogin.setOnClickListener(v -> switchMode(true));
        btnTabRegister.setOnClickListener(v -> switchMode(false));

        // Email auth
        btnEmailAuth.setOnClickListener(v -> handleEmailAuth());

        // Google
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Phone OTP
        btnPhone.setOnClickListener(v -> showPhoneDialog());

        // Forgot password
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        // Skip
        tvSkip.setOnClickListener(v -> {
            getSharedPreferences("KrishiAI_Prefs", MODE_PRIVATE)
                    .edit().putBoolean("skipped_login", true).apply();
            goToHome();
        });
    }

    // ── SWITCH LOGIN / REGISTER ───────────────────────────────
    private void switchMode(boolean loginMode) {
        isLoginMode = loginMode;
        if (loginMode) {
            btnEmailAuth.setText("Login");
            layoutName.setVisibility(View.GONE);
            tvForgotPassword.setVisibility(View.VISIBLE);
            btnTabLogin.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF2D6A4F));
            btnTabRegister.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF1A4332));
        } else {
            btnEmailAuth.setText("Create Account");
            layoutName.setVisibility(View.VISIBLE);
            tvForgotPassword.setVisibility(View.GONE);
            btnTabLogin.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF1A4332));
            btnTabRegister.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF2D6A4F));
        }
    }

    // ── EMAIL AUTH ────────────────────────────────────────────
    private void handleEmailAuth() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email"); return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be 6+ characters"); return;
        }

        btnEmailAuth.setEnabled(false);
        btnEmailAuth.setText("Please wait...");

        if (isLoginMode) {
            // Login
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        Toast.makeText(this,
                                "Welcome back! 🌾", Toast.LENGTH_SHORT).show();
                        goToHome();
                    })
                    .addOnFailureListener(e -> {
                        btnEmailAuth.setEnabled(true);
                        btnEmailAuth.setText("Login");
                        Toast.makeText(this,
                                "Login failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        } else {
            // Register
            String name = etName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                etName.setError("Enter your name");
                btnEmailAuth.setEnabled(true);
                btnEmailAuth.setText("Create Account");
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        FirebaseUser user = result.getUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), name, email, "email");
                        }
                        Toast.makeText(this,
                                "Account created! Welcome 🌾", Toast.LENGTH_SHORT).show();
                        goToHome();
                    })
                    .addOnFailureListener(e -> {
                        btnEmailAuth.setEnabled(true);
                        btnEmailAuth.setText("Create Account");
                        Toast.makeText(this,
                                "Registration failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        }
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────
    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email first");
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v ->
                        Toast.makeText(this,
                                "Reset email sent! Check your inbox 📧",
                                Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    // ── GOOGLE SIGN-IN ────────────────────────────────────────
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            try {
                GoogleSignInAccount account =
                        GoogleSignIn.getSignedInAccountFromIntent(data)
                                .getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this,
                        "Google sign-in failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null && result.getAdditionalUserInfo() != null
                            && result.getAdditionalUserInfo().isNewUser()) {
                        saveUserToFirestore(
                                user.getUid(),
                                user.getDisplayName() != null ? user.getDisplayName() : "User",
                                user.getEmail() != null ? user.getEmail() : "",
                                "google");
                    }
                    Toast.makeText(this,
                            "Welcome " + (user != null ? user.getDisplayName() : "") + "! 🌾",
                            Toast.LENGTH_SHORT).show();
                    goToHome();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Auth failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    // ── PHONE OTP ─────────────────────────────────────────────
    private void showPhoneDialog() {
        View view = LayoutInflater.from(this)
                .inflate(android.R.layout.simple_list_item_1, null);
        EditText etPhone = new EditText(this);
        etPhone.setHint("+91XXXXXXXXXX");
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        etPhone.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("Phone Number")
                .setMessage("Enter your phone number with country code")
                .setView(etPhone)
                .setPositiveButton("Send OTP", (dialog, which) -> {
                    String phone = etPhone.getText().toString().trim();
                    if (!phone.startsWith("+")) {
                        Toast.makeText(this,
                                "Add country code e.g. +91XXXXXXXXXX",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendOTP(phone);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendOTP(String phoneNumber) {
        Toast.makeText(this, "Sending OTP...", Toast.LENGTH_SHORT).show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(
                            @NonNull PhoneAuthCredential credential) {
                        // Auto-verification (rare in India)
                        mAuth.signInWithCredential(credential)
                                .addOnSuccessListener(result -> {
                                    FirebaseUser user = result.getUser();
                                    if (user != null && result.getAdditionalUserInfo() != null
                                            && result.getAdditionalUserInfo().isNewUser()) {
                                        saveUserToFirestore(
                                                user.getUid(), "Farmer",
                                                phoneNumber, "phone");
                                    }
                                    goToHome();
                                });
                    }

                    @Override
                    public void onVerificationFailed(
                            @NonNull com.google.firebase.FirebaseException e) {
                        Toast.makeText(LoginActivity.this,
                                "OTP failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(
                            @NonNull String verId,
                            @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = verId;
                        showOTPDialog(phoneNumber);
                    }
                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void showOTPDialog(String phoneNumber) {
        EditText etOtp = new EditText(this);
        etOtp.setHint("Enter 6-digit OTP");
        etOtp.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etOtp.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("Verify OTP")
                .setMessage("OTP sent to " + phoneNumber)
                .setView(etOtp)
                .setPositiveButton("Verify", (dialog, which) -> {
                    String code = etOtp.getText().toString().trim();
                    if (code.length() != 6) {
                        Toast.makeText(this,
                                "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PhoneAuthCredential credential =
                            PhoneAuthProvider.getCredential(verificationId, code);
                    mAuth.signInWithCredential(credential)
                            .addOnSuccessListener(result -> {
                                FirebaseUser user = result.getUser();
                                if (user != null && result.getAdditionalUserInfo() != null
                                        && result.getAdditionalUserInfo().isNewUser()) {
                                    saveUserToFirestore(
                                            user.getUid(), "Farmer",
                                            phoneNumber, "phone");
                                }
                                Toast.makeText(this,
                                        "Phone verified! Welcome 🌾",
                                        Toast.LENGTH_SHORT).show();
                                goToHome();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Wrong OTP: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── SAVE USER TO FIRESTORE ────────────────────────────────
    private void saveUserToFirestore(String uid, String name,
                                     String contact, String method) {
        Map<String, Object> user = new HashMap<>();
        user.put("name",       name);
        user.put("contact",    contact);
        user.put("loginMethod", method);
        user.put("createdAt",
                new java.util.Date().toString());

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(v ->
                        android.util.Log.d("KrishiAI", "User saved to Firestore"))
                .addOnFailureListener(e ->
                        android.util.Log.e("KrishiAI", "Firestore error: " + e.getMessage()));
    }

    // ── GO TO HOME ────────────────────────────────────────────
    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}