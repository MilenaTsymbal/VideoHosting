package com.example.mobile.dto.user

import android.os.Parcel
import android.os.Parcelable

data class User(
    val _id: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String,
    val avatarUrl: String,
    val subscriptions: List<String>,
    val followers: List<String>,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        _id = parcel.readString() ?: "",
        username = parcel.readString() ?: "",
        email = parcel.readString() ?: "",
        password = parcel.readString() ?: "",
        role = parcel.readString() ?: "",
        avatarUrl = parcel.readString() ?: "",
        subscriptions = parcel.createStringArrayList() ?: emptyList(),
        followers = parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_id)
        parcel.writeString(username)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(role)
        parcel.writeString(avatarUrl)
        parcel.writeStringList(subscriptions)
        parcel.writeStringList(followers)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User = User(parcel)
        override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
    }
}
