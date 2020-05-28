package com.asr.englishasr.remote

import com.google.gson.annotations.SerializedName

data class ItemResult(
    @SerializedName("end_time")
    val endTime: Float,
    @SerializedName("letters")
    val letters: List<Letter>,
    @SerializedName("phones")
    val phones: List<Phone>,
    @SerializedName("score")
    val score: Float,
    @SerializedName("score_normalize")
    val score_normalize: Float,
    @SerializedName("start_time")
    val start_time: Float,
    @SerializedName("word")
    val word: String
)