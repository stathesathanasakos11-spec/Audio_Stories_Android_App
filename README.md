# Audio Stories
Audio Stories is an innovative Android application designed to provide users with a rich and accessible audio storytelling experience. Developed for educational purposes for the University of Piraeus (Unipi), the app leverages modern Android development practices and Firebase services to deliver high-quality audio content with advanced voice-controlled navigation.

## Project Overview
This application serves as a digital library where users can browse, search, and listen to a variety of audio stories. It is designed with a strong focus on accessibility, featuring a custom voice command system and full localization in three languages. The project demonstrates a seamless integration of cloud storage, real-time databases, and native Android capabilities like Speech Recognition and Text-to-Speech (TTS).


## Features

- **Secure User Authentication**: Full integration with Firebase Auth for sign-up, login, and secure account management, including a dedicated account deletion feature.
- **Audio Streaming & Visualization**: Seamless on-demand playback of stories with a custom `WaveformView` for modern visual audio feedback.
- **Interactive Voice Commands**: Hands-free navigation allows users to open sections (Favorites, Records, Settings), change languages, or find specific stories by title using spoken commands.
- **Multilingual Support**: Fully localized in **English**, **Greek**, and **French**. Users can dynamically switch the app's language and content preferences.
- **Favorites & Listening History**: Personalized "Favorites" list and a "Records" section to track listening history and statistics, synchronized across devices via Firestore.
- **Dynamic Content Management**: Story metadata and assets are managed in real-time using Firebase Realtime Database and Cloud Storage.



## Tech Stack

- **Core**: Java, Android SDK
- **Backend (Firebase)**:
    - **Authentication**: Secure user identity management.
    - **Realtime Database**: Story metadata and live updates.
    - **Cloud Storage**: Hosting for audio files and story thumbnails.
- **Key Libraries & APIs**:
    - **Glide**: High-performance image loading and caching.
    - **Material Design 3**: Modern UI components and responsive layouts.
    -   **SpeechRecognizer API**: Native processing for voice-activated commands.
    -   **Text-to-Speech (TTS)**: Interactive audio feedback for better accessibility.



## Architecture

The project follows a modular, clean architecture that promotes separation of concerns and maintainability:

- **Activities (UI Layer)**: Manages screen lifecycles and user interactions (e.g., `MainActivity`, `StoryActivity`, `AuthActivity`).
- **Adapters**: Handles data binding between models and UI components like `RecyclerView`.
- **Models**: POJO classes representing core entities such as `Story` and `User`.
- **Utils (Managers)**: Encapsulated business logic and service wrappers.
- **Interfaces**: Defined contracts for event handling and cross-component communication.



## Threading & Concurrency

The application ensures a responsive UI by offloading intensive tasks to background threads:
- **Text-to-Speech (TTS) Threading**: The `TextToSpeech` engine operates on its own background thread. The `TTSManager` uses `UtteranceProgressListener` to track speech progress. Since these callbacks occur on a background thread, the app uses `runOnUiThread()` in `StoryActivity` to safely update UI elements like the progress bar and playback controls.
- **Asynchronous Firebase Operations**: All network tasks (fetching stories, updating favorites, logging in) are handled asynchronously by Firebase's internal thread pool. Result listeners ensure that the UI is updated only when data is ready.
- **Image Processing**: The `Glide` library handles image downloading, decoding, and caching on dedicated background threads, preventing UI "jank" during scrolling.
- **Main Thread Handlers**: A `Handler` is used in `StoryActivity` to manage the periodic rotation of story images every 7 seconds without blocking user interaction.



## Core Utility Classes

The `utils` package contains the backbone of the app's business logic, following the principle of encapsulation:
- **`AuthManager`**: Centralizes all Firebase Authentication logic, including login, registration, and account deletion.
- **`StoryDataManager`**: Manages data flow between the Firebase Realtime Database and the app for story retrieval and favorite toggling.
- **`VoiceCommandManager`**: The "brain" behind voice navigation and the fuzzy-matching algorithm for story titles.
- **`LanguageHelper`**: Orchestrates app-wide localization, handling `Locale` changes and updating system resources dynamically.
- **`TTSManager`**: A wrapper for the Android TTS engine that simplifies language switching and provides custom progress tracking.
- **`RecordsUpdate`**: Handles the logic for updating listening statistics in both Firestore (user-specific) and the Realtime Database (global).
- **`WaveformView`**: A custom UI component that renders a dynamic progress bar styled as an audio waveform.



## Deep Dive into Code

### Data Repository Pattern
While the project uses Firebase directly, it implements a **Repository-like pattern** through the `StoryDataManager` and `AuthManager` classes. These classes encapsulate the Firebase-specific implementation (e.g., `DatabaseReference`, `FirebaseAuth`), providing a clean API for the UI layer to interact with data. This abstraction makes the `Activity` classes lighter and ensures that data-related logic is centralized.

### State Management
The project handles state management using **Native Android State Preservation** and **Real-time Callbacks**:
- **`onSaveInstanceState` / `onRestoreInstanceState`**: In `StoryActivity`, these are used to preserve the playback position (`lastCharIndex`) and the visibility of UI overlays (like the "Lesson" popup) during configuration changes (e.g., screen rotation).
- **Firebase Listeners**: Real-time state updates (like "Favorite" status) are managed via `ValueEventListener`. When data changes in Firebase, the UI is automatically notified and updated, ensuring a "reactive" feel without the overhead of complex state libraries.

### Complex Logic: Voice-to-Story Matching
One of the most complex features is the **Fuzzy Matching Algorithm** in `VoiceCommandManager.java` used to find stories by voice instructions.
1. **Input Processing**: The `findStoryByTitle` method takes multiple "matches" from the `SpeechRecognizer`.
2. **Normalization**: It normalizes both the voice input and the story titles (lowercasing, trimming).
3. **Language Awareness**: It checks the titles against all three supported languages (English, Greek, French).
4. **Levenshtein Distance**: To account for speech recognition errors, it uses the Levenshtein Distance algorithm to calculate string similarity. If the similarity exceeds a 75% threshold for the majority of words in a title, a match is confirmed.

### Clean Code Principles
- **DRY (Don't Repeat Yourself)**: Centralized management of localization through `LanguageHelper` and Firebase logic through `AuthManager` ensures that complex code is written once and reused throughout the app.
- **Separation of Concerns**: UI code (Activities) is strictly separated from business logic (Utils) and data models.
- **SOLID (Interface Segregation)**: Custom interfaces like `OnStoryProgressListener` are used to allow communication between managers (like `TTSManager`) and activities without creating tight coupling.



## Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/UnipiAudioStories.git
   ```
2. **Firebase Setup**:
   - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.myprojects.unipiaudiostories`.
   - Download the `google-services.json` file and place it in the `app/` directory.
   - Enable **Authentication**, **Realtime Database**, **Firestore**, and **Storage**.
3. **Build and Run**:
   - Open the project in Android Studio.
   - Sync with Gradle files.
   - Run on an emulator or device (API 33+).


