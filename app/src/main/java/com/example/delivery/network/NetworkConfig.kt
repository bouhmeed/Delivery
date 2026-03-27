package com.example.delivery.network

object NetworkConfig {
    // Configuration URLs - changez selon votre environnement
    val BASE_URLS = listOf(
        "http://192.168.2.145:3000/",  // IP WiFi actuelle (appareil physique)
        "http://192.168.2.131:3000/",  // Ancienne IP (backup)
        "http://10.0.2.2:3000/",  // Émulateur Android (localhost du PC)
        "http://localhost:3000/"  // Test local
    )
    
    // URL actuelle (changez l'index pour tester différentes configurations)
    private var currentUrlIndex = 0  // Utilise l'IP WiFi (192.168.2.145:3000)
    
    fun getCurrentBaseUrl(): String {
        return BASE_URLS[currentUrlIndex]
    }
    
    fun switchToNextUrl(): String {
        currentUrlIndex = (currentUrlIndex + 1) % BASE_URLS.size
        return getCurrentBaseUrl()
    }
    
    fun getAllUrls(): List<String> = BASE_URLS
}
