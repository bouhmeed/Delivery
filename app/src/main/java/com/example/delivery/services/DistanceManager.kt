package com.example.delivery.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 🎯 MANAGER UNIFIÉ DES DISTANCES
 * Garantit la cohérence des distances dans TOUTE l'application
 * 
 * Problème résolu : Plusieurs méthodes de calcul donnaient des distances différentes
 * Solution : UNE SEULE méthode de calcul utilisée PARTOUT
 */
object DistanceManager {
    
    private val geocodingService = TomTomGeocodingService()
    
    /**
     * Cache des distances déjà calculées pour éviter les appels API répétés
     * Clé : "originAddress|originCity|destAddress|destCity"
     */
    private val distanceCache = mutableMapOf<String, Double?>()
    
    /**
     * 🎯 MÉTHODE UNIFIÉE DE CALCUL DE DISTANCE
     * UTILISÉE PARTOUT : Carte, Navigation, Map Globale
     * 
     * @param originAddress Adresse d'origine
     * @param originCity Ville d'origine  
     * @param originPostalCode Code postal d'origine
     * @param destAddress Adresse de destination
     * @param destCity Ville de destination
     * @param destPostalCode Code postal de destination
     * @return Distance en kilomètres (API TomTom Routing) ou null si échec
     */
    suspend fun calculateUnifiedDistance(
        originAddress: String?,
        originCity: String?,
        originPostalCode: String?,
        destAddress: String?,
        destCity: String?,
        destPostalCode: String?
    ): Double? = withContext(Dispatchers.IO) {
        
        // Créer la clé de cache
        val cacheKey = "${originAddress}|${originCity}|${originPostalCode}|${destAddress}|${destCity}|${destPostalCode}"
        
        // Vérifier le cache d'abord
        distanceCache[cacheKey]?.let { cachedDistance ->
            println("📍 DISTANCE DU CACHE: ${cachedDistance.toInt()} km")
            return@withContext cachedDistance
        }
        
        println("🔍 CALCUL DISTANCE UNIFIÉE:")
        println("   Origine: $originAddress, $originCity $originPostalCode")
        println("   Destination: $destAddress, $destCity $destPostalCode")
        
        try {
            // 🎯 UTILISER LA MÊME API PARTOUT : TomTom Routing
            val distance = geocodingService.calculateRouteDistanceBetweenAddresses(
                originAddress = originAddress,
                originCity = originCity,
                originPostalCode = originPostalCode,
                destAddress = destAddress,
                destCity = destCity,
                destPostalCode = destPostalCode
            )
            
            // Mettre en cache
            distanceCache[cacheKey] = distance
            
            println("✅ DISTANCE UNIFIÉE CALCULÉE: ${distance?.toInt()} km")
            
            return@withContext distance
            
        } catch (e: Exception) {
            println("❌ ERREUR CALCUL DISTANCE UNIFIÉE: ${e.message}")
            return@withContext null
        }
    }
    
    /**
     * Calcule la distance pour une livraison (méthode pratique)
     */
    suspend fun calculateDeliveryDistance(
        originAddress: String?,
        originCity: String?,
        originPostalCode: String?,
        deliveryAddress: String?,
        deliveryCity: String?,
        deliveryZipCode: String?
    ): Double? {
        return calculateUnifiedDistance(
            originAddress = originAddress,
            originCity = originCity,
            originPostalCode = originPostalCode,
            destAddress = deliveryAddress,
            destCity = deliveryCity,
            destPostalCode = deliveryZipCode
        )
    }
    
    /**
     * Vide le cache (utile pour les tests ou si les données changent)
     */
    fun clearCache() {
        distanceCache.clear()
        println("🗑️ Cache de distances vidé")
    }
    
    /**
     * Retourne les statistiques du cache
     */
    fun getCacheStats(): String {
        return "Cache: ${distanceCache.size} distances calculées"
    }
}
