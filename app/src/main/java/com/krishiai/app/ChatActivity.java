package com.krishiai.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private LinearLayout chatContainer;
    private EditText     messageInput;
    private ImageButton  sendButton;
    private ScrollView   scrollView;

    private String currentLanguage = "EN";

    private final List<JSONObject> chatHistory = new ArrayList<>();

    private static final String GEMINI_API_KEY =
            "AIzaSyB2baImrYwmM_vX_QHVayIDCKn4gmyj32A";

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" +
                    "gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatContainer = findViewById(R.id.chatContainer);
        messageInput  = findViewById(R.id.messageInput);
        sendButton    = findViewById(R.id.sendButton);
        scrollView    = findViewById(R.id.scrollView);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences(
                "KrishiAI_Prefs", MODE_PRIVATE);
        currentLanguage = prefs.getString("language", "EN");

        sendButton.setOnClickListener(v -> sendMessage());

        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });

        addBotMessage(getWelcomeMessage());
        addSuggestions();
    }

    // ── WELCOME MESSAGE ───────────────────────────────────────
    private String getWelcomeMessage() {
        switch (currentLanguage) {
            case "HI":
                return "नमस्ते! 🌾 मैं KrishiAI सहायक हूं।\n\n" +
                        "मैं आपकी मदद कर सकता हूं:\n" +
                        "• फसल रोग पहचान\n" +
                        "• उर्वरक और कीटनाशक\n" +
                        "• सरकारी योजनाएं\n" +
                        "• मौसम आधारित सलाह\n\n" +
                        "कोई भी सवाल पूछें!";
            case "MR":
                return "नमस्कार! 🌾 मी KrishiAI सहाय्यक आहे.\n\n" +
                        "मी मदत करू शकतो:\n" +
                        "• पीक रोग ओळख\n" +
                        "• खत आणि कीटकनाशके\n" +
                        "• सरकारी योजना\n" +
                        "• हवामान सल्ला\n\n" +
                        "कोणताही प्रश्न विचारा!";
            default:
                return "Hello! 🌾 I'm your KrishiAI farming assistant.\n\n" +
                        "I can help you with:\n" +
                        "• Crop disease identification\n" +
                        "• Fertilizer & pesticide advice\n" +
                        "• Government farming schemes\n" +
                        "• Weather-based crop tips\n\n" +
                        "Ask me anything!";
        }
    }

    // ── SUGGESTION CHIPS ──────────────────────────────────────
    private void addSuggestions() {
        String[] suggestions;
        switch (currentLanguage) {
            case "HI":
                suggestions = new String[]{
                        "टमाटर रोग के लक्षण",
                        "PM किसान योजना",
                        "नीम का तेल उपयोग"
                };
                break;
            case "MR":
                suggestions = new String[]{
                        "टोमॅटो रोगाची लक्षणे",
                        "PM किसान योजना",
                        "कांदा पीक काळजी"
                };
                break;
            default:
                suggestions = new String[]{
                        "Tomato disease symptoms",
                        "Best fertilizer for wheat",
                        "PM Kisan eligibility"
                };
        }

        // Wrap chips in horizontal ScrollView so they don't overflow
        android.widget.HorizontalScrollView hsv =
                new android.widget.HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams hsvParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        hsvParams.setMargins(8, 8, 8, 16);
        hsv.setLayoutParams(hsvParams);

        LinearLayout chipRow = new LinearLayout(this);
        chipRow.setOrientation(LinearLayout.HORIZONTAL);
        chipRow.setPadding(8, 4, 8, 4);
        chipRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        for (String suggestion : suggestions) {
            TextView chip = new TextView(this);
            chip.setText(suggestion);
            chip.setTextColor(0xFF2D6A4F);
            chip.setBackground(createChipBackground());
            chip.setPadding(28, 14, 28, 14);
            chip.setTextSize(12f);
            chip.setSingleLine(true);

            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            chipParams.setMarginEnd(10);
            chip.setLayoutParams(chipParams);

            chip.setOnClickListener(v -> {
                messageInput.setText(suggestion);
                sendMessage();
            });

            chipRow.addView(chip);
        }

        hsv.addView(chipRow);
        chatContainer.addView(hsv);
    }

    private android.graphics.drawable.GradientDrawable createChipBackground() {
        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xFFD8F3DC);
        bg.setCornerRadius(32f);
        bg.setStroke(1, 0xFF74C69D);
        return bg;
    }

    // ── SEND MESSAGE ──────────────────────────────────────────
    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) return;

        addUserMessage(message);
        messageInput.setText("");
        sendButton.setEnabled(false);

        addTypingIndicator();

        new Thread(() -> {
            String response = callGeminiAPI(message);
            runOnUiThread(() -> {
                removeTypingIndicator();
                sendButton.setEnabled(true);
                if (response != null) {
                    addBotMessage(response);
                    saveChatToFirestore(message, response);
                } else {
                    addBotMessage(getErrorMessage());
                }
            });
        }).start();
    }

    // ── GEMINI API CALL ───────────────────────────────────────
    private String callGeminiAPI(String userMessage) {
        try {
            // Add user message to history
            JSONObject userContent = new JSONObject();
            userContent.put("role", "user");
            JSONArray userParts = new JSONArray();
            userParts.put(new JSONObject().put("text", userMessage));
            userContent.put("parts", userParts);
            chatHistory.add(userContent);

            // Keep only last 10 messages for context
            while (chatHistory.size() > 10) chatHistory.remove(0);

            // Build request
            JSONObject payload = new JSONObject();

            // System instruction
            JSONObject systemInstruction = new JSONObject();
            JSONArray sysParts = new JSONArray();
            sysParts.put(new JSONObject().put("text", getSystemPrompt()));
            systemInstruction.put("parts", sysParts);
            payload.put("systemInstruction", systemInstruction);

            // Add full chat history as contents
            JSONArray contents = new JSONArray();
            for (JSONObject msg : chatHistory) {
                contents.put(msg);
            }
            payload.put("contents", contents);

            // Generation config
            JSONObject config = new JSONObject();
            config.put("temperature", 0.7);
            config.put("maxOutputTokens", 600);
            payload.put("generationConfig", config);

            URL url = new URL(GEMINI_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            OutputStream os = conn.getOutputStream();
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            os.close();

            int code = conn.getResponseCode();
            if (code == 200) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json     = new JSONObject(sb.toString());
                JSONArray  cands    = json.getJSONArray("candidates");
                JSONObject first    = cands.getJSONObject(0);
                JSONObject cont     = first.getJSONObject("content");
                JSONArray  parts    = cont.getJSONArray("parts");
                String     response = parts.getJSONObject(0).getString("text");

                // Add bot response to history
                JSONObject botContent = new JSONObject();
                botContent.put("role", "model");
                JSONArray botParts = new JSONArray();
                botParts.put(new JSONObject().put("text", response));
                botContent.put("parts", botParts);
                chatHistory.add(botContent);

                return response;
            } else {
                android.util.Log.e("KrishiAI",
                        "Gemini error code: " + code);
            }

        } catch (Exception e) {
            android.util.Log.e("KrishiAI", "Chat error: " + e.getMessage());
        }
        return null;
    }

    // ── SYSTEM PROMPT ─────────────────────────────────────────
    private String getSystemPrompt() {
        String base =
                "You are KrishiAI, an expert agricultural assistant for Indian farmers. " +
                        "You specialize in: crop disease diagnosis, organic and chemical treatments, " +
                        "fertilizer recommendations, pest control, irrigation advice, government " +
                        "farming schemes (PM Kisan, Fasal Bima Yojana, etc.), and seasonal crop " +
                        "calendars for Maharashtra and India. " +
                        "Keep answers concise, practical, and farmer-friendly. " +
                        "Use simple language. Format with bullet points when listing items. " +
                        "Always mention safety precautions when recommending chemicals.";

        if (currentLanguage.equals("HI"))
            return base + " Reply ONLY in Hindi.";
        if (currentLanguage.equals("MR"))
            return base + " Reply ONLY in Marathi.";
        return base + " Reply in English.";
    }

    // ── SAVE CHAT TO FIRESTORE ────────────────────────────────
    private void saveChatToFirestore(String userMsg, String botReply) {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return;

            Map<String, Object> chat = new HashMap<>();
            chat.put("userId",    user.getUid());
            chat.put("userMsg",   userMsg);
            chat.put("botReply",  botReply);
            chat.put("language",  currentLanguage);
            chat.put("timestamp", new java.util.Date().toString());

            FirebaseFirestore.getInstance()
                    .collection("chats")
                    .add(chat)
                    .addOnFailureListener(e ->
                            android.util.Log.e("KrishiAI",
                                    "Chat save error: " + e.getMessage()));
        } catch (Exception e) {
            android.util.Log.e("KrishiAI",
                    "Firestore chat error: " + e.getMessage());
        }
    }

    // ── UI HELPERS ────────────────────────────────────────────
    private void addUserMessage(String message) {
        View view = getLayoutInflater().inflate(
                R.layout.item_chat_message, chatContainer, false);

        CardView card  = view.findViewById(R.id.messageCard);
        TextView text  = view.findViewById(R.id.messageText);
        TextView label = view.findViewById(R.id.messageLabel);

        text.setText(message);
        label.setText("You");
        label.setTextColor(0xFF2D6A4F);
        card.setCardBackgroundColor(0xFFD8F3DC);

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) view.getLayoutParams();
        params.gravity = android.view.Gravity.END;
        view.setLayoutParams(params);

        chatContainer.addView(view);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        View view = getLayoutInflater().inflate(
                R.layout.item_chat_message, chatContainer, false);

        CardView card  = view.findViewById(R.id.messageCard);
        TextView text  = view.findViewById(R.id.messageText);
        TextView label = view.findViewById(R.id.messageLabel);

        text.setText(message);
        label.setText("🌾 KrishiAI");
        label.setTextColor(0xFF1B4332);
        card.setCardBackgroundColor(0xFFFFFFFF);

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) view.getLayoutParams();
        params.gravity = android.view.Gravity.START;
        view.setLayoutParams(params);

        chatContainer.addView(view);
        scrollToBottom();
    }

    private void addTypingIndicator() {
        View view = getLayoutInflater().inflate(
                R.layout.item_chat_message, chatContainer, false);
        view.setTag("typing");

        CardView card  = view.findViewById(R.id.messageCard);
        TextView text  = view.findViewById(R.id.messageText);
        TextView label = view.findViewById(R.id.messageLabel);

        text.setText("Thinking... 🌿");
        label.setText("🌾 KrishiAI");
        card.setCardBackgroundColor(0xFFFFFFFF);

        chatContainer.addView(view);
        scrollToBottom();
    }

    private void removeTypingIndicator() {
        for (int i = 0; i < chatContainer.getChildCount(); i++) {
            View v = chatContainer.getChildAt(i);
            if ("typing".equals(v.getTag())) {
                chatContainer.removeView(v);
                break;
            }
        }
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private String getErrorMessage() {
        switch (currentLanguage) {
            case "HI": return "❌ उत्तर प्राप्त करने में समस्या हुई। इंटरनेट जांचें।";
            case "MR": return "❌ उत्तर मिळण्यात समस्या आली. इंटरनेट तपासा.";
            default:   return "❌ Could not get response. Check your internet.";
        }
    }
}