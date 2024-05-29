/**
 * MIT License
 *
 * Copyright (c) [2024] [Laurits Kure]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package dk.itu.moapd.copenhagenbuzz.laku.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.laku.R

/**
 * An activity class with methods to manage logging in to the application.
 */
class LoginActivity : AppCompatActivity() {
    /**
     * This object launches a new activity and receives back some result data.
     */
    private val signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result ->
        onSignInResult(result)
    }

    /**
     * Called when the activity is starting.
     * For initialization. Calls a method that creates sign-in intent.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this Bundle contains the data it most recently supplied in `onSaveInstanceState()`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createSignInIntent()
    }

    /**
     * This method uses FirebaseUI to create a login activity with four sign-in/sign-up options,
     * (1) by e-mail
     * (2) by phone number
     * (3) by Google account.
     * (4) as guest (anonymous)
     */
    private fun createSignInIntent() {
        // Choose authentication providers.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.AnonymousBuilder().build())

        // Create and launch sign-in intent.
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setLogo(R.drawable.baseline_local_firebase)
            .setTheme(R.style.Theme_CopenhagenBuzz)
            .apply{
                setTosAndPrivacyPolicyUrls(
                    "https://firebase.google.com/terms/",
                    "https://firebase.google.com/policies/…"
                )
            }.build()
        signInLauncher.launch(signInIntent)
    }

    /**
     * When the second activity finishes it returns a result to this activity. If the user
     * signs into the application correctly, we redirect the user to the main activity of
     * the application.
     *
     * @param result Describes that the caller can launch authentication flow with an
     *      `Intent` and is guaranteed to receive a `FirebaseAuthUIAuthenticationResult` as
     *      result.
     */
    private fun onSignInResult(
        result: FirebaseAuthUIAuthenticationResult
    ) {
        when (result.resultCode){
            RESULT_OK -> {
                // Successfully signed in.
                showSnackBar("User logged in the app.")
                startMainActivity()
            } else -> {
            // Sign in failed.
            showSnackBar("Authentication failed.")
            }
        }
    }

    /**
     * In the case of a successful login, this method starts the main activity and the Firebase
     * Authentication application.
     */
    private fun startMainActivity() {
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    /**
     * Standard snackbar method.
     *
     * @param message The string that will be displayed to the user.
     */
    private fun showSnackBar(message: String) {
        Snackbar.make(
            window.decorView.rootView, message, Snackbar.LENGTH_SHORT
        ).show()
    }
}