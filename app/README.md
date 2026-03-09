# Unipi Audio Stories 🎙️

Unipi Audio Stories is an innovative Android application designed to provide users with a rich and accessible audio storytelling experience. Developed for the University of Piraeus (Unipi), the app leverages modern Android development practices and Firebase services to deliver high-quality audio content with advanced voice-controlled navigation.

## Project Overview

This application serves as a digital library where users can browse, search, and listen to a wide variety of audio stories. It is designed with a strong focus on accessibility, featuring a custom voice command system and full localization in three languages. The project demonstrates a seamless integration of cloud storage, real-time databases, and native Android capabilities like Speech Recognition and Text-to-Speech (TTS).

## Features

-   **Secure User Authentication**: Full integration with Firebase Auth for sign-up, login, and secure account management, including a dedicated account deletion feature.
-   **Audio Streaming & Visualization**: Seamless on-demand playback of stories with a custom `WaveformView` for modern visual audio feedback.
-   **Interactive Voice Commands**: Hands-free navigation allows users to open sections (Favorites, Records, Settings), change languages, or find specific stories by title using spoken commands.
-   **Multilingual Support**: Fully localized in **English**, **Greek**, and **French**. Users can dynamically switch the app's language and content preferences.
-   **Favorites & Listening History**: Personalized "Favorites" list and a "Records" section to track listening history and statistics, synchronized across devices via Firestore.
-   **Dynamic Content Management**: Story metadata and assets are managed in real-time using Firebase Realtime Database and Cloud Storage.

## Tech Stack

-   **Core**: Java, Android SDK
-   **Backend (Firebase)**:
    -   **Authentication**: Secure user identity management.
    -   **Realtime Database**: Story metadata and live updates.
    -   **Cloud Firestore**: Persistent user-specific data and records.
    -   **Cloud Storage**: Hosting for audio files and story thumbnails.
-   **Key Libraries & APIs**:
    -   **Glide**: High-performance image loading and caching.
    -   **Material Design 3**: Modern UI components and responsive layouts.
    -   **SpeechRecognizer API**: Native processing for voice-activated commands.
    -   **Text-to-Speech (TTS)**: Interactive audio feedback for better accessibility.

## Architecture

The project follows a modular, clean architecture that promotes separation of concerns and maintainability:

-   **Activities (UI Layer)**: Manages screen lifecycles and user interactions (e.g., `MainActivity`, `StoryActivity`, `AuthActivity`).
-   **Adapters**: Handles data binding between models and UI components like `RecyclerView`.
-   **Models**: POJO classes representing core entities such as `Story` and `User`.
-   **Utils (Managers)**: Encapsulated business logic and service wrappers:
    -   `AuthManager`: Centralized logic for Firebase authentication.
    -   `VoiceCommandManager`: Processes voice recognition results into app-specific intents.
    -   `LanguageHelper`: Manages app-wide locale settings and translations.
    -   `TTSManager`: Handles the lifecycle and execution of Text-to-Speech services.
-   **Interfaces**: Defined contracts for event handling and cross-component communication.


## 🚀 Setup Instructions

1.  **Clone the Repository**:
    