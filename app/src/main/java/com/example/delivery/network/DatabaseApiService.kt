package com.example.delivery.network

import retrofit2.Response
import retrofit2.http.GET

interface DatabaseApiService {
    @GET("api/tables")
    suspend fun testConnection(): Response<List<TableResponse>>
}

data class TableResponse(
    val table_name: String
)
