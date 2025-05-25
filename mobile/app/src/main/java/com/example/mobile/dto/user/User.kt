package com.example.mobile.dto.user

data class User(
    val _id: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String,
    val avatarUrl: String,
    val subscriptions: List<String>,
    val followers: List<String>,
)
