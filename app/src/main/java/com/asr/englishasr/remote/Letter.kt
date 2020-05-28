package com.asr.englishasr.remote

import com.google.gson.annotations.SerializedName

data class Letter(
    @SerializedName("end_time")
    val end_time: Double,
    @SerializedName("letter")
    val letter: String,
    @SerializedName("phones")
    val phones: List<Phone>,
    @SerializedName("score")
    val score: Double,
    @SerializedName("score_normalize")
    val score_normalize: Double,
    @SerializedName("start_time")
    val start_time: Double
)