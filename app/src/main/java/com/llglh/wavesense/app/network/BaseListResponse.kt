package com.llglh.wavesense.app.network

// <T> 的意思是“泛型”，表示 data 里面装什么都可以。
// 这次我们装 Alarm，下次可以装别的。
data class BaseListResponse<T>(
    val code: Int,
    val msg: String,
    val data: List<T>? // 注意这里是 List<T>，而且可能是空的(?)
)