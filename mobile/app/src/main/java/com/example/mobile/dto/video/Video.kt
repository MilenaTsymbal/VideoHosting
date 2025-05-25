package com.example.mobile.dto.video

data class Video(
    val _id: String,
    val title: String,
    val videoUrl: String,
    val posterUrl: String,
    val author: String,
    val createdAt: String,
    val likes: List<String>,
    val dislikes: List<String>,
)
