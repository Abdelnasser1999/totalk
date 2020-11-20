package com.callberry.callingapp.retrofit

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiHelper {

    companion object {
        const val BASE_URL = "https://freecall.aeronbyte.com/"
        const val BASE_URL_FLAG = "${BASE_URL}flags/flags/4x3/"
        const val API_KEY = "n3C2O5xANDn87HoMwYBSXM/yVz6pbQon6r34v20c="

        fun apiService(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiService::class.java)
        }

        fun apiServiceLogin(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OkHttpClient.Builder().addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", API_KEY).build()
                    chain.proceed(request)
                }.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiService::class.java)
        }


    }
}