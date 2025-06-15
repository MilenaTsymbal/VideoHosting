package com.example.mobile.ui.video

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.mobile.R
import com.example.mobile.api.RetrofitClient
import com.example.mobile.dto.video.ShareVideoRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShareVideoDialog(
    context: Context,
    private val videoId: String,
    private val onSuccess: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_share_video)

        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val shareButton = findViewById<Button>(R.id.shareButton)

        shareButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            shareVideo(username)
        }
    }

    private fun shareVideo(username: String) {
        val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("token", null) ?: return

        val shareRequest = ShareVideoRequest(toUserId = username)

        RetrofitClient.getInstance().videoApi.shareVideo(videoId, shareRequest, token)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Video shared successfully!", Toast.LENGTH_SHORT)
                            .show()
                        onSuccess()
                        dismiss()
                    } else {
                        val errorMessage = try {
                            response.errorBody()?.string() ?: "Failed to share video"
                        } catch (e: Exception) {
                            "Failed to share video"
                        }
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}