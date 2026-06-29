

# GDG Hub - Android News & Event App 📰✨

## [](https://kotlinlang.org) [](https://developer.android.com/jetpack/compose) [](https://firebase.google.com/) [](https://ai.google.dev/gemini-api)

GDG Hub is an Android application designed to keep Google Developer Group (GDG) members informed about the latest news, articles, and events. It features a personalized news feed powered by **Google's Gemini AI** for content filtering based on user-selected tags. Stay connected and never miss an update from your GDG community\!

-----

## ✨ Features

Discover what GDG Hub has to offer:

  * **📰 News Feed:** Browse a dynamically updated list of news articles and community updates.

      * *Add an image here showing the news feed.*
      * `![News Feed Screenshot](your_image_url_news_feed.png)`

  * **🔍 AI-Powered Filtering:** Personalize your news experience\!

      * Click on specific tags within an article or from a dedicated selection area.
      * **Gemini AI** intelligently processes your selected tags and the available articles to present you with the most relevant content, cutting through the noise.
      * *Add an image here showing the filtering in action (e.g., tags selected, filtered list).*
      * `![AI Filtering Screenshot](your_image_url_ai_filtering.png)`

  * **📄 Article Detail View:** Dive deep into the full content of news articles.

      * *Add an image here showing an article's detail view.*
      * `![Article Detail Screenshot](your_image_url_article_detail.png)`

  * **➕ Add New Articles:** (If implemented) A dedicated feature for authorized users to seamlessly contribute and share new articles with the community.

  * **☁️ Firebase Integration:** A robust and scalable backend ensures a smooth experience.

      * **Firestore:** Our primary backend for storing and retrieving all news articles efficiently.
      * **Firebase for Gemini:** Seamlessly interacts with the Gemini API to power the intelligent content personalization.
      * **(Optional) Firebase Storage:** Used for hosting images linked within articles, ensuring fast and reliable image delivery.
      * **(Optional) Firebase Authentication:** If user accounts are implemented for features like article contribution or personalized settings.

  * **📱 Modern Android Development:** Built with cutting-edge technologies for performance and maintainability.

      * Built entirely with **Kotlin** for a concise and safe codebase.
      * UI crafted using **Jetpack Compose** for a declarative, reactive, and beautiful user interface.
      * Follows **MVVM (Model-View-ViewModel)** architecture for clear separation of concerns.
      * Asynchronous operations managed with **Kotlin Coroutines and Flows** for smooth data handling.
      * Navigation handled by **Jetpack Navigation Compose** for a robust and intuitive user flow.
      * Image loading with **Coil** for efficient and fast image display.

-----

## 🚀 Getting Started

Ready to run GDG Hub on your device? Follow these steps\!

### Prerequisites

Before you begin, ensure you have the following installed:

  * **Android Studio Iguana | 2023.2.1** or newer.
  * **Kotlin version** compatible with the project (check your `build.gradle` files for specifics).
  * An Android device or emulator running **API level 24 (Nougat)** or higher (adjust as per your project's `minSdk` in `build.gradle`).

### Firebase Setup (Crucial\!)

This project relies heavily on Firebase for its backend and AI capabilities. You **must** set up your own Firebase project and connect it to this Android application.

1.  **Create a Firebase Project:**

      * Navigate to the [Firebase Console](https://console.firebase.google.com/).
      * Click on "Add project" and follow the on-screen instructions to set up a new project.

2.  **Add an Android App to Your Firebase Project:**

      * From your newly created Firebase project dashboard, click the **Android icon** (or "Add app") to register an Android app.
      * Enter your app's **package name** (e.g., `com.example.gdghub`). You can find this in your `app/build.gradle` file under `applicationId`.
      * Download the `google-services.json` configuration file when prompted.

3.  **Add `google-services.json` to Your Project:**

      * Place the downloaded `google-services.json` file directly into the **`app/` directory** of your Android Studio project.
      * *Visual aid: A screenshot showing where to place `google-services.json`.*
      * `![Place google-services.json](your_image_url_google_services_json_location.png)`

4.  **Enable Firebase Services:**

      * **Firestore:**
          * In the Firebase console, go to "Firestore Database" and click "Create database."
          * For initial development, we recommend starting in **test mode** for easier setup. **Remember to set up robust [security rules](https://firebase.google.com/docs/firestore/security/get-started) before deploying to production\!**
          * Choose your desired Firestore location.
      * **Vertex AI / Gemini API:**
          * Ensure the **Vertex AI API** is enabled for your Google Cloud project (this is the same project associated with your Firebase project). You can enable it from the [Google Cloud Console API Library](https://console.cloud.google.com/apis/library/vertexai.googleapis.com).
          * This project leverages the Firebase for Gemini (Vertex AI) SDK, which significantly simplifies authentication.
      * **(Optional) Firebase Storage:** If you plan to store images (like article thumbnails), enable Firebase Storage in the console. Don't forget to configure its security rules as well.

5.  **API Keys & Gemini Model Access (Important for Gemini):**

      * For the Firebase for Gemini (Vertex AI) SDK, authentication is generally handled seamlessly via Firebase service accounts if your app is configured correctly with Firebase.
      * **Crucially**, ensure that your chosen Gemini model (e.g., `gemini-1.5-flash-latest`, typically specified in `NewsViewModel.kt` or similar files) is available in your Firebase project's region.
      * **Billing must be enabled** for your Google Cloud project, as Vertex AI services are billable.

### Build and Run

1.  **Clone the Repository:**

    ```bash
    git clone https://github.com/YourUsername/GDGHub.git
    cd GDGHub
    ```

2.  **Open in Android Studio:**

      * Open the cloned project in Android Studio.

3.  **Sync Project with Gradle Files:**

      * Allow Android Studio to sync the project with Gradle files. This might take a moment as it downloads dependencies.

4.  **Run the App:**

      * Connect an Android device to your computer or launch an Android emulator.
      * Click the **"Run" (▶︎) button** in Android Studio (usually in the toolbar) to build and install the app on your selected device/emulator.

-----

## 🤝 Contributing

We welcome contributions to GDG Hub\! If you have suggestions, bug reports, or want to contribute code, please feel free to open an issue or submit a pull request.

-----

## 📞 Contact

Have questions or want to connect?

  * **Your Name/Handle:** [https://github.com/Hemanshu4949]
  * **Email:** [hemanshusojitra30@gmail.com]

-----

## 🙏 Acknowledgments

  * Special thanks to Google for the amazing **Firebase** platform and **Gemini AI** capabilities.
  * The **Kotlin**, **Jetpack Compose**, and **Android Development** communities for their continuous innovation and support.

