# Silent Mode Scheduler - Technical Stack & Architectural Document

This document outlines the comprehensive technical architecture, frameworks, system services, and data flows of the **Silent Mode Scheduler** Android application. It provides both high-level design descriptions and exact file-level mapping to support technical reviews, project presentations, and developer onboarding.

---

## Part 1: Technology Stack Analysis

For each technology utilized in the project, the details of its role, placement, operations, and dependencies are analyzed below.

### 1. Kotlin
*   **Category:** Programming Language
*   **Purpose:** The primary language for writing Android application code, UI configurations, business logic, and build scripts.
*   **Why Chosen:** Modern standard for Android development, offering null safety (preventing `NullPointerException`), coroutines for easy asynchronous processing, and full compatibility with Jetpack Compose.
*   **Where Used:** All files ending in `.kt` and `.kts` across the modules.
*   **How it Works:** Compiles down to JVM bytecode (targeting JVM 17 in this project) to run on the Android Runtime (ART/Dalvik). Uses property delegates (`by lazy`, `by remember`), structured concurrency (coroutines), and extension functions.
*   **Interactions:** Android SDK, Jetpack Compose runtime, Firebase Kotlin Extensions (KTX), and system services.
*   **What Breaks if Removed:** The entire application since all source files and Gradle build scripts are written in Kotlin.

### 2. Android Studio
*   **Category:** IDE (Integrated Development Environment)
*   **Purpose:** The official development environment for designing, compiling, refactoring, and testing the Android application.
*   **Why Chosen:** Provides specialized tooling for Jetpack Compose previews, layout inspections, Android device emulator management, Logcat debugging, and Firebase Assistant integrations.
*   **Where Used:** Used during the development phase. Settings are saved in `.idea/` and `.kotlin/` folders.
*   **How it Works:** Automates code generation, manages the Android SDK installations, compiles resources, and orchestrates the Gradle build tools.
*   **Interactions:** Gradle build tool, Android SDK tools, JDK 17, and emulator/physical test devices.
*   **What Breaks if Removed:** Development workflow slows down significantly; code compilation and debugging must be done manually using terminal scripts.

