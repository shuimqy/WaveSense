package com.llglh.wavesense.app.network

import com.google.gson.annotations.SerializedName

data class Alarm(
    val id: Int,
    @SerializedName("event_type") val type: String, // 对应数据库字段 event_type
    val level: Int,                                 // 1=警告, 2=严重
    val description: String,
    @SerializedName("create_time") val time: String
)