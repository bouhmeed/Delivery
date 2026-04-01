package com.example.delivery.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.delivery.network.TodayTourApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object ApiClient {
    // Utiliser NetworkConfig pour gérer les URLs
    private val BASE_URL: String by lazy {
        NetworkConfig.getCurrentBaseUrl()
    }
    
    private val okHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)      // 30s pour se connecter
            .readTimeout(60, TimeUnit.SECONDS)          // 60s pour lire la réponse
            .writeTimeout(60, TimeUnit.SECONDS)         // 60s pour envoyer la requête
            .build()
    }
    
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun getRetrofit(): Retrofit {
        return instance
    }
    
    fun getRetrofitInstance(): Retrofit {
        return instance
    }
    
    // Pour débogage - affiche l'URL actuelle
    fun getCurrentUrl(): String = BASE_URL
}
