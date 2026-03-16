package com.example.delivery.auth

import com.auth0.android.Auth0

object Auth0Config {
    private const val CLIENT_ID = "nv6hJnrENz4yWMuXAKeHclNLveG39oq5"
    private const val CLIENT_SECRET = "JF1ENICGOjkjUCTkhz3Aq1RoHR4aLp04rSqY-CPYtW9WW94NgcZyYcvkqAVriZsd"
    private const val ISSUER_BASE_URL = "https://almakom.eu.auth0.com"
    
    fun getAuth0(): Auth0 {
        return Auth0(CLIENT_ID, ISSUER_BASE_URL)
    }
    
    const val CALLBACK_URL = "delivery://almakom.eu.auth0.com/android/com.example.delivery/callback"
    const val LOGOUT_CALLBACK_URL = "delivery://almakom.eu.auth0.com/android/com.example.delivery/callback"
}
