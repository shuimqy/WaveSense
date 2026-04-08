package com.llglh.wavesense.app.network

// 1. 设置数据的实体类 (接收数据用)
data class Settings(
    val user_id: Int,
    val alarm_duration: Int,
    val is_vibrate: Int,
    val ringtone_uri: String?
)

// 2. 响应类 (接收服务器返回的 code/msg)
data class SettingsResponse(
    val code: Int,
    val msg: String,
    val data: Settings?
)

// 3. 更新设置的请求类 (发送数据用)
data class UpdateSettingsRequest(
    val user_id: Int,
    val alarm_duration: Int,
    val is_vibrate: Int,
    val ringtone_uri: String?
)
