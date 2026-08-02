# NexGen LMS

NexGen By KGC is an Android Learning Management System (LMS) mobile application built with Kotlin, Jetpack Compose, and Room DB, leveraging the InsForge SDK for Backend-as-a-Service capabilities and Cloudinary for media uploads.

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Local Database:** Room Database
- **Backend-as-a-Service:** InsForge SDK
- **Media Uploads:** Cloudinary
- **Build System:** Gradle (9.3.1+)

## Prerequisites

- JDK 17
- Android SDK Build-Tools 36
- A generated `.env` file (see below)

## Setup Instructions

1. **Environment Variables:**
   Copy the `.env.example` file to create a `.env` file in the root directory:
   ```bash
   cp .env.example .env
   ```
   Populate the `.env` file with your specific variables. The application requires the following variables to build correctly (either via `.env` or CI secrets):
   - `INSFORGE_PROJECT_URL`
   - `INSFORGE_ANON_KEY`
   - `CLOUDINARY_CLOUD_NAME`
   - `CLOUDINARY_UPLOAD_PRESET`

2. **Debug Keystore (Optional, if build fails):**
   If the Android debug build fails with `validateSigningDebug FAILED`, generate a temporary debug keystore in the root directory:
   ```bash
   keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
   ```

## Build and Run

To build the debug version of the application, run:
```bash
./gradlew app:assembleDebug
```

To run the unit test suite, use:
```bash
./gradlew app:testDebugUnitTest
```

## Features

- **Strict Role Partitioning:** Secure workflows for Students, Tutors, and Super Admins.
- **Media Management:** Direct uploads and playback of lessons and resources using Cloudinary.
- **Authentication:** Native authentication and Google OAuth integration via InsForge.
- **Accessible UI:** Jetpack Compose UI engineered for a seamless and accessible mobile experience.

## Contributing

When contributing to UI changes, ensure they adhere to mobile accessibility guidelines (such as `KeyboardOptions` for appropriate form inputs, and `contentDescription` for icon-only buttons).
