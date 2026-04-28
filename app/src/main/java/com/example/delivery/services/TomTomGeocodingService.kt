package com.example.delivery.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import org.json.JSONObject

/**
 * Service de géocodage utilisant l'API TomTom
 * Convertit les adresses en coordonnées GPS
 */
class TomTomGeocodingService {
    
    companion object {
        private const val API_KEY = "GyrQYLHTCqja2kxwBXno1UGLMWT2AMPe"
        private const val BASE_URL = "https://api.tomtom.com/search/2/geocode"
    }
    
    /**
     * Résultat du géocodage
     */
    data class GeocodingResult(
        val latitude: Double,
        val longitude: Double,
        val formattedAddress: String? = null
    )
    
    /**
     * Géocode une adresse complète
     * @param address Adresse complète
     * @param city Ville
     * @param postalCode Code postal
     * @param country Pays (optionnel)
     * @return GeocodingResult ou null si échec
     */
    suspend fun geocodeAddress(
        address: String?,
        city: String?,
        postalCode: String?,
        country: String? = "France"
    ): GeocodingResult? = withContext(Dispatchers.IO) {
        try {
            // Construire l'adresse complète
            val fullAddress = buildString {
                address?.let { append(it).append(", ") }
                postalCode?.let { append(it).append(" ") }
                city?.let { append(it) }
                country?.let { append(", ").append(it) }
            }
            
            if (fullAddress.isBlank()) {
                return@withContext null
            }
            
            // Encoder l'adresse pour l'URL
            val encodedAddress = java.net.URLEncoder.encode(fullAddress, "UTF-8")
            
            // Construire l'URL de l'API TomTom
            val url = URL("$BASE_URL/$encodedAddress.json?key=$API_KEY")
            
            println("🗺️ Géocodage: $fullAddress")
            println("🔗 URL: $url")
            
            // Faire la requête
            val response = url.readText()
            val json = JSONObject(response)
            
            // Extraire les coordonnées
            val results = json.optJSONArray("results")
            if (results != null && results.length() > 0) {
                val firstResult = results.getJSONObject(0)
                val position = firstResult.getJSONObject("position")
                val lat = position.getDouble("lat")
                val lon = position.getDouble("lon")
                val formattedAddress = firstResult.optString("address", null)
                
                println("✅ Géocodage réussi: $lat, $lon")
                
                GeocodingResult(lat, lon, formattedAddress)
            } else {
                println("❌ Aucun résultat trouvé pour: $fullAddress")
                null
            }
        } catch (e: Exception) {
            println("❌ Erreur de géocodage: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Calcule la distance entre deux points GPS en utilisant la formule Haversine
     * @param lat1 Latitude du point 1
     * @param lon1 Longitude du point 1
     * @param lat2 Latitude du point 2
     * @param lon2 Longitude du point 2
     * @return Distance en kilomètres
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val R = 6371.0 // Rayon de la Terre en km
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return R * c
    }
    
    /**
     * Calcule la distance entre deux adresses (distance directe en ligne droite)
     * @param originAddress Adresse d'origine
     * @param originCity Ville d'origine
     * @param originPostalCode Code postal d'origine
     * @param destAddress Adresse de destination
     * @param destCity Ville de destination
     * @param destPostalCode Code postal de destination
     * @return Distance en kilomètres ou null si échec
     */
    suspend fun calculateDistanceBetweenAddresses(
        originAddress: String?,
        originCity: String?,
        originPostalCode: String?,
        destAddress: String?,
        destCity: String?,
        destPostalCode: String?
    ): Double? {
        // Géocoder l'origine
        val originCoords = geocodeAddress(originAddress, originCity, originPostalCode)
        if (originCoords == null) {
            println("❌ Impossible de géocoder l'origine")
            return null
        }
        
        // Géocoder la destination
        val destCoords = geocodeAddress(destAddress, destCity, destPostalCode)
        if (destCoords == null) {
            println("❌ Impossible de géocoder la destination")
            return null
        }
        
        // Calculer la distance
        val distance = calculateDistance(
            originCoords.latitude,
            originCoords.longitude,
            destCoords.latitude,
            destCoords.longitude
        )
        
        println("📏 Distance calculée: $distance km")
        return distance
    }
    
    /**
     * Calcule la distance réelle des routes entre deux adresses via l'API TomTom Routing
     * @param originAddress Adresse d'origine
     * @param originCity Ville d'origine
     * @param originPostalCode Code postal d'origine
     * @param destAddress Adresse de destination
     * @param destCity Ville de destination
     * @param destPostalCode Code postal de destination
     * @return Distance en kilomètres ou null si échec
     */
    suspend fun calculateRouteDistanceBetweenAddresses(
        originAddress: String?,
        originCity: String?,
        originPostalCode: String?,
        destAddress: String?,
        destCity: String?,
        destPostalCode: String?
    ): Double? {
        // Géocoder l'origine
        val originCoords = geocodeAddress(originAddress, originCity, originPostalCode)
        if (originCoords == null) {
            println("❌ Impossible de géocoder l'origine")
            return null
        }
        
        // Géocoder la destination
        val destCoords = geocodeAddress(destAddress, destCity, destPostalCode)
        if (destCoords == null) {
            println("❌ Impossible de géocoder la destination")
            return null
        }
        
        // Calculer la distance des routes via l'API TomTom Routing
        val routeDistance = calculateRouteDistance(
            originCoords.latitude,
            originCoords.longitude,
            destCoords.latitude,
            destCoords.longitude
        )
        
        println("📏 Distance des routes calculée: $routeDistance km")
        return routeDistance
    }
    
    /**
     * Calcule la distance réelle des routes entre deux points GPS via l'API TomTom Routing
     * @param lat1 Latitude du point 1
     * @param lon1 Longitude du point 1
     * @param lat2 Latitude du point 2
     * @param lon2 Longitude du point 2
     * @return Distance en kilomètres ou null si échec
     */
    suspend fun calculateRouteDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double? = withContext(Dispatchers.IO) {
        try {
            // Construire l'URL de l'API TomTom Routing
            val url = URL(
                "https://api.tomtom.com/routing/1/calculateRoute/" +
                "$lat1,$lon1:$lat2,$lon2/json?key=$API_KEY"
            )
            
            println("🚗 Calcul de route: $lat1,$lon1 -> $lat2,$lon2")
            println("🔗 URL: $url")
            
            // Faire la requête
            val response = url.readText()
            val json = JSONObject(response)
            
            // Extraire la distance de la réponse
            val routes = json.optJSONArray("routes")
            if (routes != null && routes.length() > 0) {
                val firstRoute = routes.getJSONObject(0)
                val summary = firstRoute.getJSONObject("summary")
                val distanceInMeters = summary.getDouble("lengthInMeters")
                val distanceInKm = distanceInMeters / 1000.0
                
                println("✅ Route calculée: $distanceInKm km")
                
                distanceInKm
            } else {
                println("❌ Aucun itinéraire trouvé")
                null
            }
        } catch (e: Exception) {
            println("❌ Erreur de calcul de route: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
