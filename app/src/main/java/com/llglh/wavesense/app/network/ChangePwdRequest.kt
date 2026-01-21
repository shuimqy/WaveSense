package com.llglh.wavesense.app.network

data class ChangePwdRequest(
    val username: String,
    val old_password: String,
    val new_password: String
)