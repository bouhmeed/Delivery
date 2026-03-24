package com.example.delivery.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.delivery.network.TodayTourApiService

object ApiClient {
    // Utiliser NetworkConfig pour gérer les URLs
    private val BASE_URL: String by lazy {
        NetworkConfig.getCurrentBaseUrl()
    }
    
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun getRetrofit(): Retrofit {
        return instance
    }
    
    // Pour débogage - affiche l'URL actuelle
    fun getCurrentUrl(): String = BASE_URL
}
