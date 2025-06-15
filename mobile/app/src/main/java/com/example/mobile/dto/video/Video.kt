package com.example.mobile.dto.video

import android.os.Parcel
import android.os.Parcelable
import com.example.mobile.dto.user.User

data class Video(
    val _id: String,
    val title: String,
    val videoUrl: String,
    val posterUrl: String,
    val author: User,
    val createdAt: String,
    val likes: List<String>,
    val dislikes: List<String>,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        _id = parcel.readString() ?: "",
        title = parcel.readString() ?: "",
        videoUrl = parcel.readString() ?: "",
        posterUrl = parcel.readString() ?: "",
        author = parcel.readParcelable(User::class.java.classLoader)!!,
        createdAt = parcel.readString() ?: "",
        likes = parcel.createStringArrayList() ?: emptyList(),
        dislikes = parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_id)
        parcel.writeString(title)
        parcel.writeString(videoUrl)
        parcel.writeString(posterUrl)
        parcel.writeParcelable(author, flags)
        parcel.writeString(createdAt)
        parcel.writeStringList(likes)
        parcel.writeStringList(dislikes)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Video> {
        override fun createFromParcel(parcel: Parcel): Video = Video(parcel)
        override fun newArray(size: Int): Array<Video?> = arrayOfNulls(size)
    }
}
