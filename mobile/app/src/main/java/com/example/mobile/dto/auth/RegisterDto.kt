package com.example.mobile.dto.auth

data class RegisterDto(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
)