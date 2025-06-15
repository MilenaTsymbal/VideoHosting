package com.example.mobile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobile.api.RetrofitClient
import com.example.mobile.databinding.ActivityLoginBinding
import com.example.mobile.databinding.FragmentNotificationCardBinding
import com.example.mobile.dto.auth.LoginDto
import com.example.mobile.dto.auth.LoginResponse
import es.dmoral.toasty.Toasty
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        enableEdgeToEdge()

        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        _binding.loginButton.setOnClickListener {
            login()
        }

        val registerTextView = findViewById<TextView>(R.id.registerTextView)
        val fullText = "Don't have an account? Register"
        val spannable = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }

        val start = fullText.indexOf("Register")
        val end = start + "Register".length

        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        registerTextView.text = spannable
        registerTextView.movementMethod = LinkMovementMethod.getInstance()
        registerTextView.highlightColor = Color.TRANSPARENT
    }

    fun login(){

        val loginDto = LoginDto(
            email = _binding.emailEditText.text.toString(),
            password = _binding.passwordEditText.text.toString(),
        )

        val authApi = RetrofitClient.getInstance().authApi

        authApi.login(loginDto).enqueue(object :
            Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()

                    val sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("token", loginResponse?.token)
                        putString("_id", loginResponse?.user?._id)
                        putString("username", loginResponse?.user?.username)
                        putString("email", loginResponse?.user?.email)
                        putString("avatarUrl", loginResponse?.user?.avatarUrl)
                        loginResponse?.token?.let { token ->
                            try {
                                val payload = token.split(".")[1]
                                val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
                                val decodedString = String(decodedBytes)
                                val jsonObject = org.json.JSONObject(decodedString)
                                val role = jsonObject.optString("role", "user")
                                putString("role", role)
                                Log.d("Login", "Saved user role: $role")
                            } catch (e: Exception) {
                                Log.e("Login", "Failed to decode JWT: ${e.message}")
                                putString("role", "user")
                            }
                        }
                        apply()
                    }

                    Toasty.success(this@LoginActivity,
                        "You logged in successfully!", Toast.LENGTH_SHORT, true).show()
                    Log.d("Login", "User successfully logged in: $loginResponse")

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage = "Login failed"
                    
                    if (!errorBody.isNullOrEmpty()) {
                        try {
                            val errorJson = JSONObject(errorBody)
                            errorMessage = errorJson.optString("message", "Login failed")
                        } catch (e: Exception) {
                            Log.e("Login", "Error parsing error response: ${e.message}")
                        }
                    }
                    
                    Toasty.error(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT, true).show()
                    Log.e("Login", "Login failed with response: $response")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toasty.error(this@LoginActivity, "Response failed"
                    , Toast.LENGTH_SHORT, true).show()
                Log.e("Login", "Failed: ${t.message}")
            }
        })
    }
}