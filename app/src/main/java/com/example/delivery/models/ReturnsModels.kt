package com.example.delivery.models

/**
 * Request model for submitting returns
 */
data class ReturnsRequest(
    val shipmentId: Int,
    val shipmentNo: String,
    val packagesRecovered: Boolean,
    val packagingRecovered: Boolean,
    val quantities: RecoveredQuantities,
    val comment: String,
    val defects: List<ItemDefect>,
    val photoUri: String? = null,
    val photoBase64: String? = null
)

/**
 * Quantities of recovered packaging
 */
data class RecoveredQuantities(
    val palettes: Int = 0,
    val caisses: Int = 0,
    val bouteilles: Int = 0,
    val futs: Int = 0,
    val autre: Int = 0
)

/**
 * Item defect information
 */
data class ItemDefect(
    val article: String,
    val quantity: Int,
    val reason: String
)

/**
 * Article for dropdown selection
 */
data class Article(
    val id: String,
    val name: String,
    val description: String? = null
)

/**
 * Sample articles for demo
 */
val sampleArticles = listOf(
    Article("1", "Bouteille 1L"),
    Article("2", "Bouteille 5L"),
    Article("3", "Caisse 12x1L"),
    Article("4", "Palette 48x1L"),
    Article("5", "Fût 50L"),
    Article("6", "Autre")
)

/**
 * Response from returns API
 */
data class ReturnsResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)
