package com.tomtom.sdk.routing.model

/**
 * TEMPORARY STUB CLASS - TomTom SDK not available due to missing Maven credentials
 */
data class Route(
    val geometry: List<com.tomtom.sdk.common.GeoCoordinate>
)

/**
 * TEMPORARY STUB CLASS - TomTom SDK not available due to missing Maven credentials
 */
data class RouteCalculationOptions(
    val origin: com.tomtom.sdk.common.GeoCoordinate,
    val destination: com.tomtom.sdk.common.GeoCoordinate,
    val travelMode: TravelMode = TravelMode.CAR
)

/**
 * TEMPORARY STUB CLASS - TomTom SDK not available due to missing Maven credentials
 */
data class RoutePlanningOptions(
    val calculationOptions: RouteCalculationOptions
)

/**
 * TEMPORARY STUB CLASS - TomTom SDK not available due to missing Maven credentials
 */
enum class TravelMode {
    CAR, PEDESTRIAN, BICYCLE
}
