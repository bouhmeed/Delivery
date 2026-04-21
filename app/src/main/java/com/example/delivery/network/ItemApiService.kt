package com.example.delivery.network

import retrofit2.Response
import retrofit2.http.GET

interface ItemApiService {
    
    /**
     * Obtenir tous les articles actifs
     */
    @GET("api/items")
    suspend fun getItems(): Response<ItemsResponse>
}

/**
 * Réponse pour la liste des articles
 */
data class ItemsResponse(
    val success: Boolean,
    val data: List<ItemDto>,
    val message: String? = null
)

/**
 * DTO pour un article
 */
data class ItemDto(
    val id: Int,
    val itemNo: String,
    val description: String,
    val unit: String,
    val weight: Double? = null,
    val volume: Double? = null,
    val category: String? = null,
    val isActive: Boolean
)
