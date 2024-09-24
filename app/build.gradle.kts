plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.mertaltun.tetheringservice"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mertaltun.tetheringservice"
        minSdk = 29
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    val core_version = "1.3.2"

    // Java language implementation
    implementation("androidx.core:core:$core_version")
    // Kotlin
    implementation("androidx.core:core-ktx:$core_version")

    // To use RoleManagerCompat
    implementation("androidx.core:core-role:1.0.0")

    // To use the Animator APIs
    implementation("androidx.core:core-animation:1.0.0")
    // To test the Animator APIs
    androidTestImplementation("androidx.core:core-animation-testing:1.0.0")

    // Optional - To enable APIs that query the performance characteristics of GMS devices.
    implementation("androidx.core:core-performance:1.0.0")

    // Optional - to use ShortcutManagerCompat to donate shortcuts to be used by Google
    implementation("androidx.core:core-google-shortcuts:1.1.0")

    // Optional - to support backwards compatibility of RemoteViews
    implementation("androidx.core:core-remoteviews:1.1.0")

    // For background job
    implementation (libs.androidx.work.runtime.ktx)


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.ktx.v190)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}