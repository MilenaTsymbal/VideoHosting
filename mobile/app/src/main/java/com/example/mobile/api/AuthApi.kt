package com.example.mobile.api

import com.example.mobile.dto.auth.LoginDto
import com.example.mobile.dto.auth.LoginResponse
import com.example.mobile.dto.auth.RegisterDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("Auth/register")
    fun register(@Body registerDto: RegisterDto): Call<ResponseBody>

    @POST("Auth/login")
    fun login(@Body loginDto: LoginDto): Call<LoginResponse>
}