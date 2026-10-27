package com.krishiai.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import android.view.View;

public class ResultActivity extends AppCompatActivity {

    private String diseaseName;
    private float  confidence;
    private String plantName;
    private String chemicalCure;
    private String organicCure;
    private String prevention;

    private TextView    tvDiseaseName, tvPlantName, tvConfidence;
    private TextView    tvChemicalCure, tvOrganicCure, tvPrevention;
    private ProgressBar progressBar;
    private Button      btnEN, btnHI, btnMR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        diseaseName  = getIntent().getStringExtra("disease_name");
        confidence   = getIntent().getFloatExtra("confidence", 0f);
        plantName    = getIntent().getStringExtra("plant_name");
        chemicalCure = getIntent().getStringExtra("chemical_cure");
        organicCure  = getIntent().getStringExtra("organic_cure");
        prevention   = getIntent().getStringExtra("prevention");

        tvDiseaseName  = findViewById(R.id.tvDiseaseName);
        tvPlantName    = findViewById(R.id.tvPlantName);
        tvConfidence   = findViewById(R.id.tvConfidence);
        tvChemicalCure = findViewById(R.id.tvChemicalCure);
        tvOrganicCure  = findViewById(R.id.tvOrganicCure);
        tvPrevention   = findViewById(R.id.tvPrevention);
        progressBar    = findViewById(R.id.progressConfidence);
        btnEN          = findViewById(R.id.btnEN);
        btnHI          = findViewById(R.id.btnHI);
        btnMR          = findViewById(R.id.btnMR);

