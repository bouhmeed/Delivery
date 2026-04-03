package com.tomtom.sdk.routing

import android.content.Context

/**
 * TEMPORARY STUB CLASS - TomTom SDK not available due to missing Maven credentials
 */
class RoutingApi(context: Context, apiKey: String) {
    suspend fun planRoute(options: com.tomtom.sdk.routing.model.RoutePlanningOptions): com.tomtom.sdk.routing.model.Route {
        return com.tomtom.sdk.routing.model.Route(emptyList())
    }
}
