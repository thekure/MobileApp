plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "dk.itu.moapd.copenhagenbuzz.laku"
    compileSdk = 34

    defaultConfig {
        applicationId = "dk.itu.moapd.copenhagenbuzz.laku"
        minSdk = 26
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    // TO DO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    // implementation("com.google.firebase:firebase-analytics")
    implementation("com.firebaseui:firebase-ui-auth:8.0.0")
    // Updating this past 20.5.0 messes with Firebase as of 15/03/2024
    implementation("com.google.android.gms:play-services-auth:20.5.0")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    // FirebaseUI for Firebase Realtime Database
    implementation ("com.firebaseui:firebase-ui-database:8.0.2")

    // Dotenv to access secrets
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // Other stuff
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.github.javafaker:javafaker:1.0.2")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}