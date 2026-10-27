package com.krishiai.app;

import java.util.ArrayList;
import java.util.List;

public class NewsHelper {

    public static class NewsItem {
        public String  title;
        public String  summary;
        public String  date;
        public String  source;
        public boolean isAlert;
        public String  imageUrl;

        public NewsItem(String title, String summary, String date,
                        String source, boolean isAlert, String imageUrl) {
            this.title    = title;
            this.summary  = summary;
            this.date     = date;
            this.source   = source;
            this.isAlert  = isAlert;
            this.imageUrl = imageUrl;
        }
    }

    public static List<NewsItem> getNews(String language) {
        List<NewsItem> news = new ArrayList<>();

        if (language.equals("HI")) {
            news.add(new NewsItem(
                    "🌾 खरीफ फसलों के MSP में 7% की वृद्धि",
                    "सरकार ने धान, कपास और दलहन की समर्थन मूल्य बढ़ाने की घोषणा की",
                    "2 मार्च 2026", "कृषि समाचार भारत", false,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/2/27/Paddy_field_Tamil_Nadu.jpg/320px-Paddy_field_Tamil_Nadu.jpg"));
            news.add(new NewsItem(
                    "🌧 महाराष्ट्र में सामान्य मानसून का अनुमान",
                    "IMD ने 2026 में दीर्घकालीन औसत वर्षा का 98% अनुमान लगाया",
                    "1 मार्च 2026", "IMD रिपोर्ट", false,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9c/Monsoon_clouds_over_Mumbai.jpg/320px-Monsoon_clouds_over_Mumbai.jpg"));
            news.add(new NewsItem(
                    "🦠 नासिक में टमाटर पर लेट ब्लाइट की चेतावनी",
                    "किसानों को तांबा आधारित कवकनाशी छिड़कने की सलाह दी गई",
                    "28 फरवरी 2026", "कृषि विज्ञान केंद्र", true,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/Late_blight_on_tomato.jpg/320px-Late_blight_on_tomato.jpg"));
            news.add(new NewsItem(
                    "💧 PM किसान सिंचाई योजना विस्तार",
                    "सरकार ने 50 लाख हेक्टेयर को सिंचाई कवरेज में जोड़ा",
                    "25 फरवरी 2026", "कृषि मंत्रालय", false,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Drip_irrigation_img_0107.jpg/320px-Drip_irrigation_img_0107.jpg"));

        } else if (language.equals("MR")) {
            news.add(new NewsItem(
                    "🌾 खरीप पिकांच्या MSP मध्ये 7% वाढ",
                    "सरकारने धान, कापूस आणि डाळींच्या आधारभूत किमती वाढवल्या",
                    "2 मार्च 2026", "कृषी बातम्या भारत", false,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/2/27/Paddy_field_Tamil_Nadu.jpg/320px-Paddy_field_Tamil_Nadu.jpg"));
            news.add(new NewsItem(
                    "🌧 महाराष्ट्रात सामान्य पाऊस अपेक्षित",
                    "IMD ने 2026 मध्ये दीर्घकालीन सरासरी पावसाच्या 98% अंदाज केला",
                    "1 मार्च 2026", "IMD अहवाल", false,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9c/Monsoon_clouds_over_Mumbai.jpg/320px-Monsoon_clouds_over_Mumbai.jpg"));
            news.add(new NewsItem(
                    "🦠 नाशिकमध्ये टोमॅटोवर उशिरा ब्लाइटचा इशारा",
                    "शेतकऱ्यांना तांबे-आधारित बुरशीनाशक फवारण्याचा सल्ला",
                    "28 फेब्रुवारी 2026", "कृषी विज्ञान केंद्र", true,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/Late_blight_on_tomato.jpg/320px-Late_blight_on_tomato.jpg"));
            news.add(new NewsItem(
                    "💧 PM शेतकरी सिंचन योजना विस्तार",
                    "50 लाख हेक्टर जमीन सिंचन कव्हरेजमध्ये जोडली",
                    "25 फेब्रुवारी 2026", "कृषी मंत्रालय", false,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Drip_irrigation_img_0107.jpg/320px-Drip_irrigation_img_0107.jpg"));

        } else {
            // English
            news.add(new NewsItem(
                    "🌾 MSP for Kharif crops increased by 7%",
                    "Government announces support price hike for paddy, cotton and pulses this season",
                    "March 2, 2026", "AgriNews India", false,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/2/27/Paddy_field_Tamil_Nadu.jpg/320px-Paddy_field_Tamil_Nadu.jpg"));
            news.add(new NewsItem(
                    "🌧 IMD forecasts normal monsoon for Maharashtra",
                    "India Meteorological Department predicts 98% of long-period average rainfall in 2026",
                    "March 1, 2026", "IMD Report", false,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9c/Monsoon_clouds_over_Mumbai.jpg/320px-Monsoon_clouds_over_Mumbai.jpg"));
            news.add(new NewsItem(
                    "🦠 Late blight alert for tomato farmers in Nashik",
                    "Farmers advised to spray copper-based fungicides as preventive measure immediately",
                    "Feb 28, 2026", "Krishi Vigyan Kendra", true,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/Late_blight_on_tomato.jpg/320px-Late_blight_on_tomato.jpg"));
            news.add(new NewsItem(
                    "💧 PM Kisan Irrigation Scheme expansion",
                    "Government adds 50 lakh hectares to irrigation coverage across 12 states",
                    "Feb 25, 2026", "Agriculture Ministry", false,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Drip_irrigation_img_0107.jpg/320px-Drip_irrigation_img_0107.jpg"));
            news.add(new NewsItem(
                    "🌱 New drought-resistant wheat variety released by ICAR",
                    "HD-3385 variety shows 15% higher yield in low rainfall conditions",
                    "Feb 20, 2026", "ICAR India", false,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/24701-nature-natural-beauty.jpg/320px-24701-nature-natural-beauty.jpg"));
        }
        return news;
    }
}