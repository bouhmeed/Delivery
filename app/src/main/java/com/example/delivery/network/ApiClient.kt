package com.example.delivery.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Utiliser l'IP locale du PC pour l'émulateur Android
    private const val BASE_URL = "http://192.168.2.131:3000/" // IP locale de votre machine
    // private const val BASE_URL = "http://10.0.2.2:3000/" // Pour émulateur Android (alternative)
    
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // Alternative pour HTTPS (décommentez si vous utilisez un certificat SSL)
    /*
    private const val BASE_URL = "https://10.0.2.2:3000/" // Pour émulateur Android avec HTTPS
    
    val instance: Retrofit by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .hostnameVerifier { _, _ -> true } // Accepter tous les certificats (uniquement pour le développement)
            .build()
            
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    */
}
