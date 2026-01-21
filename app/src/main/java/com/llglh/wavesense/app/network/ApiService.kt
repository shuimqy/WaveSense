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
}