package com.example.mobile.dto.user

data class ChannelUser(
    val _id: String,
    val username: String,
    val avatarUrl: String,
    val subscriptionsCount: Int,
    val followersCount: Int,
    val subscriptions: List<String>,
    val followers: List<String>
)