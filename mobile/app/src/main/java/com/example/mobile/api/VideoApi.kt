package com.example.mobile.api

import com.example.mobile.dto.video.Comment
import com.example.mobile.dto.video.CommentRequest
import com.example.mobile.dto.video.CommentsResponse
import com.example.mobile.dto.video.LikeDislikeResponse
import com.example.mobile.dto.video.ShareVideoRequest
import com.example.mobile.dto.video.Video
import com.example.mobile.dto.video.VideoListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface VideoApi {
    @GET("videos")
    fun listVideos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("timestamp") timestamp: Long
    ): Call<VideoListResponse>

    @GET("videos/{id}")
    fun getVideoById(@Path("id") id: String): Call<Video>

    @POST("videos/{id}/like")
    fun likeVideo(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Call<LikeDislikeResponse>

    @POST("videos/{id}/dislike")
    fun dislikeVideo(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Call<LikeDislikeResponse>

    @DELETE("videos/{id}")
    fun deleteVideo(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Call<Void>

    @GET("comments/{id}")
    fun getComments(@Path("id") id: String): Call<CommentsResponse>

    @POST("comments/{id}")
    fun addComment(
        @Path("id") id: String,
        @Body request: CommentRequest,
        @Header("Authorization") token: String
    ): Call<Comment>

    @Multipart
    @POST("videos")
    fun uploadVideo(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part video: MultipartBody.Part,
        @Part poster: MultipartBody.Part
    ): Call<ResponseBody>

    @POST("videos/{id}/share")
    fun shareVideo(
        @Path("id") videoId: String,
        @Body shareRequest: ShareVideoRequest,
        @Header("Authorization") token: String
    ): Call<ResponseBody>
}