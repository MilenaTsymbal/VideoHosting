package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobile.api.RetrofitClient
import com.example.mobile.databinding.ActivityRegisterBinding
import com.example.mobile.dto.auth.RegisterDto
import es.dmoral.toasty.Toasty
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()

        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        _binding.registerButton.setOnClickListener {
            register()
        }
    }

    private fun register() {

        val registerDto = RegisterDto(
            username = _binding.usernameEditText.text.toString(),
            email = _binding.emailEditText.text.toString(),
            password = _binding.passwordEditText.text.toString(),
            confirmPassword = _binding.repeatPasswordEditText.text.toString(),
        )

        val authApi = RetrofitClient.getInstance().authApi

        authApi.register(registerDto).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()

                    Toasty.success(
                        this@RegisterActivity,
                        "You registered successfully!",
                        Toast.LENGTH_SHORT,
                        true
                    ).show()
                    Log.d("Register", "User successfully registered: $registerResponse")

                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    Toasty.error(
                        this@RegisterActivity,
                        "Response failed: ${response}",
                        Toast.LENGTH_SHORT,
                        true
                    ).show()
                    Log.e("Register", "Response failed: ${response}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toasty.error(
                    this@RegisterActivity,
                    "Response failed with code: ${t.message}",
                    Toast.LENGTH_SHORT,
                    true
                ).show()
                Log.e("Register", "Failed: ${t.message}")
            }
        })
    }
}