        String savedLang = getSharedPreferences("KrishiAI_Prefs", MODE_PRIVATE)
                .getString("language", "EN");
        displayResult(savedLang);
        updateButtonStyles(savedLang);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnFeedback).setOnClickListener(v -> {
            Intent intent = new Intent(this, FeedbackActivity.class);
            intent.putExtra("disease_name", diseaseName);
            startActivity(intent);
        });

        findViewById(R.id.btnScanAgain).setOnClickListener(v -> {
            Intent intent = new Intent(this, ScanActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnEN.setOnClickListener(v -> {
            saveLanguage("EN");
            displayResult("EN");
            updateButtonStyles("EN");
        });

        btnHI.setOnClickListener(v -> {
            saveLanguage("HI");
            displayResult("HI");
            updateButtonStyles("HI");
        });

        btnMR.setOnClickListener(v -> {
            saveLanguage("MR");
            displayResult("MR");
            updateButtonStyles("MR");
        });
    }

    // ── DISPLAY RESULT IN CHOSEN LANGUAGE ────────────────────
    private void displayResult(String lang) {

        // Safety checks
        if (diseaseName  == null) diseaseName  = "Unknown";
        if (plantName    == null) plantName    = "Unknown";
        if (chemicalCure == null) chemicalCure = "";
        if (organicCure  == null) organicCure  = "";
        if (prevention   == null) prevention   = "";

        // Confidence bar never changes
        int confPct = (int)(confidence * 100);
        tvConfidence.setText(confPct + "%");
        progressBar.setProgress(confPct);

        // Color based on result type
        if (diseaseName.contains("Not Recognized")) {
            tvDiseaseName.setTextColor(0xFFE53935);
        } else if (diseaseName.contains("Healthy") || diseaseName.contains("✅")) {
            tvDiseaseName.setTextColor(0xFF74C69D);
        } else {
            tvDiseaseName.setTextColor(0xFFFFFFFF);
        }

        switch (lang) {
            case "HI":
                tvDiseaseName.setText(translateDiseaseHindi(diseaseName));
                tvPlantName.setText(translatePlantHindi(plantName));
                tvChemicalCure.setText(translateChemicalHindi(diseaseName));
                tvOrganicCure.setText(translateOrganicHindi(diseaseName));
                tvPrevention.setText(translatePreventionHindi(diseaseName));
                break;

            case "MR":
                tvDiseaseName.setText(translateDiseaseMarathi(diseaseName));
                tvPlantName.setText(translatePlantMarathi(plantName));
                tvChemicalCure.setText(translateChemicalMarathi(diseaseName));
                tvOrganicCure.setText(translateOrganicMarathi(diseaseName));
                tvPrevention.setText(translatePreventionMarathi(diseaseName));
                break;

            default:
                tvDiseaseName.setText(diseaseName);
                tvPlantName.setText(plantName);
                tvChemicalCure.setText(chemicalCure);
                tvOrganicCure.setText(organicCure);
                tvPrevention.setText(prevention);
                break;
        }

        // Load treatment product images
        ImageView ivChem    = findViewById(R.id.ivChemicalProduct);
        ImageView ivOrganic = findViewById(R.id.ivOrganicProduct);

        String chemImg    = getTreatmentImageUrl(diseaseName, "chemical");
        String organicImg = getTreatmentImageUrl(diseaseName, "organic");

        if (ivChem != null && chemImg != null) {
            ivChem.setVisibility(View.VISIBLE);
            Glide.with(this).load(chemImg).centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivChem);
        }
        if (ivOrganic != null && organicImg != null) {
            ivOrganic.setVisibility(View.VISIBLE);
            Glide.with(this).load(organicImg).centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivOrganic);
        }
    }

    // ── HINDI TRANSLATIONS ────────────────────────────────────

    private String translateDiseaseHindi(String d) {
        if (d == null) return "अज्ञात";
        if (d.contains("Not Recognized"))  return "पौधा पहचाना नहीं गया";
        if (d.contains("Late Blight"))     return "उशिरा करपा रोग";
        if (d.contains("Early Blight"))    return "अर्ली ब्लाइट";
        if (d.contains("Bacterial"))       return "बैक्टीरियल स्पॉट";
        if (d.contains("Healthy") || d.contains("✅")) return "स्वस्थ पौधा ✅";
        if (d.contains("Rust"))            return "गेरुआ रोग";
        if (d.contains("Powdery") || d.contains("Mildew")) return "चूर्णिल आसिता";
        if (d.contains("Septoria"))        return "सेप्टोरिया पत्ती धब्बा";
        if (d.contains("Mold"))            return "पत्ती फफूंदी";
        if (d.contains("Scab"))            return "पपड़ी रोग";
        if (d.contains("Spot"))            return "पत्ती धब्बा";
        if (d.contains("Mosaic"))          return "मोज़ेक वायरस";
        if (d.contains("Curl"))            return "पत्ती मुड़न वायरस";
        if (d.contains("Spider"))          return "मकड़ी माइट";
        if (d.contains("Target"))         return "टार्गेट स्पॉट";
        if (d.contains("Greening"))        return "सिट्रस ग्रीनिंग";
        if (d.contains("Black rot") || d.contains("Black Rot")) return "काली सड़न";
        return d;
    }

    private String translatePlantHindi(String p) {
        if (p == null) return "अज्ञात";
        if (p.contains("Tomato"))      return "टमाटर";
        if (p.contains("Pepper"))      return "मिर्च";
        if (p.contains("Corn"))        return "मक्का";
        if (p.contains("Potato"))      return "आलू";
        if (p.contains("Apple"))       return "सेब";
        if (p.contains("Grape"))       return "अंगूर";
        if (p.contains("Strawberry"))  return "स्ट्रॉबेरी";
        if (p.contains("Peach"))       return "आड़ू";
        if (p.contains("Cherry"))      return "चेरी";
        if (p.contains("Orange"))      return "संतरा";
        if (p.contains("Blueberry"))   return "ब्लूबेरी";
        if (p.contains("Soybean"))     return "सोयाबीन";
        if (p.contains("Raspberry"))   return "रसभरी";
        if (p.contains("Squash"))      return "कद्दू";
        if (p.contains("Unknown"))     return "अज्ञात";
        return p;
    }

    private String translateChemicalHindi(String d) {
        if (d == null) return "";
        if (d.contains("Not Recognized"))
            return "❌ यह छवि पहचानी नहीं गई।\n\nसमर्थित फसलें:\nटमाटर, मिर्च, मक्का, आलू, सेब, अंगूर, स्ट्रॉबेरी, आड़ू, चेरी, संतरा, ब्लूबेरी, सोयाबीन, रसभरी, कद्दू";
        if (d.contains("Late Blight"))
            return "• मैनकोज़ेब 75% WP @ 2g/L पानी में छिड़कें\n• मेटालैक्सिल + मैनकोज़ेब (रिडोमिल गोल्ड) लगाएं\n• गीले मौसम में हर 7 दिन में दोहराएं";
        if (d.contains("Early Blight"))
            return "• क्लोरोथालोनिल 75% WP @ 2g/L छिड़कें\n• आइप्रोडियोन @ 1.5g/L लगाएं\n• 7-10 दिन के अंतराल पर छिड़काव करें";
        if (d.contains("Bacterial"))
            return "• कॉपर ऑक्सीक्लोराइड 50% WP @ 3g/L छिड़कें\n• स्ट्रेप्टोमाइसिन @ 0.5g/L लगाएं\n• हर 5-7 दिन में छिड़काव करें";
        if (d.contains("Healthy") || d.contains("✅"))
            return "कोई उपचार नहीं — आपका पौधा स्वस्थ है! ✅";
        if (d.contains("Septoria"))
            return "• मैनकोज़ेब 75% WP @ 2g/L छिड़कें\n• क्लोरोथालोनिल @ 2g/L लगाएं\n• हर 7-10 दिन में छिड़काव करें";
        if (d.contains("Rust"))
            return "• प्रोपिकोनाज़ोल 25% EC @ 1ml/L छिड़कें\n• मैनकोज़ेब 75% WP @ 2g/L लगाएं\n• 10 दिन के अंतराल पर दो छिड़काव करें";
        return chemicalCure;
    }

    private String translateOrganicHindi(String d) {
        if (d == null) return "";
        if (d.contains("Not Recognized"))
            return "📸 बेहतर परिणाम के लिए:\n\n• अच्छी रोशनी में फोटो लें\n• पत्ती को फ्रेम में भरें\n• धुंधली फोटो से बचें\n• प्रभावित पत्ती की कोशिश करें";
        if (d.contains("Late Blight"))
            return "• बोर्डो मिश्रण (कॉपर सल्फेट) छिड़कें\n• नीम का तेल @ 3ml/L हर 10 दिन में लगाएं\n• संक्रमित पौधे के हिस्से हटाएं और जलाएं";
        if (d.contains("Early Blight"))
            return "• ट्राइकोडर्मा विराइड मिट्टी में डालें\n• पतला गोमूत्र (1:20) साप्ताहिक छिड़कें\n• लहसुन-मिर्च का अर्क स्प्रे करें";
        if (d.contains("Bacterial"))
            return "• स्यूडोमोनास फ्लोरेसेंस @ 5g/L लगाएं\n• बोर्डो मिश्रण 1% छिड़कें\n• संक्रमित सामग्री तुरंत हटाएं";
        if (d.contains("Healthy") || d.contains("✅"))
            return "नियमित देखभाल और निगरानी जारी रखें।";
        if (d.contains("Septoria"))
            return "• संक्रमित पत्तियां तुरंत हटाएं\n• कॉपर-आधारित कवकनाशी लगाएं\n• नीम का तेल @ 3ml/L छिड़कें";
        return organicCure;
    }

    private String translatePreventionHindi(String d) {
        if (d == null) return "";
        if (d.contains("Not Recognized"))
            return "📞 सहायता के लिए:\n\n• कृषि विज्ञान केंद्र (KVK)\n• राज्य कृषि विभाग\n• किसान कॉल सेंटर: 1800-180-1551";
        if (d.contains("Late Blight"))
            return "• ऊपर से सिंचाई से बचें\n• पौधों के बीच 45-60 सेमी की दूरी रखें\n• रोग-प्रतिरोधी किस्में उपयोग करें\n• हर मौसम में फसल बदलें";
        if (d.contains("Early Blight"))
            return "• वायु संचार के लिए पौधों को सहारा दें\n• संक्रमित निचली पत्तियां तुरंत हटाएं\n• सिंचाई करते समय पत्तियों को गीला न करें";
        if (d.contains("Bacterial"))
            return "• प्रमाणित रोग-मुक्त बीजों का उपयोग करें\n• गीले खेतों में काम करने से बचें\n• पौधों के बीच उपकरण कीटाणुरहित करें";
        if (d.contains("Healthy") || d.contains("✅"))
            return "• रोग के संकेतों के लिए साप्ताहिक निगरानी करें\n• उचित पोषण बनाए रखें\n• अच्छा वायु संचार सुनिश्चित करें";
        if (d.contains("Septoria"))
            return "• ऊपर से पानी देने से बचें\n• पौधों के आधार पर मल्च डालें\n• हर साल टमाटर की फसल बदलें";
        return prevention;
    }

    // ── MARATHI TRANSLATIONS ──────────────────────────────────

    private String translateDiseaseMarathi(String d) {
        if (d == null) return "अज्ञात";
        if (d.contains("Not Recognized"))  return "वनस्पती ओळखली नाही";
        if (d.contains("Late Blight"))     return "उशिरा करपा";
        if (d.contains("Early Blight"))    return "लवकर करपा";
        if (d.contains("Bacterial"))       return "जिवाणू ठिपके";
        if (d.contains("Healthy") || d.contains("✅")) return "निरोगी वनस्पती ✅";
        if (d.contains("Rust"))            return "गंज रोग";
        if (d.contains("Powdery") || d.contains("Mildew")) return "भुरी बुरशी";
        if (d.contains("Septoria"))        return "सेप्टोरिया पान ठिपके";
        if (d.contains("Mold"))            return "पानावरील बुरशी";
        if (d.contains("Scab"))            return "खरूज रोग";
        if (d.contains("Spot"))            return "पानावरील ठिपके";
        if (d.contains("Mosaic"))          return "मोझॅक व्हायरस";
        if (d.contains("Curl"))            return "पान गुंडाळणे व्हायरस";
        if (d.contains("Spider"))          return "कोळी माइट";
        if (d.contains("Target"))          return "टार्गेट स्पॉट";
        if (d.contains("Greening"))        return "लिंबूवर्गीय हरितीकरण";
        if (d.contains("Black rot") || d.contains("Black Rot")) return "काळी कूज";
        return d;
    }

    private String translatePlantMarathi(String p) {
        if (p == null) return "अज्ञात";
        if (p.contains("Tomato"))      return "टोमॅटो";
        if (p.contains("Pepper"))      return "मिरची";
        if (p.contains("Corn"))        return "मका";
        if (p.contains("Potato"))      return "बटाटा";
        if (p.contains("Apple"))       return "सफरचंद";
        if (p.contains("Grape"))       return "द्राक्षे";
        if (p.contains("Strawberry"))  return "स्ट्रॉबेरी";
        if (p.contains("Peach"))       return "पीच";
        if (p.contains("Cherry"))      return "चेरी";
        if (p.contains("Orange"))      return "संत्री";
        if (p.contains("Blueberry"))   return "ब्लूबेरी";
        if (p.contains("Soybean"))     return "सोयाबीन";
        if (p.contains("Raspberry"))   return "रासबेरी";
        if (p.contains("Squash"))      return "भोपळा";
        if (p.contains("Unknown"))     return "अज्ञात";
        return p;
    }

    private String translateChemicalMarathi(String d) {
        if (d == null) return "";
        if (d.contains("Not Recognized"))
            return "❌ ही प्रतिमा ओळखली नाही.\n\nसमर्थित पिके:\nटोमॅटो, मिरची, मका, बटाटा, सफरचंद, द्राक्षे, स्ट्रॉबेरी, पीच, चेरी, संत्री, ब्लूबेरी, सोयाबीन, रासबेरी, भोपळा";
        if (d.contains("Late Blight"))
            return "• मॅन्कोझेब 75% WP @ 2g/L पाण्यात फवारा\n• मेटॅलॅक्सिल + मॅन्कोझेब (रिडोमिल गोल्ड) लावा\n• पावसाळ्यात दर 7 दिवसांनी पुन्हा करा";
        if (d.contains("Early Blight"))
            return "• क्लोरोथालोनिल 75% WP @ 2g/L फवारा\n• आयप्रोडियोन @ 1.5g/L लावा\n• 7-10 दिवसांच्या अंतराने फवारणी करा";
        if (d.contains("Bacterial"))
            return "• कॉपर ऑक्सिक्लोराईड 50% WP @ 3g/L फवारा\n• स्ट्रेप्टोमायसिन @ 0.5g/L लावा\n• दर 5-7 दिवसांनी फवारणी करा";
        if (d.contains("Healthy") || d.contains("✅"))
            return "उपचाराची गरज नाही — वनस्पती निरोगी आहे! ✅";
        if (d.contains("Septoria"))
            return "• मॅन्कोझेब 75% WP @ 2g/L फवारा\n• क्लोरोथालोनिल @ 2g/L लावा\n• दर 7-10 दिवसांनी फवारणी करा";
        if (d.contains("Rust"))
            return "• प्रोपिकोनाझोल 25% EC @ 1ml/L फवारा\n• मॅन्कोझेब 75% WP @ 2g/L लावा\n• 10 दिवसांच्या अंतराने दोन फवारण्या करा";
        return chemicalCure;
    }

    private String translateOrganicMarathi(String d) {
        if (d == null) return "";
        if (d.contains("Not Recognized"))
            return "📸 चांगल्या परिणामासाठी:\n\n• चांगल्या प्रकाशात फोटो घ्या\n• पान फ्रेममध्ये भरा\n• अस्पष्ट फोटो टाळा\n• प्रभावित पानाचा प्रयत्न करा";
        if (d.contains("Late Blight"))
            return "• बोर्डो मिश्रण (कॉपर सल्फेट) फवारा\n• निंबाचे तेल @ 3ml/L दर 10 दिवसांनी लावा\n• संक्रमित वनस्पतींचे भाग काढून जाळा";
        if (d.contains("Early Blight"))
            return "• ट्रायकोडर्मा व्हिरिडे माती मध्ये घाला\n• पातळ गोमूत्र (1:20) आठवड्यातून फवारा\n• लसूण-मिरची अर्क फवारा";
        if (d.contains("Bacterial"))
            return "• स्यूडोमोनास फ्लुओरेसेन्स @ 5g/L लावा\n• बोर्डो मिश्रण 1% फवारा\n• संक्रमित साहित्य लगेच काढा";
        if (d.contains("Healthy") || d.contains("✅"))
            return "नियमित काळजी आणि देखरेख सुरू ठेवा.";
        if (d.contains("Septoria"))
            return "• संक्रमित पाने लगेच काढा\n• तांबे-आधारित बुरशीनाशक लावा\n• निंबाचे तेल @ 3ml/L फवारा";
        return organicCure;
    }

    private String translatePreventionMarathi(String d) {
        if (d == null) return "";
        if (d.contains("Not Recognized"))
            return "📞 मदतीसाठी:\n\n• कृषी विज्ञान केंद्र (KVK)\n• राज्य कृषी विभाग\n• किसान कॉल सेंटर: 1800-180-1551";
        if (d.contains("Late Blight"))
            return "• वरून सिंचन टाळा\n• झाडांमध्ये 45-60 सेमी अंतर ठेवा\n• रोग-प्रतिरोधक वाण वापरा\n• दर हंगामात पीक बदला";
        if (d.contains("Early Blight"))
            return "• हवा खेळती राहण्यासाठी झाडांना आधार द्या\n• संक्रमित खालची पाने लगेच काढा\n• सिंचन करताना पाने ओली होऊ देऊ नका";
        if (d.contains("Bacterial"))
            return "• प्रमाणित रोगमुक्त बियाणे वापरा\n• ओल्या शेतात काम टाळा\n• झाडांमध्ये साधने निर्जंतुक करा";
        if (d.contains("Healthy") || d.contains("✅"))
            return "• रोगाच्या लक्षणांसाठी आठवड्यातून निरीक्षण करा\n• योग्य पोषण राखा\n• चांगले हवा खेळणे सुनिश्चित करा";
        if (d.contains("Septoria"))
            return "• वरून पाणी देणे टाळा\n• झाडांच्या पायाशी आच्छादन घाला\n• दर वर्षी टोमॅटो पीक बदला";
        return prevention;
    }

    // ── GET TREATMENT IMAGE URL ───────────────────────────────
    private String getTreatmentImageUrl(String disease, String type) {
        if (disease == null) return null;

        if (type.equals("chemical")) {
            if (disease.contains("Late Blight"))
                return "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/320px-Camponotus_flavomarginatus_ant.jpg";
            if (disease.contains("Early Blight"))
                return "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400";
            if (disease.contains("Bacterial"))
                return "https://images.unsplash.com/photo-1464226184884-fa280b87c399?w=400";
            if (disease.contains("Septoria"))
                return "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400";
            if (disease.contains("Healthy"))
                return "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400";
            // Default — Mancozeb spray image
            return "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400";
        } else {
            // Organic
            if (disease.contains("Late Blight") || disease.contains("Early Blight"))
                return "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400";
            // Default neem image
            return "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Azadirachta_indica.jpg/320px-Azadirachta_indica.jpg";
        }
    }

    // ── BUTTON STYLES ─────────────────────────────────────────
    private void updateButtonStyles(String activeLang) {
        btnEN.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF40916C));
        btnHI.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF40916C));
        btnMR.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF40916C));
        btnEN.setTextColor(0xFFFFFFFF);
        btnHI.setTextColor(0xFFFFFFFF);
        btnMR.setTextColor(0xFFFFFFFF);

        switch (activeLang) {
            case "EN":
                btnEN.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
                btnEN.setTextColor(0xFF1B4332);
                break;
            case "HI":
                btnHI.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
                btnHI.setTextColor(0xFF1B4332);
                break;
            case "MR":
                btnMR.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
                btnMR.setTextColor(0xFF1B4332);
                break;
        }
    }

    // ── SAVE LANGUAGE ─────────────────────────────────────────
    private void saveLanguage(String lang) {
        getSharedPreferences("KrishiAI_Prefs", MODE_PRIVATE)
                .edit().putString("language", lang).apply();
    }
}