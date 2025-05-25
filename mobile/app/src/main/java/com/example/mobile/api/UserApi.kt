package com.example.mobile.api

import com.example.mobile.dto.auth.RegisterDto
import com.example.mobile.dto.user.NotificationsResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserApi {

    @GET("users/me/notifications")
    fun getNotifications(@Header("Authorization") token: String): Call<List<NotificationsResponse>>
}