package com.example.mobile.dto.user

import com.example.mobile.dto.video.Video

data class NotificationsResponse(
    val _id: String,
    val user: String,
    val type: String,
    val fromUser: User,
    val video: Video,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
)


