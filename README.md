# KrishiAI: Offline Crop Disease Detection App

An Android app that helps farmers identify crop diseases from a photo of a leaf, even without an internet connection. The disease detection model runs on the phone using TensorFlow Lite, so it works in fields with poor or no network.

## Features

- Offline crop disease detection using a MobileNetV2 model running through TensorFlow Lite
- Firebase integration
- Gemini API integration for AI assistance
- Available in English, Hindi and Marathi

> Edit this list to match exactly what the app does today (for example: scan from camera or gallery, disease details, treatment advice, chat).

## Tech Stack

| Area | Technology |
|------|------------|
| App | Android (Java) |
| On-device ML | MobileNetV2, TensorFlow Lite |
| Backend services | Firebase |
| AI | Gemini API |
| Languages | English, Hindi, Marathi |

## Getting Started

### Prerequisites

- Android Studio (latest stable version)
- JDK 17 (the one bundled with Android Studio works)
- An Android phone or emulator running a recent Android version

### 1. Clone the project

```bash
git clone https://github.com/shaunak36/Krishi-Ai.git
```

Open the `android_app` folder in Android Studio.

### 2. Add your Firebase config

1. Create a project in the [Firebase Console](https://console.firebase.google.com) and add an Android app with the package name `com.krishiai.app`.
2. Download `google-services.json` and place it in the `app/` folder.

This file is ignored by git, so you must add your own.

### 3. Add your Gemini API key

Create an API key in [Google AI Studio](https://aistudio.google.com/apikey), then add it to `local.properties` in the project root:

```
GOOGLE_API_KEY=your_key_here
```

`local.properties` is ignored by git. Never commit API keys.

### 4. Run

Click **Sync Project with Gradle Files**, then **Run** in Android Studio.

## Security Notes

- API keys and `google-services.json` are never committed to this repository.
- If you fork this project, restrict your keys to the Android package name and your signing SHA-1 in Google Cloud Console.

## Screenshots

Add screenshots of the home screen, scan screen and result screen here.

## Author

Shaunak Paul
GitHub: [shaunak36](https://github.com/shaunak36)
LinkedIn: [shaunak-paul-000907321](https://linkedin.com/in/shaunak-paul-000907321)
