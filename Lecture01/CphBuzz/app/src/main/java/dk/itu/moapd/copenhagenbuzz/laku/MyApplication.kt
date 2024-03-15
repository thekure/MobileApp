package dk.itu.moapd.copenhagenbuzz.laku

import android.app.Application
import com.google.android.material.color.DynamicColors
import io.github.cdimascio.dotenv.dotenv
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

val DATABASE_URL: String = dotenv {
    directory = "./assets"
    filename = "env"
}["DATABASE_URL"]

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        // Enable disk persistence for the Firebase Realtime Database and keep it synchronized.
        Firebase.database(DATABASE_URL).setPersistenceEnabled(true)
        Firebase.database(DATABASE_URL).reference.keepSynced(true)
    }
}