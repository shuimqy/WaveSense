package com.llglh.wavesense.app.network

data class LoginResponse(
    val code: Int,      // 状态码：200 成功，401 失败
    val msg: String,    // 提示信息："登录成功"
    val data: UserData? // 用户详情
)

data class UserData(
    val id: Int,
    val username: String,
    val role: String?,   // 角色：family / nurse
    val phone: String?
)