package com.example.musicapp

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class StartingActivity : AppCompatActivity() {
    var contentLoaded: Boolean = false
    val passwordRegex: Regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*-+=_<>?`~])[A-Za-z\\d!@#\$%^&*-+=_<>?`~]{8,}\$")
    val spUserDetails = "com.musicapp.user_details"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starting)

        contentLoaded = false

        addDelay()
        setupSplashScreen()

        printAllUserDetails()
        supportActionBar?.hide()

        val nightModeFlags = baseContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val logo: ImageView = findViewById(R.id.logo)
        val createAccountTextView: TextView = findViewById(R.id.create_account)
        val loginButton: MaterialButton = findViewById(R.id.login)
        val usernameTextView: TextInputEditText = findViewById(R.id.username)
        val passwordTextView: TextInputEditText = findViewById(R.id.password)

        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                logo.setImageResource(R.drawable.login_img2_white)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                logo.setImageResource(R.drawable.login_img2)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> Log.d("custtest", "NIGHT HUH?")
        }

        createAccountTextView.setOnClickListener(View.OnClickListener {
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
        })

        loginButton.setOnClickListener(View.OnClickListener {
            val username = usernameTextView.text?.trim().toString()
            val password = passwordTextView.text.toString()
            var signUp = true

            if (username.isEmpty()) {
                signUp = false
                usernameTextView.error = "Cannot be empty!"
            }

            if (password.isEmpty()) {
                signUp = false
                passwordTextView.error = "Cannot be empty!"
            } else if (!passwordRegex.matches(password)) {
                signUp = false
                passwordTextView.error = "Must contain min 8 characters, at least 1 uppercase letter, 1 lowercase letter, 1 number & 1 special character."
            }

            if (signUp) {
                val sp = getSharedPreferences(spUserDetails, MODE_PRIVATE)

                if (sp.getString(username, "null") == "null") {
                    usernameTextView.error = "Username does not exist!"
                } else if (sp.getString(username, "null") != password) {
                    passwordTextView.error = "Password does not match!"
                } else {
                    val intent = Intent(baseContext, MainActivity::class.java)
                    intent.putExtra("username", username)
                    startActivity(intent)
                    finish()
                }
            }
        })
    }

    private fun addDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            contentLoaded = true
        }, 1000)
    }

    private fun setupSplashScreen() {
        val content: View = findViewById(R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object: ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (contentLoaded) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )
    }

    private fun printAllUserDetails() {
        val allEntries = getSharedPreferences(spUserDetails, MODE_PRIVATE).all
        allEntries.forEach {
            Log.d("user_details", "${it.key}: ${it.value}")
        }
    }

    override fun onResume() {
        super.onResume()
        val usernameTextView: TextInputEditText = findViewById(R.id.username)
        val passwordTextView: TextInputEditText = findViewById(R.id.password)

        usernameTextView.text!!.clear()
        passwordTextView.text!!.clear()
    }
}