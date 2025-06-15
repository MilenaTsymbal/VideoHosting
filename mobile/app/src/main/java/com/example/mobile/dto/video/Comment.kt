package com.example.mobile.dto.video

import com.example.mobile.dto.user.User

data class Comment(
    val _id: String,
    val text: String,
    val author: User,
    val createdAt: String,
    val updatedAt: String
)