package com.llglh.wavesense.app.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("register")
    fun register(@Body request: RegisterRequest): Call<LoginResponse>
    // 注：注册的返回值结构通常和登录一样(都有code/msg)，所以可以复用 LoginResponse
    // ✅ 新增：修改密码
    @POST("change_password")
    fun changePassword(@Body request: ChangePwdRequest): Call<LoginResponse>
    // 注：返回值可以用 LoginResponse，因为我们只需要 code 和 msg
    @POST("get_alarms")
    fun getAlarms(@Body request: Map<String, String>): Call<BaseListResponse<Alarm>>

    // 解释：
    // 1. @POST("get_alarms") -> 对应 Python 的 @app.route('/get_alarms')
    // 2. Call<BaseListResponse<Alarm>> -> 期待收到一个装满 Alarm 的包裹
    @POST("get_latest_data")
    fun getLatestData(@Body request: Map<String, String>): Call<BaseListResponse<Map<String, String>>>
    // 注意：这里为了省事，我用了 Map<String, String> 来接数据。
    // 因为 Python 返回的是 {"bpm": "75", "breath": "18"}，用 Map 接正好。
}