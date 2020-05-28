package com.asr.englishasr.remote

import android.media.session.MediaSession
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.io.File

interface ApiService {

    @Multipart
    @POST("/api/x3/pronunciation")
    fun getPronunciation(
        @Part("token") token: RequestBody,
        @Part file: MultipartBody.Part,
        @Part ("text-refs") refs: RequestBody
    ): Call<ResultApi>
}