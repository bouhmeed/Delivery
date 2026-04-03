package com.tomtom.sdk.map.display

import android.content.Context
import android.view.View

/**
 * TEMPORARY STUB CLASS - TomTom SDK not available due to missing Maven credentials
 */
class MapView(context: Context) : View(context) {
    fun getMapAsync(callback: (TomtomMap) -> Unit) {
        // Stub - provide empty map
        callback(TomtomMap())
    }
    
    fun start() {}
    fun stop() {}
}

/**
 * TEMPORARY STUB CLASS - TomTom SDK not available due to missing Maven credentials
 */
class TomtomMap {
    fun start() {}
    fun stop() {}
}
