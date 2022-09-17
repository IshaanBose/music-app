package com.example.musicapp

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson

class SignUpActivity : AppCompatActivity() {
    val passwordRegex: Regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*-+=_<>?`~])[A-Za-z\\d!@#\$%^&*-+=_<>?`~]{8,}\$")
    val spUserDetails = "com.musicapp.user_details"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val nightModeFlags = baseContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val logo: ImageView = findViewById(R.id.logo)
        val signUpButton: MaterialButton = findViewById(R.id.sign_up)
        val usernameTextView: TextInputEditText = findViewById(R.id.username)
        val passwordTextView: TextInputEditText = findViewById(R.id.password)
        val confirmPasswordTextView: TextInputEditText = findViewById(R.id.conf_password)

        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                logo.setImageResource(R.drawable.login_img2_white)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                logo.setImageResource(R.drawable.login_img2)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> Log.d("custtest", "NIGHT HUH?")
        }

        signUpButton.setOnClickListener(View.OnClickListener {
            var signUp = true
            val username = usernameTextView.text?.trim().toString()
            val password = passwordTextView.text.toString()
            val confPassword = confirmPasswordTextView.text.toString()

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

            if (confPassword.isEmpty()) {
                signUp = false
                confirmPasswordTextView.error = "Cannot be empty!"
            } else if (confPassword != password) {
                signUp = false
                confirmPasswordTextView.error = "Password does not match!"
            }

            if (signUp) {
                val sp = getSharedPreferences(spUserDetails, MODE_PRIVATE)

                if (sp.getString(username, "null") != "null") {
                    usernameTextView.error = "Username already exists!"
                } else {
                    val spEditor = sp.edit()
                    spEditor.putString(username, password)
                    spEditor.apply()

                    Toast.makeText(baseContext, "Account created!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        })
    }
}