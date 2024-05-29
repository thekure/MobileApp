package dk.itu.moapd.copenhagenbuzz.laku

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger
import io.github.cdimascio.dotenv.dotenv
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

val DATABASE_URL: String = dotenv {
    directory = "/assets"
    filename = "env"
}["DATABASE_URL"] ?: error("DATABASE_URL not found in assets/env file")

val BUCKET_URL: String = dotenv {
    directory = "/assets"
    filename = "env"
}["BUCKET_URL"] ?: error("BUCKET_URL not found in assets/env file")

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)


        // Enable disk persistence for the Firebase Realtime Database and keep it synchronized.
        Firebase.database(DATABASE_URL).setPersistenceEnabled(true)
        Firebase.database(DATABASE_URL).reference.keepSynced(true)
    }
}