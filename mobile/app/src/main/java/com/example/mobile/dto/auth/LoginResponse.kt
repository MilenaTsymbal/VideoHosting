package com.example.mobile.dto.auth

data class LoginResponse(
    val token: String,
    val user: User,
)

data class User(
    val _id: String,
    val username: String,
    val email: String,
    val avatarUrl: String,
)
