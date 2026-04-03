# 🐾 FoundBuddy - Lost & Found AI Companion

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Render](https://img.shields.io/badge/Deployed_on-Render-46E3B7?logo=render&logoColor=white)](https://render.com/)

**FoundBuddy** is a modern Lost & Found platform that leverages AI to help people find their lost items faster. By using visual similarity search (CLIP), users can find lost pets or belongings just by uploading a photo.

---

## 🚀 Features

- **🤖 AI Visual Search**: Search for lost items using image similarity (powered by CLIP).
- **📸 Quick Reporting**: easily report lost or found items with photos and descriptions.
- **💬 Real-time Chat**: Coordinate the return of items directly within the app.
- **📧 Smart Notifications**: Get notified via email when someone might have found your item.
- **🔐 Secure Auth**: Firebase-powered authentication with email verification.
- **📱 Modern UI**: Sleek Android app built with Jetpack Compose.

---

## 🛠️ Technology Stack

### Frontend
- **Language**: Kotlin
- **Framework**: Jetpack Compose (Modern Android UI)
- **Networking**: Retrofit & OkHttp
- **Local Storage**: DataStore / Room
- **Authentication**: Firebase Auth

### Backend
- **Language**: Java / Spring Boot
- **Database**: PostgreSQL / H2
- **Storage**: Firebase Storage (for images)
- **Email**: Brevo (formerly Sendinblue)
- **Deployment**: Render

### AI Service
- **Model**: OpenAI CLIP (hosted on Hugging Face Spaces)
- **Functionality**: Generates image embeddings for visual similarity search.

---

## 📂 Project Structure

```bash
FoundBuddy/
├── FoundBuddy-frontend/   # Android App (Kotlin/Compose)
├── FoundBuddy-backend/    # Spring Boot Service (Java)
├── foundbuddy-clip/       # AI Service (CLIP Integration)
└── render.yaml            # Infrastructure as Code (Render)
```

---

## ⚙️ Setup & Installation

### Backend
1. Navigate to `FoundBuddy-backend`.
2. Configure your environment variables (Firebase, Brevo API key, etc.) in `application.properties` or environment.
3. Run `./gradlew bootRun`.

### Frontend
1. Open `FoundBuddy-frontend` in Android Studio.
2. Add your `google-services.json` (Firebase).
3. Build and run on an emulator or physical device.

---

## 🎨 Screenshots
*(Coming soon - Add your app screenshots here!)*

---

## 🤝 Contributing

This project was developed as part of a school project (HTL). Contributors:
- **Matthias Sperl**
- **Monika Juric**

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
<p align="center">Made with ❤️ for the HTL community.</p>
