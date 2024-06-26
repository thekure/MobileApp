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
import dk.itu.moapd.copenhagenbuzz.laku.fragments.CreateEventDialogFragment
import dk.itu.moapd.copenhagenbuzz.laku.fragments.UserInfoDialogFragment

/**
 * An activity class with methods to manage the main activity of the application.
 */
class MainActivity : AppCompatActivity() {

    /**
     * View binding allows writing code easier. Binding classes for each XML layout file in
     * the module are generated.
     */
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth
    private lateinit var _menu: Menu


    /**
     * Called when the activity is starting.
     * For initialization.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this Bundle contains the data it most recently supplied in `onSaveInstanceState()`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window,false)
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavMenuAndTopBar()
    }


    // ------------------------------------
    // All things navigation
    // ------------------------------------

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
     * Redirects to login if not already logged in
     */
    private fun startLoginActivity() {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let(::startActivity)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment_container_view)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    // ------------------------------------
    // This section is all things Top Menu
    // ------------------------------------
    /**
     * Initializes _menu and inflates the top_app_bar
     * @param menu The menu needed to inflate the menuInflater.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        _menu = menu
        menuInflater.inflate(R.menu.top_app_bar, _menu)
        return true
    }

    /**
     * Updates what options should be visible every time the menu is accessed.
     * @param menu The menu that is updated.
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        updateOptionsMenu()
        return true
    }

    /**
     * Action listeners for each top menu option.
     * @param item The menu item that was selected by the user.
     */
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
        R.id.action_login -> {
            startLoginActivity()
            true
        }
        R.id.action_create_event -> {
            CreateEventDialogFragment().apply {
                isCancelable = true
            }.also { dialogFragment ->
                dialogFragment.show(supportFragmentManager,
                    "CreateEventDialogFragment")
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /**
     * Auxiliary function for updating menu item visibility.
     */
    private fun updateOptionsMenu() {
        val logOutButton = _menu.findItem(R.id.action_logout)
        val logInButton = _menu.findItem(R.id.action_login)
        val createEventButton = _menu.findItem(R.id.action_create_event)
        val userIsValid = !(auth.currentUser == null || auth.currentUser!!.isAnonymous)

        if(userIsValid){
            createEventButton.isVisible = true
            logOutButton.isVisible = true
            logInButton.isVisible = false
        } else {
            createEventButton.isVisible = false
            logOutButton.isVisible = false
            logInButton.isVisible = true
        }
    }
}















