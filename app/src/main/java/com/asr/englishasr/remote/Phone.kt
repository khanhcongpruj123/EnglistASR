package com.asr.englishasr.remote

import com.google.gson.annotations.SerializedName

data class Phone(
    @SerializedName("end_time")
    val end_time: Double,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("phone_ipa")
    val phone_ipa: String,
    @SerializedName("score")
    val score: Double,
    @SerializedName("score_normalize")
    val score_normalize: Double,
    @SerializedName("start_time")
    val start_time: Double
)