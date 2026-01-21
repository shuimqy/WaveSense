package com.llglh.wavesense.app.network

data class RegisterRequest(
    val username: String,
    val password: String,
    val role: String = "family", // 默认为家属，你可以后续在UI里加个下拉框选 'nurse'
    val phone: String = ""
)