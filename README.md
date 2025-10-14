# 📚 BookSwap - College Android Application

**BookSwap** is an Android application developed as a **university project**.  
It serves as a **mobile marketplace** for students to buy and sell used textbooks — fostering a community of exchange and making educational resources more affordable.

The platform includes features such as user authentication, book listings, profile management, a shopping cart, and a secure transaction system.  
This project is built using **modern Android development practices** with **Kotlin**, **Firebase**, and the **MVVM (Model-View-ViewModel)** architecture.

---

## 🧭 Table of Contents
1. [Core Features](#core-features)  
2. [Technology Stack & Architecture](#technology-stack--architecture)  
3. [Getting Started: Setup Instructions](#getting-started-setup-instructions)  
   - [Prerequisites](#prerequisites)  
   - [Firebase Setup](#firebase-setup)  
   - [Configuration](#configuration)  
4. [Firebase Services Used](#firebase-services-used)  
5. [App Architecture](#app-architecture)  
   - [Project Structure](#project-structure)  
   - [Key Architectural Decisions](#key-architectural-decisions)  
6. [Screenshots](#screenshots)  
7. [Dependencies](#dependencies)

---

## ⚙️ Core Features

- **User Authentication:**  
  Secure sign-up and login functionality using Firebase Authentication (Email & Password).  

- **Book Listings:**  
  Users can create, view, update, and delete book listings.  
  Each listing includes details such as title, author, price, category, and image.

- **Profile Management:**  
  Users can view and update their profile information, including their name, email, and profile picture.

- **Image Uploads:**  
  Seamless upload of book covers and profile pictures using Firebase Cloud Storage.

- **Dynamic Home Page:**  
  Displays available books, personalized user greetings, and filtering capabilities.

- **Search and Filter:**  
  Search for books by title or author, and filter listings by category (e.g., Tech, Law, Business).

- **Shopping Cart:**  
  Add books to a cart for a streamlined checkout process.

- **Transaction System:**  
  A Firestore-backed system to manage the full purchase lifecycle from pending to completed.

---

## 🧱 Technology Stack & Architecture

| Category | Technology |
|-----------|-------------|
| **Language** | Kotlin (100%) |
| **Architecture** | MVVM (Model–View–ViewModel) |
| **UI** | XML Layouts with ViewBinding |
| **Backend** | Firebase |
| **Database** | Cloud Firestore |
| **Authentication** | Firebase Authentication |
| **File Storage** | Firebase Cloud Storage |
| **Security** | Firebase App Check (Play Integrity) |
| **Async Handling** | Kotlin Coroutines |
| **Image Loading** | Glide |
| **Dependency Management** | Gradle |

---

## 🚀 Getting Started: Setup Instructions

To run this project locally for development and testing, follow these steps.

### ✅ Prerequisites
- [Android Studio](https://developer.android.com/studio) (latest stable version)
- An **Android Emulator** or **physical Android device**
- A **Google Account** to create a Firebase project

---

## 🔥 Firebase Setup

This project is tightly integrated with Firebase.  
You must create your own Firebase project to run the app.

### 1. Create a Firebase Project
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click on **“Add project”** and follow the setup steps.

### 2. Add an Android App to Your Firebase Project
1. In your project dashboard, click the **Android icon** to register your app.  
2. The **Package Name** must be:
com.example.bookswap

markdown
Copy code
⚠️ *This is critical.*
3. You can skip adding the SHA-1 fingerprint for now (needed only for Google Sign-In).

### 3. Download and Add `google-services.json`
- After registering the app, Firebase will provide a **google-services.json** file.
- Place this file inside the `app/` directory of your project.

### 4. Enable Firebase Services
In the Firebase Console, under **Build**, enable:
- **Authentication:**  
Go to *Authentication → Sign-in method → Enable “Email/Password”*.
- **Cloud Firestore:**  
Go to *Firestore Database → Create Database → Test Mode* (initial setup).
- **Cloud Storage:**  
Go to *Storage → Create Bucket → Test Mode*.

---

## ⚙️ Configuration

### 1. Firestore Indexes

The app performs composite queries that require indexes.  
If they’re missing, the app will crash (Logcat will provide a link to create them).

#### Required Indexes
| Collection ID | Field 1 | Field 2 |
|---------------|----------|----------|
| `books` | status (Ascending) | listedAt (Descending) |
| `transactions` | buyerId (Ascending) | createdAt (Descending) |
| `transactions` | sellerId (Ascending) | createdAt (Descending) |

---

### 2. App Check Setup (Highly Recommended)

To prevent “Permission Denied” errors and secure your backend, set up **App Check**.

**Steps:**
1. In the Firebase Console → *Build → App Check*.
2. Select your app (`com.example.bookswap`).
3. Click **Play Integrity** and register it.
4. Obtain a **SHA-256 fingerprint** from Android Studio:
- Open the **Gradle** panel → *bookswap → app → Tasks → android → signingReport*.
- Copy the SHA-256 fingerprint for the **debug** variant.
- Paste it into the Firebase App Check setup.
5. Set **Token TTL** to *7 days*.
6. Save and confirm.

---

## 🏗️ App Architecture

The application follows the **MVVM** pattern to separate UI logic from business logic — ensuring clean, modular, and testable code.

### 📂 Project Structure
com.example.bookswap
├── adapters/ # RecyclerView adapters
├── data/
│ ├── models/ # Data classes (User, Book, Transaction)
│ └── repository/ # Data operations (UserRepository, BookRepository, etc.)
├── ui/ # Activities and Fragments (UI layer)
├── utils/ # Utility classes and constants
├── FirebaseModule.kt # Centralized Firebase provider
└── MyApplication.kt # Initializes App Check and global configurations

yaml
Copy code

---

### 🧠 Key Architectural Decisions

- **Centralized Firebase Initialization (`FirebaseModule.kt`):**  
  Ensures consistent Firebase instances (Auth, Firestore, Storage) across the app.  
  Prevents authentication state mismatches.

- **Repository Pattern:**  
  The data layer abstracts Firestore and Storage from the rest of the app.  
  ViewModels handle data requests without worrying about implementation details.

- **Kotlin Coroutines:**  
  All async tasks (e.g., network or Firestore queries) use coroutines for simplicity and lifecycle safety.  
  This avoids “callback hell” and integrates seamlessly with `lifecycleScope`.

---

## 🖼️ Screenshots

*(Add your screenshots here once ready — e.g. Home screen, Book detail, Profile, etc.)*

---

## 📦 Dependencies

| Library | Purpose |
|----------|----------|
| **AndroidX** | Core Android components (`appcompat`, `core-ktx`, `constraintlayout`, `recyclerview`) |
| **Material Components** | Modern Material Design widgets |
| **Firebase SDK** | `firebase-bom`, `firebase-auth`, `firebase-firestore`, `firebase-storage`, `firebase-appcheck` |
| **Kotlin Coroutines** | Asynchronous programming |
| **Glide** | Efficient image loading and caching |

---

### 🏁 End Note

BookSwap demonstrates modern Android app development principles — blending Firebase integration, MVVM architecture, and Kotlin best practices to create a scalable and user-friendly marketplace for students.

---

**Developed by:** *[Nkosinathi Mabena, Barefile Lephoto, Lesego Letsapa, Lufuno Dagada]*  
📧 *[ST10344257@rcconnect.edu.za]*  
🎓 *For University Project Use*
