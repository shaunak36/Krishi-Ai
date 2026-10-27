package com.krishiai.app;

import android.content.Context;
import android.graphics.Bitmap;
import org.tensorflow.lite.Interpreter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DiseaseClassifier {

    private Interpreter  interpreter;
    private List<String> labels;
    private static final int   INPUT_SIZE     = 224;
    private static final float MIN_CONFIDENCE = 0.40f;

    public static class Result {
        public String  label;
        public String  displayName;
        public float   confidence;
        public String  plantName;
        public String  chemicalCure;
        public String  organicCure;
        public String  prevention;
        public boolean isUnknown = false;
    }

    public DiseaseClassifier(Context context) throws Exception {
        interpreter = new Interpreter(loadModel(context));
        labels      = loadLabels(context);
        android.util.Log.d("KrishiAI",
                "Model loaded with " + labels.size() + " classes");
        for (int i = 0; i < labels.size(); i++) {
            android.util.Log.d("KrishiAI", "Label " + i + ": " + labels.get(i));
        }
    }

    // ─────────────────────────────────────────────────────────
    // MAIN CLASSIFY
    // ─────────────────────────────────────────────────────────
    public Result classify(Bitmap bitmap) {

        // Step 1 — Leaf check
        if (!isLikelyLeaf(bitmap)) {
            return buildUnknownResult(0f, "no_leaf");
        }

        // Step 2 — Preprocess + run model
        Bitmap     processed = preprocessImage(bitmap);
        ByteBuffer buffer    = convertToByteBuffer(processed);
        float[][]  output    = new float[1][labels.size()];
        interpreter.run(buffer, output);
        float[] probs = output[0];

        // Step 3 — Find top prediction
        int   topIndex = 0;
        float topConf  = 0f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > topConf) {
                topConf  = probs[i];
                topIndex = i;
            }
        }

        String rawLabel = labels.size() > topIndex
                ? labels.get(topIndex) : "Unknown";

        android.util.Log.d("KrishiAI",
                "TOP: " + rawLabel + " Conf=" +
                        String.format("%.3f", topConf));

        // Log all probabilities above 5%
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > 0.05f) {
                android.util.Log.d("KrishiAI",
                        "  " + labels.get(i) + " = " +
                                String.format("%.1f%%", probs[i] * 100));
            }
        }

        if (topConf < MIN_CONFIDENCE) {
            return buildUnknownResult(topConf, "low_confidence");
        }

        return buildResult(rawLabel, topConf);
    }

    // ─────────────────────────────────────────────────────────
    // PREPROCESS IMAGE
    // ─────────────────────────────────────────────────────────
    private Bitmap preprocessImage(Bitmap original) {
        int w = original.getWidth();
        int h = original.getHeight();

        // Center crop
        int cropSize = (int)(Math.min(w, h) * 0.85f);
        int cropX    = (w - cropSize) / 2;
        int cropY    = (int)((h - cropSize) * 0.4f);
        cropX    = Math.max(0, cropX);
        cropY    = Math.max(0, cropY);
        cropSize = Math.min(cropSize, Math.min(w - cropX, h - cropY));

        Bitmap cropped = Bitmap.createBitmap(
                original, cropX, cropY, cropSize, cropSize);

        // Resize to 224x224
        return Bitmap.createScaledBitmap(
                cropped, INPUT_SIZE, INPUT_SIZE, true);
    }

    // ─────────────────────────────────────────────────────────
    // LEAF CHECK
    // ─────────────────────────────────────────────────────────
    private boolean isLikelyLeaf(Bitmap bitmap) {
        Bitmap small = Bitmap.createScaledBitmap(bitmap, 100, 100, false);

        int total       = 0;
        int plantPixels = 0;
        int skinPixels  = 0;
        int grayPixels  = 0;
        int whitePixels = 0;
        int bluePixels  = 0;
        int redPixels   = 0;

        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                int pixel = small.getPixel(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >>  8) & 0xFF;
                int b =  pixel        & 0xFF;
                total++;

                // Plant colors
                boolean isGreen =
                        g > r + 20 && g > b + 15 && g > 35;
                boolean isYellowGreen =
                        g > 90 && r > 60 && b < 110
                                && g > b + 15 && r < g + 55;
                boolean isBrownGreen =
                        r > 45 && g > 35 && b < 95
                                && Math.abs(r - g) < 75
                                && b < r - 5 && g > 30;
                boolean isDark =
                        r < 70 && g < 70 && b < 70
                                && g >= r - 8 && g > b - 5;

                if (isGreen || isYellowGreen || isBrownGreen || isDark)
                    plantPixels++;

                // Skin
                boolean isLightSkin =
                        r > 155 && g > 95 && b > 65
                                && r > g && g > b
                                && r - b > 40 && r - g < 85;
                boolean isMediumSkin =
                        r > 95 && g > 55 && b > 35
                                && r > g && g > b
                                && r - b > 22 && r < 215
                                && (float) r / Math.max(g, 1) > 1.12f;
                boolean isDarkSkin =
                        r > 55 && g > 30 && b > 15
                                && r > g && g > b
                                && r - b > 12 && r < 155
                                && (float) r / Math.max(g, 1) > 1.08f;
                if (isLightSkin || isMediumSkin || isDarkSkin)
                    skinPixels++;

                // Gray
                int maxC = Math.max(r, Math.max(g, b));
                int minC = Math.min(r, Math.min(g, b));
                if (maxC - minC < 22 && maxC > 45 && maxC < 225)
                    grayPixels++;

                // White
                if (r > 225 && g > 225 && b > 225) whitePixels++;

                // Blue
                if (b > r + 25 && b > g + 15 && b > 75) bluePixels++;

                // Red/orange
                if (r > g + 40 && r > b + 40 && r > 100) redPixels++;
            }
        }

        float plantRatio = (float) plantPixels / total;
        float skinRatio  = (float) skinPixels  / total;
        float grayRatio  = (float) grayPixels  / total;
        float whiteRatio = (float) whitePixels / total;
        float blueRatio  = (float) bluePixels  / total;
        float redRatio   = (float) redPixels   / total;

        android.util.Log.d("KrishiAI",
                "Leaf plant=" + String.format("%.2f", plantRatio) +
                        " skin="  + String.format("%.2f", skinRatio)  +
                        " gray="  + String.format("%.2f", grayRatio)  +
                        " blue="  + String.format("%.2f", blueRatio)  +
                        " red="   + String.format("%.2f", redRatio));

        if (skinRatio  > 0.15f) { android.util.Log.d("KrishiAI","REJECTED: skin");  return false; }
        if (grayRatio  > 0.55f) { android.util.Log.d("KrishiAI","REJECTED: gray");  return false; }
        if (whiteRatio > 0.50f) { android.util.Log.d("KrishiAI","REJECTED: white"); return false; }
        if (blueRatio  > 0.30f) { android.util.Log.d("KrishiAI","REJECTED: blue");  return false; }
        if (redRatio   > 0.25f) { android.util.Log.d("KrishiAI","REJECTED: red");   return false; }
        if (plantRatio < 0.12f) { android.util.Log.d("KrishiAI","REJECTED: plant"); return false; }

        float nonPlant = skinRatio + blueRatio + redRatio + grayRatio;
        if (nonPlant > 0.45f && nonPlant > plantRatio) {
            android.util.Log.d("KrishiAI", "REJECTED: nonPlant=" + nonPlant);
            return false;
        }

        android.util.Log.d("KrishiAI", "ACCEPTED plant=" + plantRatio);
        return true;
    }

    // ─────────────────────────────────────────────────────────
    // BUILD RESULT — matches YOUR labels.json exactly
    // ─────────────────────────────────────────────────────────
    private Result buildResult(String rawLabel, float confidence) {
        Result r     = new Result();
        r.label      = rawLabel;
        r.confidence = confidence;
        r.isUnknown  = false;

        switch (rawLabel) {

            // ── PEPPER ────────────────────────────────────────
            case "Pepper__bell___Bacterial_spot":
                r.displayName  = "Bell Pepper Bacterial Spot";
                r.plantName    = "Bell Pepper";
                r.chemicalCure = "• Spray Copper Oxychloride 50% WP @ 3g/L\n• Apply Streptomycin @ 0.5g/L\n• Spray every 5-7 days";
                r.organicCure  = "• Apply Pseudomonas fluorescens @ 5g/L\n• Spray Bordeaux mixture 1%\n• Remove infected material immediately";
                r.prevention   = "• Use certified disease-free seeds\n• Avoid working in wet fields\n• Disinfect tools between plants";
                break;

            case "Pepper__bell___healthy":
                r.displayName  = "Healthy Bell Pepper ✅";
                r.plantName    = "Bell Pepper";
                r.chemicalCure = "No treatment needed — pepper is healthy! ✅";
                r.organicCure  = "Continue regular crop management.";
                r.prevention   = "• Monitor for bacterial spot\n• Ensure proper drainage\n• Maintain balanced fertilization";
                break;

            // ── POTATO ────────────────────────────────────────
            case "Potato___Early_blight":
                r.displayName  = "Potato Early Blight";
                r.plantName    = "Potato";
                r.chemicalCure = "• Spray Chlorothalonil 75% WP @ 2g/L\n• Apply Mancozeb 75% WP @ 2g/L\n• Spray at 7-10 day intervals";
                r.organicCure  = "• Apply Trichoderma viride to soil\n• Spray Copper Oxychloride 0.3%\n• Remove infected lower leaves";
                r.prevention   = "• Use certified disease-free seed potatoes\n• Rotate crops yearly\n• Maintain proper plant spacing";
                break;

            case "Potato___Late_blight":
                r.displayName  = "Potato Late Blight";
                r.plantName    = "Potato";
                r.chemicalCure = "• Spray Mancozeb 75% WP @ 2g/L\n• Apply Metalaxyl + Mancozeb (Ridomil Gold)\n• Repeat every 5-7 days in wet season";
                r.organicCure  = "• Spray Bordeaux Mixture (Copper Sulphate)\n• Apply Neem oil @ 3ml/L every 10 days\n• Remove and destroy infected plants";
                r.prevention   = "• Use certified blight-resistant varieties\n• Avoid overhead irrigation\n• Hill up soil around plants";
                break;

            case "Potato___healthy":
                r.displayName  = "Healthy Potato ✅";
                r.plantName    = "Potato";
                r.chemicalCure = "No treatment needed — potato is healthy! ✅";
                r.organicCure  = "Continue regular crop management.";
                r.prevention   = "• Monitor for late blight in wet weather\n• Use certified seed potatoes\n• Ensure proper drainage";
                break;

            // ── TOMATO ────────────────────────────────────────
            case "Tomato_Bacterial_spot":
                r.displayName  = "Tomato Bacterial Spot";
                r.plantName    = "Tomato";
                r.chemicalCure = "• Spray Copper Oxychloride 50% WP @ 3g/L\n• Apply Streptomycin @ 0.5g/L\n• Spray every 5-7 days";
                r.organicCure  = "• Apply Pseudomonas fluorescens @ 5g/L\n• Spray Bordeaux mixture 1%\n• Remove infected material immediately";
                r.prevention   = "• Use certified disease-free seeds\n• Avoid working in wet fields\n• Disinfect tools between plants";
                break;

            case "Tomato_Early_blight":
                r.displayName  = "Tomato Early Blight";
                r.plantName    = "Tomato";
                r.chemicalCure = "• Spray Chlorothalonil 75% WP @ 2g/L\n• Apply Iprodione @ 1.5g/L\n• Spray at 7-10 day intervals";
                r.organicCure  = "• Apply Trichoderma viride to soil\n• Spray diluted cow urine (1:20) weekly\n• Use garlic-chilli extract spray";
                r.prevention   = "• Stake plants for air circulation\n• Remove infected lower leaves\n• Avoid wetting leaves while irrigating";
                break;

            case "Tomato_Late_blight":
                r.displayName  = "Tomato Late Blight";
                r.plantName    = "Tomato";
                r.chemicalCure = "• Spray Mancozeb 75% WP @ 2g/L water\n• Apply Metalaxyl + Mancozeb (Ridomil Gold)\n• Repeat every 7 days in wet season";
                r.organicCure  = "• Spray Bordeaux Mixture (Copper Sulphate)\n• Apply Neem oil @ 3ml/L every 10 days\n• Remove and burn infected plant parts";
                r.prevention   = "• Avoid overhead irrigation\n• Maintain 45-60 cm spacing\n• Use disease-resistant varieties\n• Rotate crops every season";
                break;

            case "Tomato_Leaf_Mold":
                r.displayName  = "Tomato Leaf Mold";
                r.plantName    = "Tomato";
                r.chemicalCure = "• Spray Chlorothalonil @ 2g/L\n• Apply Copper Oxychloride @ 3g/L\n• Spray every 7-10 days";
                r.organicCure  = "• Improve greenhouse ventilation\n• Apply Trichoderma-based biocontrol\n• Remove infected lower leaves";
                r.prevention   = "• Reduce humidity below 85%\n• Ensure good air circulation\n• Use resistant tomato varieties";
                break;

            case "Tomato_Septoria_leaf_spot":
                r.displayName  = "Tomato Septoria Leaf Spot";
                r.plantName    = "Tomato";
                r.chemicalCure = "• Spray Mancozeb 75% WP @ 2g/L\n• Apply Chlorothalonil @ 2g/L\n• Spray every 7-10 days";
                r.organicCure  = "• Remove infected leaves immediately\n• Apply copper-based fungicide\n• Spray neem oil @ 3ml/L";
                r.prevention   = "• Avoid overhead watering\n• Mulch around base of plants\n• Rotate tomato crops yearly";
                break;

            case "Tomato_Spider_mites_Two_spotted_spider_mite":
                r.displayName  = "Tomato Spider Mites";
                r.plantName    = "Tomato";
                r.chemicalCure = "• Spray Abamectin @ 0.5ml/L\n• Apply Spiromesifen @ 1ml/L\n• Spray every 5-7 days for 3 applications";
                r.organicCure  = "• Spray neem oil @ 5ml/L\n• Use insecticidal soap spray\n• Release predatory mites";
                r.prevention   = "• Monitor undersides of leaves regularly\n• Maintain adequate plant moisture\n• Avoid water stress in plants";
                break;

            case "Tomato__Target_Spot":
                r.displayName  = "Tomato Target Spot";
                r.plantName    = "Tomato";
                r.chemicalCure = "• Spray Azoxystrobin @ 1ml/L\n• Apply Chlorothalonil @ 2g/L\n• Spray every 10-14 days";
                r.organicCure  = "• Remove infected plant material\n• Apply copper-based fungicide\n• Improve air circulation";
                r.prevention   = "• Avoid dense planting\n• Stake plants for airflow\n• Remove infected lower leaves";
                break;

            case "Tomato__Tomato_YellowLeaf__Curl_Virus":
                r.displayName  = "Tomato Yellow Leaf Curl Virus";
                r.plantName    = "Tomato";
                r.chemicalCure = "• No cure for the virus itself\n• Control whitefly with Imidacloprid @ 0.5ml/L\n• Remove and destroy infected plants";
                r.organicCure  = "• Use yellow sticky traps for whitefly\n• Apply neem oil @ 3ml/L\n• Remove infected plants immediately";
                r.prevention   = "• Use virus-resistant tomato varieties\n• Control whitefly population\n• Use reflective mulch to deter whitefly";
                break;

            case "Tomato__Tomato_mosaic_virus":
                r.displayName  = "Tomato Mosaic Virus";
                r.plantName    = "Tomato";
                r.chemicalCure = "• No cure for the virus\n• Remove infected plants immediately\n• Control aphids with Imidacloprid @ 0.5ml/L";
                r.organicCure  = "• Remove and destroy infected plants\n• Disinfect tools with 10% bleach\n• Control aphid vectors with neem oil";
                r.prevention   = "• Use certified virus-free seeds\n• Wash hands before handling plants\n• Control aphid population";
                break;

            case "Tomato_healthy":
                r.displayName  = "Healthy Tomato ✅";
                r.plantName    = "Tomato";
                r.chemicalCure = "No treatment needed — tomato plant is healthy! ✅";
                r.organicCure  = "Continue regular crop management.";
                r.prevention   = "• Monitor weekly for disease signs\n• Maintain proper nutrition\n• Ensure good air circulation";
                break;

            default:
                r.displayName  = rawLabel.replace("_", " ");
                r.plantName    = rawLabel.split("_")[0];
                r.chemicalCure = "Consult your local Krishi Vigyan Kendra for treatment.";
                r.organicCure  = "Apply neem oil @ 3ml/L as general preventive.";
                r.prevention   = "• Monitor crops regularly\n• Maintain good farm hygiene\n• Contact KVK for expert guidance";
                break;
        }
        return r;
    }

    // ─────────────────────────────────────────────────────────
    // UNKNOWN RESULT
    // ─────────────────────────────────────────────────────────
    private Result buildUnknownResult(float confidence, String reason) {
        Result r      = new Result();
        r.isUnknown   = true;
        r.label       = "unknown";
        r.confidence  = confidence;
        r.displayName = "Plant Not Recognized";
        r.plantName   = "Unknown";
        if (reason.equals("no_leaf")) {
            r.chemicalCure =
                    "❌ No plant leaf detected.\n\n" +
                            "Please photograph:\n" +
                            "• A single diseased leaf\n" +
                            "• In good natural lighting\n" +
                            "• With leaf filling the frame\n\n" +
                            "Supported crops:\nTomato, Potato, Bell Pepper";
        } else {
            r.chemicalCure =
                    "❌ Plant not recognized.\n\n" +
                            "Supported crops:\n" +
                            "Tomato, Potato, Bell Pepper\n\n" +
                            "Try taking a clearer photo of the leaf.";
        }
        r.organicCure =
                "📸 Tips for better results:\n\n" +
                        "• Good lighting — no shadows\n" +
                        "• Leaf fills the whole frame\n" +
                        "• Clear focus — not blurry\n" +
                        "• Single leaf — no background";
        r.prevention =
                "📞 Contact:\n\n" +
                        "• Krishi Vigyan Kendra (KVK)\n" +
                        "• Kisan Call Centre: 1800-180-1551\n" +
                        "• State Agriculture Department";
        return r;
    }

    // ─────────────────────────────────────────────────────────
    // MODEL LOADING
    // ─────────────────────────────────────────────────────────
    private MappedByteBuffer loadModel(Context context) throws Exception {
        android.content.res.AssetFileDescriptor fd =
                context.getAssets().openFd("krishi_model.tflite");
        FileInputStream fis = new FileInputStream(fd.getFileDescriptor());
        return fis.getChannel().map(
                FileChannel.MapMode.READ_ONLY,
                fd.getStartOffset(),
                fd.getDeclaredLength());
    }

    // ─────────────────────────────────────────────────────────
    // LABEL LOADING
    // ─────────────────────────────────────────────────────────
    private List<String> loadLabels(Context context) {
        List<String> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            context.getAssets().open("labels.json")));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            // Parse JSON — sorted by key number
            org.json.JSONObject json =
                    new org.json.JSONObject(sb.toString());
            int size = json.length();
            for (int i = 0; i < size; i++) {
                list.add(json.getString(String.valueOf(i)));
            }

            android.util.Log.d("KrishiAI",
                    "Loaded " + list.size() + " labels");

        } catch (Exception e) {
            android.util.Log.e("KrishiAI",
                    "Label error: " + e.getMessage());
            list.add("Unknown");
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────
    // BITMAP TO FLOAT BUFFER — MobileNetV2 normalization
    // ─────────────────────────────────────────────────────────
    private ByteBuffer convertToByteBuffer(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(
                4 * INPUT_SIZE * INPUT_SIZE * 3);
        buffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(pixels, 0, INPUT_SIZE,
                0, 0, INPUT_SIZE, INPUT_SIZE);

        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >>  8) & 0xFF;
            int b =  pixel        & 0xFF;
            // MobileNetV2: normalize to [-1, 1]
            buffer.putFloat((r - 127.5f) / 127.5f);
            buffer.putFloat((g - 127.5f) / 127.5f);
            buffer.putFloat((b - 127.5f) / 127.5f);
        }
        return buffer;
    }

    public void close() {
        if (interpreter != null) interpreter.close();
    }
}