### 3. Jetpack Compose
*   **Category:** UI Framework
*   **Purpose:** A modern, declarative toolkit for building native Android user interfaces with less boilerplate and dynamic state binding.
*   **Why Chosen:** Simplifies UI updates. Eliminates XML layout files. Replaces them with reusable composable functions that automatically redraw (recompose) when state updates.
*   **Where Used:** Under `presentation/screens/` and `presentation/ui/`.
*   **How it Works:** UI elements are represented as `@Composable` functions. It listens to state transformations (via Compose's `StateFlow.collectAsState()`) and triggers recomposition only for components affected by the state changes.
*   **Interactions:** Material Design 3 library, Navigation Compose, and ViewModels.
*   **What Breaks if Removed:** The visual layout and UI interface since no XML files are present in the project.

### 4. Material Design 3 (M3)
*   **Category:** Design System Library
*   **Purpose:** Provides modern UI components (like Buttons, OutlinedTextFields, AlertDialogs, FloatingActionButtons, Cards) that follow Google's latest design specifications.
*   **Why Chosen:** Ensures a modern look and feel out-of-the-box, with built-in accessibility, support for light/dark dynamic themes, and rounded corner shapes.
*   **Where Used:** Main themes in `presentation/ui/theme/` and components inside screens (`MainScreen.kt`, `TimePeriodDialog.kt`, etc.).
*   **How it Works:** Implements Material design tokens. Composable components are configured using `MaterialTheme.colorScheme`, `MaterialTheme.typography`, and `MaterialTheme.shapes`.
*   **Interactions:** Jetpack Compose layout.
*   **What Breaks if Removed:** UI styling, dialog formats, input fields, cards, and colors revert to basic unstyled components or cause immediate compilation failures.

### 5. MVVM (Model-View-ViewModel) Architecture
*   **Category:** Architectural Pattern
*   **Purpose:** Decouples UI display logic (View) from business rules and data models (Model) using a state-holder class (ViewModel).
*   **Why Chosen:** Makes the codebase testable, maintainable, and scalable. Prevents activities from holding business state, ensuring state survives configuration changes (such as device rotation).
*   **Where Used:** Implemented across the entire project structure:
    *   **Model:** `data/model/TimePeriod.kt`
    *   **View:** `presentation/screens/`
    *   **ViewModel:** `presentation/viewmodel/`
*   **How it Works:** The View (Compose UI) observes UI State from the ViewModel. The View forwards user interactions (clicks, text input) to the ViewModel as events. The ViewModel coordinates with repositories to fetch/save data and updates the UI State.
*   **Interactions:** All layers of the application.
*   **What Breaks if Removed:** Code separation degrades; screens become cluttered with database and state logic, leading to memory leaks and UI reset bugs during lifecycle changes.

### 6. ViewModel (Jetpack Lifecycle)
*   **Category:** State Holder Library
*   **Purpose:** A lifecycle-aware component designed to store and manage UI-related data.
*   **Why Chosen:** Automatically survives configuration changes (like device rotation).
*   **Where Used:** Subdirectory `presentation/viewmodel/` (`MainViewModel.kt`, `TimePeriodViewModel.kt`, etc.).
*   **How it Works:** Scoped to the lifecycle of the destination screen. It remains in memory as long as the screen is in the backstack. It provides data using `StateFlow` and runs background threads using `viewModelScope`.
*   **Interactions:** View (Compose Screens), Repositories, Coroutines, and Navigation.
*   **What Breaks if Removed:** Changing device orientations will clear input text, reset loadings, and trigger redundant database reads.

### 7. StateFlow and MutableStateFlow (Kotlin Flows)
*   **Category:** Reactive Data Stream Library
*   **Purpose:** Provides a state-holder observable data flow that emits current and new state updates to its collectors.
*   **Why Chosen:** Native to Kotlin, lightweight, and integrates with Compose via `collectAsState()`.
*   **Where Used:** ViewModels (`MainViewModel.kt`, `LoginViewModel.kt`, etc.) and collected in screens (`MainScreen.kt`).
*   **How it Works:** `MutableStateFlow` is updated inside the ViewModel using `.update {}` or `.value = ...`. The UI collects it as a read-only `StateFlow`, triggering recomposition whenever the value updates.
*   **Interactions:** Kotlin Coroutines, ViewModels, and Jetpack Compose.
*   **What Breaks if Removed:** Reactive state updates stop working; the UI will not reflect data changes from Firebase or validation states.

### 8. Kotlin Coroutines
*   **Category:** Asynchronous Programming Library
*   **Purpose:** Simplifies execution of asynchronous, non-blocking code.
*   **Why Chosen:** Prevents blocking the Main Thread (UI thread), allowing database transactions, network authentication, and timers to run smoothly in the background.
*   **Where Used:** ViewModels, repositories, database checks, and receiver task scopes.
*   **How it Works:** Uses suspension points instead of blocking. Routines are launched on specific dispatchers (e.g., `Dispatchers.IO` for network/disk operations, `Dispatchers.Main` for UI modifications).
*   **Interactions:** StateFlow, Firebase SDK, and ViewModels.
*   **What Breaks if Removed:** Running database operations or network calls on the main thread throws `NetworkOnMainThreadException` or freezes the user interface (ANR - Application Not Responding).

### 9. Firebase Authentication (Phone OTP)
*   **Category:** Identity & Authentication Service
*   **Purpose:** Handles secure, passwordless authentication using one-time verification codes sent via SMS.
*   **Why Chosen:** Eliminates the need for credentials management, providing a quick mobile authentication system.
*   **Where Used:** `data/firebase/auth/FirebaseAuthManager.kt`, used in `LoginViewModel` and `RegisterViewModel`.
*   **How it Works:** Sends an OTP code to a verified phone number via Google Play Services. Receives code input from the user, verifies it, and returns a authenticated `FirebaseUser` session.
*   **Interactions:** Google Services plugin, repository layer, and auth view models.
*   **What Breaks if Removed:** Users cannot register, login, or access their individual schedules.

### 10. Firebase Firestore
*   **Category:** NoSQL Cloud Database
*   **Purpose:** Stores user profiles and silent schedules in a cloud database.
*   **Why Chosen:** Provides real-time synchronization, automatic offline cache support, and dynamic scaling.
*   **Where Used:** Repository interface and implementation (`data/firebase/firestore/`).
*   **How it Works:** Stores data in documents grouped inside collections. Queries are mapped to Kotlin structures. Uses offline cache configuration inside `SilentModeSchedulerApp` to allow the app to work without network access.
*   **Interactions:** Firebase BOM, repository implementation, and Coroutines.
*   **What Breaks if Removed:** Users cannot save, edit, view, or delete silent schedules.

### 11. Google Services Plugin & google-services.json
*   **Category:** Build Tool Plugin & Configuration
*   **Purpose:** Configures build targets to link with specific Firebase projects by parsing configuration settings.
*   **Why Chosen:** Automatically injects the Firebase app ID, database URLs, and API keys into the app resources during compile time.
*   **Where Used:** Root `build.gradle.kts` (as a classpath dependency), `app/build.gradle.kts` (applied plugin), and `app/google-services.json`.
*   **How it Works:** The plugin parses the `google-services.json` file and generates XML configuration files in the build folder that the Firebase SDK reads at startup.
*   **Interactions:** Gradle, Android Gradle Plugin, and Firebase SDK.
*   **What Breaks if Removed:** Compilation fails with `File google-services.json is missing` or Firebase fails to initialize at runtime.

### 12. AlarmManager
*   **Category:** System Service API
*   **Purpose:** Schedules exact intent broadcasts at specified times, waking up the device if it is asleep.
*   **Why Chosen:** Allows exact scheduling of silent mode toggles without running a persistent background service, conserving battery.
*   **Where Used:** `core/scheduler/SilentModeScheduler.kt` and `SilentModeReceiver.kt`.
*   **How it Works:** Registers an alarm with the OS. The OS wakes up the app when the time matches and fires the registered `BroadcastReceiver` intent.
*   **Interactions:** BroadcastReceiver, PendingIntent, and Android OS.
*   **What Breaks if Removed:** Automatic silent mode scheduling stops working; times will not trigger toggles.

### 13. BroadcastReceiver (`SilentModeReceiver`)
*   **Category:** Android Component
*   **Purpose:** Listens to system broadcasts (like boot complete) and scheduled alarm intents.
*   **Why Chosen:** Essential for receiving `AlarmManager` wake signals and starting app routines from a closed state.
*   **Where Used:** `core/scheduler/SilentModeReceiver.kt` (declared in `AndroidManifest.xml`).
*   **How it Works:** When the OS triggers the registered alarm action (`ACTION_START_SILENT` or `ACTION_END_SILENT`), `onReceive` is invoked. It checks permissions, toggles the phone ringer, and posts notifications.
*   **Interactions:** AlarmManager, AudioManager, NotificationManager, and Firestore.
*   **What Breaks if Removed:** Alarms fired by the system will have no listener; scheduling will fail, and schedules will not survive a device reboot.

### 14. AudioManager
*   **Category:** System Service API
*   **Purpose:** Modifies device volume and ringer profiles.
*   **Why Chosen:** Used to toggle the phone ringer mode to Silent and back to Normal.
*   **Where Used:** `core/scheduler/SilentModeReceiver.kt`.
*   **How it Works:** Obtains context-level audio system access and modifies `ringerMode` to `RINGER_MODE_SILENT` or `RINGER_MODE_NORMAL`.
*   **Interactions:** BroadcastReceiver.
*   **What Breaks if Removed:** The app cannot change the ringer status of the device, breaking the primary silent mode scheduler function.

### 15. NotificationManager
*   **Category:** System Service API
*   **Purpose:** Displays local notifications to notify users about silent mode activations, restorations, or errors.
*   **Why Chosen:** Standard way to provide transparent feedback about background actions.
*   **Where Used:** `core/scheduler/SilentModeReceiver.kt` and `MainScreen.kt` (to inspect DND permission).
*   **How it Works:** Builds a notification channel and posts a notification to the system tray. Uses `notificationManager.isNotificationPolicyAccessGranted` to check DND access.
*   **Interactions:** BroadcastReceiver.
*   **What Breaks if Removed:** Users will not know if silent mode succeeded or failed due to permission issues while the app was closed.

### 16. Do Not Disturb (Notification Policy Access)
*   **Category:** Android System Permission
*   **Purpose:** Allows third-party apps to change the ringer and DND settings (necessary on Android 6.0+ to set silent mode).
*   **Why Chosen:** Security constraint enforced by Android. Without it, setting `ringerMode` to silent throws a security crash.
*   **Where Used:** Checked in `MainScreen.kt` and `SilentModeReceiver.kt`.
*   **How it Works:** Checks `isNotificationPolicyAccessGranted`. If missing, the app redirects the user to the system settings page using `Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS`.
*   **Interactions:** MainScreen UI, AudioManager, and BroadcastReceiver.
*   **What Breaks if Removed:** Attempting to toggle silent mode on Android 6.0+ causes the app to crash with a `SecurityException`.

### 17. Navigation Compose
*   **Category:** Navigation Library
*   **Purpose:** Manages screen transitions and routes in a single-activity Compose application.
*   **Why Chosen:** Simplifies composable routing, handles transitions, and manages navigation backstacks.
*   **Where Used:** `presentation/navigation/` (`SilentModeSchedulerNavHost.kt` and `AppRoute.kt`).
*   **How it Works:** Defines paths as string routes. Uses `navController.navigate("route")` and `composable("route")` inside a `NavHost`.
*   **Interactions:** ViewModels and Compose Screens.
*   **What Breaks if Removed:** Screen navigation breaks; screens cannot navigate, open dialogs, or return.

### 18. Dependency Injection (Manual AppContainer)
*   **Category:** Design Pattern / DI
*   **Purpose:** Manages object instantiation and scopes dependencies (Auth, Firestore, Scheduler) globally.
*   **Why Chosen:** Simple, lightweight alternative to Hilt/Dagger, providing singletons without build-time code generation.
*   **Where Used:** `di/AppContainer.kt`, `di/DefaultAppContainer.kt`, and `SilentModeSchedulerApp.kt`.
*   **How it Works:** Declares shared instances lazily. Accesses dependencies using the `Application` context in ViewModels.
*   **Interactions:** Application class, ViewModel Factories, and Screens.
*   **What Breaks if Removed:** ViewModels cannot access the repositories or schedulers, causing compile failures.

### 19. Repository Pattern
*   **Category:** Design Pattern
*   **Purpose:** Abstracts data access sources from the ViewModels, exposing clean interfaces.
*   **Why Chosen:** Keeps ViewModels independent of the database library. Allows switching from Firestore to local SQL databases without modifying the UI layer.
*   **Where Used:** `data/firebase/firestore/`.
*   **How it Works:** Defines operations (`getDocument`, `setDocument`) in an interface (`FirestoreRepository`) and implements them in `FirebaseFirestoreRepository`.
*   **Interactions:** Firestore SDK and ViewModels.
*   **What Breaks if Removed:** ViewModels would have direct imports of `FirebaseFirestore`, violating clean coding standards and architecture constraints.

### 20. Gradle, Gradle Wrapper, and Android Gradle Plugin (AGP)
*   **Category:** Build Automation Tool
*   **Purpose:** Manages project compilation, dependency downloads, configuration manifests, packaging, and APK signing.
*   **Why Chosen:** Standard build system for Android development.
*   **Where Used:** `build.gradle.kts`, `app/build.gradle.kts`, `settings.gradle.kts`, and `gradle/wrapper/`.
*   **How it Works:** Resolves libraries from repositories, compiles Kotlin/Java files, processes resources, and packages everything into an APK. The wrapper guarantees identical build environments.
*   **Interactions:** Android Studio, JDK, and Android SDK.
*   **What Breaks if Removed:** Cannot compile, sync dependencies, or build the application.

---

## Part 2: Layered Project Architecture

The application is structured into distinct, decoupled architectural layers.

```
┌────────────────────────────────────────────────────────┐
│                        UI LAYER                        │
│ (Compose Screens: Login, Register, Main, Dialogs)      │
└───────────▲────────────────────────────────▲───────────┘
            │                                │
            │ Observes State                 │ Sends Events
            │                                │
┌───────────▼────────────────────────────────▼───────────┐
│                     VIEWMODEL LAYER                    │
│ (LoginViewModel, RegisterViewModel, MainViewModel, etc)│
└───────────▲────────────────────────────────▲───────────┘
            │                                │
            │ Calls Methods                  │ Returns Data
            │                                │
┌───────────▼────────────────────────────────▼───────────┐
│                    REPOSITORY LAYER                    │
│ (FirestoreRepository, FirebaseFirestoreRepository)     │
└───────────▲────────────────────────────────▲───────────┘
            │                                │
            │ Performs I/O                   │ Emits Updates
            │                                │
┌───────────▼────────────────────────────────▼───────────┐
│                     FIREBASE LAYER                     │
│ (FirebaseAuthManager, Firestore SDK, Offline Cache)    │
└────────────────────────────────────────────────────────┘
```

### 1. UI Layer
*   **Responsibility:** Renders the screen, handles animations, displays states, and captures user input.
*   **Files Involved:**
    *   `presentation/screens/auth/LoginScreen.kt`
    *   `presentation/screens/auth/RegisterScreen.kt`
    *   `presentation/screens/auth/OtpVerificationScreen.kt`
    *   `presentation/screens/main/MainScreen.kt`
    *   `presentation/screens/timeperiod/TimePeriodDialog.kt`
    *   `presentation/screens/userselection/UserSelectionScreen.kt`
    *   `presentation/screens/splash/SplashScreen.kt`
    *   `presentation/screens/common/ScreenScaffold.kt`
*   **Data Flow:** Collects UI State updates from ViewModels. Emits user interaction events (e.g., clicking "Add Time Period") back to the ViewModels.
*   **Interaction with other layers:** Interacts directly with the Navigation Layer (to navigate routes) and the ViewModel Layer (to obtain states).

### 2. Navigation Layer
*   **Responsibility:** Configures routing paths, animations, transitions, and handles parameters between screens.
*   **Files Involved:**
    *   `presentation/navigation/AppRoute.kt` (Defines string routes)
    *   `presentation/navigation/RegistrationNavKeys.kt` (Navigation arguments keys)
    *   `presentation/navigation/SilentModeSchedulerNavHost.kt` (Orchestrates target routes)
*   **Data Flow:** Forwards arguments (such as `verificationId` and `phoneNumber`) to screen composition trees.
*   **Interaction with other layers:** Interacts with the UI Layer to handle screen changes.

### 3. ViewModel Layer
*   **Responsibility:** Holds UI-related states, executes validations, launches async tasks, and maps domain outputs to display structures.
*   **Files Involved:**
    *   `presentation/viewmodel/SplashViewModel.kt`
    *   `presentation/viewmodel/UserSelectionViewModel.kt`
    *   `presentation/viewmodel/LoginViewModel.kt`
    *   `presentation/viewmodel/RegisterViewModel.kt`
    *   `presentation/viewmodel/OtpVerificationViewModel.kt`
    *   `presentation/viewmodel/MainViewModel.kt`
    *   `presentation/viewmodel/TimePeriodViewModel.kt`
*   **Data Flow:** Listens to repository calls. Mutates `MutableStateFlow` models and exposes them as read-only `StateFlow` structures.
*   **Interaction with other layers:** Connects the UI Layer (input/output) to the Repository and Scheduler layers.

### 4. Repository Layer
*   **Responsibility:** Provides clean data interfaces for the ViewModel layer, shielding the rest of the application from database details.
*   **Files Involved:**
    *   `data/firebase/firestore/FirestoreRepository.kt` (Interface)
    *   `data/firebase/firestore/FirebaseFirestoreRepository.kt` (Implementation)
    *   `data/model/TimePeriod.kt` (Data model)
*   **Data Flow:** Maps Firebase raw snapshots into clean Kotlin data classes (e.g. `TimePeriod`) and returns `FirebaseResult` status outcomes.
*   **Interaction with other layers:** Called by ViewModels; communicates directly with the Firebase Cloud APIs.

### 5. Firebase Layer
*   **Responsibility:** Handles secure authentication sessions, data storage, and local offline database cache updates.
*   **Files Involved:**
    *   `data/firebase/auth/FirebaseAuthManager.kt`
    *   `data/firebase/auth/PhoneOtpSession.kt`
    *   `core/firebase/FirebaseResult.kt` / `FirebaseFailure.kt` / `FirebaseErrorMapper.kt`
*   **Data Flow:** Sends network requests to Firebase services and parses raw SDK outputs.
*   **Interaction with other layers:** Interacts directly with the Repository layer.

### 6. Scheduler Layer
*   **Responsibility:** Coordinates wake events with the Android OS to trigger silent mode automation at scheduled times.
*   **Files Involved:**
    *   `core/scheduler/SilentModeScheduler.kt` (Schedules/Cancels alarms)
    *   `core/scheduler/SilentModeReceiver.kt` (Executes ringer updates)
*   **Data Flow:** Places target properties (times, schedules) inside system intent extras. Reads these fields upon receiver wake triggers.
*   **Interaction with other layers:** Interacts with System Services (AlarmManager) and the database (to reload schedules on system boot).

### 7. System Services Layer
*   **Responsibility:** Interacts with physical hardware constraints and device ringer volumes.
*   **Files Involved:**
    *   Android OS Services (`AudioManager`, `AlarmManager`, `NotificationManager`).
*   **Data Flow:** Modifies device hardware status directly.
*   **Interaction with other layers:** Triggered by the Scheduler Layer's `SilentModeReceiver`.

---

## Part 3: Text Workflow Diagrams

### 1. User Authentication & Database Synchronization Flow
```
  [User Action]
        │  Inputs details (Phone/OTP)
        ▼
  [Compose UI Screen] (Login/Register/OTP Screen)
        │  Calls ViewModel actions
        ▼
  [ViewModel] (LoginViewModel/OtpVerificationViewModel)
        │  Triggers async operations
        ▼
  [FirebaseAuthManager] (Firebase Authentication SDK)
        │  Authenticates credential tokens
        ▼
  [Firestore Repository] (Firebase Firestore SDK)
        │  Saves/Creates user configuration
        ▼
  [Firebase Cloud / Disk Cache]
        │  Returns transaction status (Success/Failure)
        ▼
  [ViewModel]
        │  Updates UI State flows
        ▼
  [Compose UI Screen] (Recomposes view / Redirects routes)
```

### 2. Automatic Silent Mode Scheduling Lifecycle
```
  [User Adds Time Period]
        │
        ▼
  [TimePeriodViewModel] (Calls Firestore to save item)
        │
        ▼
  [SilentModeScheduler] (Creates PendingIntents for Start/End times)
        │
        ▼
  [AlarmManager] (Registers exact wake alerts with the OS)
        │
        │  Time matches scheduled period
        ▼
  [Android OS] (Wakes device, fires broadcast event)
        │
        ▼
  [SilentModeReceiver] (Wakes from closed state, receives intent)
        │
        ├─► [DND Permission Check] (If denied: Posts warning notification)
        │
        ├─► [AudioManager] (If approved: Toggles ringer to Silent/Normal)
        │
        ├─► [NotificationManager] (Displays local status tray alert)
        │
        └─► [SilentModeScheduler] (Reschedules the alarm for the next day)
```

---

## Part 4: Technical Summary Table

| Technology | Purpose | Where Used | Files/Packages | Why Used | Alternative Technologies |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Kotlin** | primary programming language | All source files | `com.example.silentmodescheduler.*` | Modern, null-safe, coroutine-native, standard for Android development | Java |
| **Jetpack Compose** | Declarative UI layout builder | Screen presentation layer | `presentation/screens/`, `presentation/ui/` | Simplifies UI code, eliminates XML boilerplate | XML Layouts / View System |
| **Material Design 3** | Visual UI component styling | Themes and UI screens | `presentation/ui/theme/`, screen files | Provides sleek UI elements and color schemes | Material 2, custom Canvas drawing |
| **ViewModel** | Lifecycle-aware data state holder | Screen presentation logic | `presentation/viewmodel/` | Retains state across configuration changes (rotation) | Presenters (MVP), custom controllers |
| **StateFlow** | Reactive state stream | ViewModel state updates | `presentation/viewmodel/`, `presentation/screens/` | Standard Kotlin flow for updating UI upon data changes | LiveData, RxJava Observables |
| **Coroutines** | Async non-blocking operations | Repositories, ViewModels | ViewModels and database files | Executes I/O tasks off the main thread to avoid UI freezes | Threads, RxJava, Executors |
| **Firebase Auth** | Passwordless identity provider | Auth operations | `data/firebase/auth/FirebaseAuthManager.kt` | Provides OTP validation services | Custom OAuth servers, Auth0 |
| **Firebase Firestore** | NoSQL cloud database | Schedule storage | `data/firebase/firestore/` | Handles real-time syncing and offline disk caching | SQLite, Room Database, Realm |
| **AlarmManager** | Exact wake event scheduler | Background scheduler | `core/scheduler/SilentModeScheduler.kt` | Triggers background code at exact times even when app is closed | WorkManager (inexact), JobScheduler |
| **BroadcastReceiver** | Listens to system wake signals | Background scheduler | `core/scheduler/SilentModeReceiver.kt` | Catches alarm triggers and boot signals from the OS | Services, custom system listeners |
| **AudioManager** | Ringer profile controller | Silent automation | `core/scheduler/SilentModeReceiver.kt` | Modifies hardware ringer profiles programmatically | Custom volume stream adjustments |
| **NotificationManager**| Posts system tray notifications | Background notifications | `core/scheduler/SilentModeReceiver.kt` | Displays status alerts when silent mode changes | Dialogs, Snackbars |
| **Navigation Compose** | Routing between screens | Screen navigation | `presentation/navigation/` | Handles navigation backstack and route arguments | FragmentManager, Navigation Component XML |
| **AppContainer** | Manual dependency resolver | Dependency Injection | `di/AppContainer.kt`, `di/DefaultAppContainer.kt` | Lightweight DI container that avoids compile-time generation | Hilt, Dagger 2, Koin |
| **Gradle** | Build compiler automation | Build configuration files | `build.gradle.kts`, `app/build.gradle.kts` | Manages compilations, plugins, and dependencies | Maven, Ant |
