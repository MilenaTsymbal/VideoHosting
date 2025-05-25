package com.example.mobile.api

import com.example.mobile.dto.auth.RegisterDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VideoApi {

    @Multipart
    @POST("videos")
    fun uploadVideo(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part video: MultipartBody.Part,
        @Part poster: MultipartBody.Part
    ): Call<ResponseBody>
}