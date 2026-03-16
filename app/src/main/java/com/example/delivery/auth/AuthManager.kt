package com.example.delivery.auth

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthManager(private val activity: Activity) {
    private val auth0: Auth0 = Auth0Config.getAuth0()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    
    private val _currentUser = MutableStateFlow<String?>(null)
    val currentUser: StateFlow<String?> = _currentUser
    
    private val _isLoading = mutableStateOf(false)
    val isLoading = _isLoading
    
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage = _errorMessage
    
    private val _logoutInProgress = MutableStateFlow(false)
    val logoutInProgress: StateFlow<Boolean> = _logoutInProgress.asStateFlow()
    
    init {
        checkExistingSession()
    }
    
    private fun checkExistingSession() {
        try {
            // Check if we have stored credentials using SharedPreferences
            val prefs = activity.getSharedPreferences("auth0", Activity.MODE_PRIVATE)
            val accessToken = prefs.getString("access_token", null)
            val idToken = prefs.getString("id_token", null)
            
            // Only set logged in if we have valid tokens
            // For now, just check if tokens exist and are not empty
            // In production, you should validate the token expiration with Auth0
            if (!accessToken.isNullOrEmpty() && !idToken.isNullOrEmpty()) {
                // Additional basic validation: check if tokens look like JWT (have 3 parts)
                val isAccessTokenValid = accessToken.split(".").size == 3
                val isIdTokenValid = idToken.split(".").size == 3
                
                if (isAccessTokenValid && isIdTokenValid) {
                    _isLoggedIn.value = true
                    _currentUser.value = idToken
                } else {
                    // Clear invalid tokens
                    clearLocalSession()
                }
            }
        } catch (e: Exception) {
            // No existing session or invalid session
            clearLocalSession()
        }
    }
    
    fun login() {
        _isLoading.value = true
        _errorMessage.value = null
        
        // Try without audience first to test basic login
        WebAuthProvider
            .login(auth0)
            .withScheme("delivery")
            .withAudience("https://almakom.eu.auth0.com/api/v2/")
            .start(activity, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    _isLoading.value = false
                    handleAuthenticationSuccess(result)
                }
                
                override fun onFailure(error: AuthenticationException) {
                    _isLoading.value = false
                    _errorMessage.value = "Échec de connexion: ${error.message}"
                    println("Auth0 Error Details: ${error.getDescription()}")
                    println("Auth0 Error Code: ${error.getCode()}")
                    
                    // If basic login fails, try with audience
                    if (error.message?.contains("callback") == true) {
                        println("Trying with audience parameter...")
                        loginWithAudience()
                    }
                }
            })
    }
    
    private fun loginWithAudience() {
        WebAuthProvider
            .login(auth0)
            .withScheme("delivery")
            .withAudience("https://almakom.eu.auth0.com/api/v2/")
            .start(activity, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    _isLoading.value = false
                    handleAuthenticationSuccess(result)
                }
                
                override fun onFailure(error: AuthenticationException) {
                    _isLoading.value = false
                    _errorMessage.value = "Échec de connexion: ${error.message}"
                    println("Auth0 Error with Audience: ${error.getDescription()}")
                }
            })
    }
    
    private fun handleAuthenticationSuccess(credentials: Credentials) {
        // Save credentials using SharedPreferences
        val prefs = activity.getSharedPreferences("auth0", Activity.MODE_PRIVATE)
        prefs.edit()
            .putString("access_token", credentials.accessToken)
            .putString("id_token", credentials.idToken)
            .putString("refresh_token", credentials.refreshToken)
            .apply()
        
        // Update state
        _isLoggedIn.value = true
        _currentUser.value = credentials.idToken ?: credentials.accessToken
    }
    
    fun logout(onLogoutComplete: (Boolean) -> Unit = {}) {
        _logoutInProgress.value = true
        _errorMessage.value = null
        
        // Simple local logout - clears session without Auth0 browser logout
        clearLocalSession()
        _logoutInProgress.value = false
        onLogoutComplete(true)
    }
    
    private fun clearLocalSession() {
        val prefs = activity.getSharedPreferences("auth0", Activity.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        _isLoggedIn.value = false
        _currentUser.value = null
    }
}
