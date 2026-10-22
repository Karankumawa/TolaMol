# TolaMol v2.0.0 🪙

**TolaMol** is a modern, feature-rich Android application designed for jewellery merchants, goldsmiths, and buyers to perform precise gold and silver weight/price calculations, quick billing, cash counting, and inventory management with real-time MongoDB cloud synchronization.

---

## ✨ Features

- ⚖️ **Weight Calculator**: Calculate total amount based on weight, purity (Karat / Melting percentage), making charges, and taxes (GST).
- 💰 **Amount Calculator**: Determine exact weight of gold/silver achievable for a given budget.
- 🔍 **Price Finder**: Rapidly calculate rate per gram or per Tola based on target price and weight.
- 🧾 **Quick Billing**: Seamless point-of-sale invoice generation, itemized billing, TTS (Text-to-Speech) summary, and receipt management.
- 💵 **Cash Counter**: Intuitive currency note counter for instant tallying and shop register balancing.
- 📦 **Saved Items & Rate Management**: Store daily gold/silver rates, item templates, and custom catalog items locally with Room Database.
- ☁️ **Cloud Synchronization**: Automatic sync with MongoDB backend for backup and multi-device rate updates.
- 🎨 **Material Design 3**: Fully expressive, dynamic Material 3 theme with Dark Mode support and responsive edge-to-edge UI.

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM with StateFlow
- **Database**: Room Database (Local storage) + MongoDB Realm/Remote (Cloud sync)
- **Navigation**: Jetpack Navigation Compose
- **Other Utilities**: Text-To-Speech (TTS), AndroidX Lifecycle, Kotlin Coroutines

---

## 🚀 Getting Started

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Karankumawa/TolaMol.git
   cd TolaMol
   ```

2. **Configure Local Properties**
   Create a `local.properties` file in the root directory and add your credentials if required:
   ```properties
   sdk.dir=YOUR_ANDROID_SDK_PATH
   MONGO_APP_ID=YOUR_MONGO_APP_ID
   ```

3. **Build & Run**
   Open the project in Android Studio (Ladybug / Iguana or later) and execute `./gradlew assembleDebug` or run directly on an emulator/device.

---

## 📄 License

```
Copyright (C) 2025 TolaMol Team
Licensed under the Apache License, Version 2.0.
```
