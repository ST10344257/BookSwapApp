plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.example.bookswap"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.bookswap"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

// All dependencies are now in a single, organized block.
dependencies {

    // --- CORE ANDROID & UI ---
    implementation(libs.androidx.core.ktx) // Use the version from libs
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.viewpager2)
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // --- ACTIVITY, FRAGMENT, LIFECYCLE (KTX) ---
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation("androidx.work:work-runtime-ktx:2.9.0") // For background tasks

    // --- COROUTINES ---
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // --- FIREBASE ---
    // Define the BoM platform ONLY ONCE. This manages all Firebase dependency versions.
    implementation(platform(libs.firebase.bom))

    // Now add the individual SDKs you need without specifying versions.
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation("com.google.firebase:firebase-messaging") // For push notifications
    implementation("com.google.android.gms:play-services-auth:21.4.0") // For Google Sign-In

    // --- NETWORKING (Retrofit) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // Useful for debugging

    // --- IMAGE LOADING ---
    implementation(libs.glide)

    // --- OTHER LIBS ---
    implementation(libs.gson)
    implementation(libs.imagepicker)

    // --- TESTING ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
