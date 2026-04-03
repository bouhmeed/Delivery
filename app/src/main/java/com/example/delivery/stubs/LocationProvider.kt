package com.tomtom.sdk.common.location

import android.content.Context

/**
 * TEMPORARY STUB CLASS - TomTom SDK not available due to missing Maven credentials
 */
class LocationProvider(context: Context) : LocationSource {
    override fun start() {}
    override fun stop() {}
}

/**
 * TEMPORARY STUB CLASS - TomTom SDK not available due to missing Maven credentials
 */
interface LocationSource {
    fun start()
    fun stop()
}
