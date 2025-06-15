package com.example.mobile.api

import com.example.mobile.dto.auth.RegisterDto
import com.example.mobile.dto.user.ChannelResponse
import com.example.mobile.dto.user.NotificationsResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {

    @GET("users/me/notifications")
    fun getNotifications(@Header("Authorization") token: String): Call<List<NotificationsResponse>>

    @GET("users/channel/{username}")
    fun getUserByUsername(@Path("username") username: String): Call<ChannelResponse>

    @POST("users/{id}/subscribe")
    fun subscribe(
        @Path("id") userId: String,
        @Header("Authorization") token: String
    ): Call<ResponseBody>

    @POST("users/{id}/unsubscribe")
    fun unsubscribe(
        @Path("id") userId: String,
        @Header("Authorization") token: String
    ): Call<ResponseBody>
}