package com.example.noisense.service

import com.example.noisense.db.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

interface ApiService {
    @FormUrlEncoded
    @POST("add_recording")
    fun addRecording(
        @Field("audio_id") audioId: String,
        @Field("audio_title") audioTitle: String,
        @Field("audio_timestamp") audioTimestamp: String,
        @Field("audio_path") audioPath: String,
        @Field("audio_size") audioSize: Float,
        @Field("audio_duration") audioDuration: String,
        @Field("audio_label") audioLabel: String
    ): Call<Map<String, String>>
     
    @Multipart
    @POST("upload_audio_file")
    fun uploadAudio(
        @Part("file_id") fileId: RequestBody,
        @Part("audio_id") audioId: RequestBody,
        @Part("path") path: RequestBody,
        @Part audio: MultipartBody.Part
    ): Call<Map<String, String>>
}
