package com.asr.englishasr.remote

import com.google.gson.annotations.SerializedName

data class ResultApi(

    @SerializedName("audio_url")
    val audio_url: String,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("result")
    val result: List<ItemResult>,
    @SerializedName("success")
    val success: Boolean
)