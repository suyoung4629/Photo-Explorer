package com.unsplash.photoexplorer.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UnsplashAuthInterceptor @Inject constructor(
    private val accessKey: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Client-ID $accessKey")
            .addHeader("Accept-Version", "v1")
            .build()
        return chain.proceed(request)
    }
}
