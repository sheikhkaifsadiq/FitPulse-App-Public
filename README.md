# 🏥 FitPulse: Offline-First Mobile Health Application

<div align="center">
  <img src="https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Android_SDK-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/SQLite_(Room)-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room SQLite" />
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase" />
</div>

<br />

FitPulse is a mobile fitness tracking client designed to solve a critical real-world problem: tracking applications frequently lock up or lose user metrics when operating in low-connectivity areas (like basement gyms or remote running trails). FitPulse implements an offline-first state machine to ensure zero data loss.

---

> ### 🔒 Security & Intellectual Property Note
> This repository is a public UI/UX and architectural demo showcasing Compose layout structures and Room database models. **To protect proprietary workout indexing algorithms and user authentication secrets, the live synchronization handlers and Firebase API credentials are kept in a secure, private repository.** The local client layouts, state managers, and database schemas are open-sourced here for structural review.

---

## ✨ Features & Capabilities

*   **📱 Jetpack Compose Declarative UI**
    *   Responsive health dashboard displaying active minutes, steps, and hydration logs.
    *   Unidirectional data flows via structured ViewModels to guarantee screen states match underlying data.
*   **💾 Robust SQLite (Room) Caching**
    *   Writes all user metrics to a local SQLite cache instantly, avoiding network dependency.
*   **🔄 Automated Sync Protocol**
    *   Detects internet connection recovery in the background and batches data updates securely.
*   **🎨 Material 3 Design**
    *   Modern design system with fluid layout animations and micro-interaction states.

---

## 🛠️ Mobile Architecture Layers

| Layer | Component | Purpose |
| :--- | :--- | :--- |
| **Presentation** | Jetpack Compose + ViewModels | Drives interactive screen states and state flows. |
| **Domain** | Use Cases | Contains core fitness metrics logic, separating business rules from UI. |
| **Data** | Room DB + Repository | Manages local caching and serves as the single source of truth. |

---

## 📐 Offline-First Synchronization State Flow

```mermaid
stateDiagram-v2
    [*] --> ActiveMetrics : User Logs Activity
    ActiveMetrics --> LocalCache : Write to Room DB (Instant)
    LocalCache --> SyncWorker : Trigger Background Check
    state SyncWorker {
        [*] --> CheckInternet
        CheckInternet --> NetworkOffline : Offline
        CheckInternet --> NetworkOnline : Online
        NetworkOffline --> CheckInternet : Retrying
        NetworkOnline --> BatchUpload : Sync to Firebase
    }
    BatchUpload --> LocalCache : Mark Records as Synced
    LocalCache --> [*]
```

---

## ⚙️ Project Structure Review

Open this project in Android Studio to review:
*   `/ui`: Compose layout systems, themes, and screens.
*   `/data`: Local Room Database definitions and entity configurations.
*   `/viewmodel`: Clean UI state flows with structured coroutine dispatchers.
