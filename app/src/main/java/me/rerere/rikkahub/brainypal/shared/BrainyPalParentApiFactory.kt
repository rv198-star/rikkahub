package me.rerere.rikkahub.brainypal.shared

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class BrainyPalParentApiFactory(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) {
    fun create(rootUrl: String, apiKey: String): BrainyPalParentApi {
        val client = okHttpClient.newBuilder()
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val request = if (apiKey.isNotBlank()) {
                    original.newBuilder()
                        .header("Authorization", "Bearer $apiKey")
                        .build()
                } else {
                    original
                }
                chain.proceed(request)
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(rootUrl.trimEnd('/') + "/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
            .build()
            .create(BrainyPalParentApi::class.java)
    }
}
