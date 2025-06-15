package com.example.mobile.dto.user

import com.example.mobile.dto.video.Video

data class ChannelResponse(
    val user: ChannelUser,
    val videos: List<Video>
)
