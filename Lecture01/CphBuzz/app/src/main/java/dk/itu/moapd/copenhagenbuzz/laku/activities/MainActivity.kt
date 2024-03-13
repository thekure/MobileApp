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
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.laku.fragments.UserInfoDialogFragment

/**
 * An activity class with methods to manage the main activity of the application.
 */
class MainActivity : AppCompatActivity() {

    /**
     * View binding is a feature that allows you to more easily write code that interacts with
     * views. Once view binding is enabled in a module, it generates a binding class for each XML
     * layout file present in that module. An instance of a binding class contains direct references
     * to all views that have an ID in the corresponding layout.
     * - This text was written by Fabricio Narcizo
     */
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth

    /**
     * A set of private constants used in this class.
     * - This text was written by Fabricio Narcizo
     */
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }


    /**
     * Called when the activity is starting. This is where most initialization should go: calling
     * `setContentView(int)` to inflate the activity's UI, using `findViewById()` to
     * programmatically interact with widgets in the UI, calling
     * `managedQuery(android.net.Uri, String[], String, String[], String)` to retrieve cursors for
     * data being displayed, etc.
     *
     * You can call `finish()` from within this function, in which case `onDestroy()` will be
     * immediately called after `onCreate()` without any of the rest of the activity lifecycle
     * (`onStart()`, `onResume()`, onPause()`, etc) executing.
     *
     * <em>Derived classes must call through to the super class's implementation of this method. If
     * they do not, an exception will be thrown.</em>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this Bundle contains the data it most recently supplied in `onSaveInstanceState()`.
     * <b><i>Note: Otherwise it is null.</i></b>
     * - This text was written by Fabricio Narcizo
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window,false)
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavMenuAndTopBar()
        setListeners()
    }

    override fun onStart() {
        super.onStart()

        // Redirect the user to the LoginActivity if they are not logged in.
        auth.currentUser ?: startLoginActivity()
    }

    /**
     * Inflates top_app_bar and decides whether or not to show login or logout button.
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        menu.findItem(R.id.accounts_button).isVisible =
        !intent.getBooleanExtra("isLoggedIn", false)
        menu.findItem(R.id.logout_button).isVisible =
        intent.getBooleanExtra("isLoggedIn", false)
        return true
    }

    /**
     * Redirects to login if not already logged in
     */
    private fun startLoginActivity() {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let(::startActivity)
    }


    /**
     * Sets up the user interface components by attaching relevant listeners to the
     * necessary components.
     */
    private fun setListeners() {
        with(binding.topAppBar){
            setOnMenuItemClickListener{menuItem ->
                when (menuItem.itemId) {
                    R.id.accounts_button -> {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                        true
                    }

                    R.id.logout_button -> {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                        true
                    }

                    else -> false
                }
            }
        }
    }

    /**
     * - Defines a NavHostFragment
     * - Uses that to instantiate a NavController
     * - Uses that to set up bottom_navigation_menu
     * - Makes sure the top_app_bar is configured with the NavController
     */
    private fun initNavMenuAndTopBar() {
        // Bottom Navigation:
        val navHostFragment = supportFragmentManager
            .findFragmentById(
                R.id.fragment_container_view
            ) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        // Top Bar:
        setSupportActionBar(binding.topAppBar)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Standard function for hiding the keyboard. Imported from java.
     */
    private fun hideKeyboard(){
        val imm = getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment_container_view)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        // Handle top app bar menu item clicks.
        R.id.action_user_info -> {
            UserInfoDialogFragment().apply {
                isCancelable = false
            }.also { dialogFragment ->
                dialogFragment.show(supportFragmentManager,
                    "UserInfoDialogFragment")
            }
            true
        }
        R.id.action_logout -> {
            auth.signOut()
            startLoginActivity()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}















