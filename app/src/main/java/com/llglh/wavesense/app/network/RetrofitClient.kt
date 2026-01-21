package com.llglh.wavesense.app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // ⚠️⚠️⚠️ 必改：把下面的 IP 换成你云服务器的公网 IP ⚠️⚠️⚠️
    // 格式必须是：http://x.x.x.x:5000/  (最后要有斜杠)
    private const val BASE_URL = "http://124.222.238.77:5000/"

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // 会在 Logcat 打印详细请求日志
            })
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}