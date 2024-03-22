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
    private val signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result ->
        onSignInResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createSignInIntent()
    }

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

    private fun startMainActivity() {
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    /**
     * Standard snackbar message, copied directly from course GitHub
     */
    private fun showSnackBar(message: String) {
        Snackbar.make(
            window.decorView.rootView, message, Snackbar.LENGTH_SHORT
        ).show()
    }
}