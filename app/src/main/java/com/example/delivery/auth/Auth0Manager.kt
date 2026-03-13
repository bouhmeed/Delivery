package com.example.delivery.auth

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Data classes for Auth0 API responses
data class Auth0TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("id_token") val idToken: String? = null
)

data class Auth0UserInfo(
    @SerializedName("sub") val sub: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("picture") val picture: String? = null
)

class Auth0Manager(private val context: Context) {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    
    // Generate random state for security
    private fun generateState(): String {
        return UUID.randomUUID().toString()
    }
    
    // Build Universal Login URL
    fun buildUniversalLoginUrl(state: String): String {
        return Uri.Builder()
            .scheme("https")
            .authority("almakom.eu.auth0.com")
            .path("/authorize")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", Auth0Config.CLIENT_ID)
            .appendQueryParameter("redirect_uri", "delivery://auth0/callback")
            .appendQueryParameter("scope", "openid profile email")
            .appendQueryParameter("state", state)
            .build()
            .toString()
    }
    
    // Exchange authorization code for tokens
    suspend fun exchangeCodeForTokens(code: String, state: String): Result<Auth0TokenResponse> {
        return suspendCancellableCoroutine { continuation ->
            try {
                val formBody = FormBody.Builder()
                    .add("grant_type", "authorization_code")
                    .add("client_id", Auth0Config.CLIENT_ID)
                    .add("client_secret", Auth0Config.CLIENT_SECRET)
                    .add("code", code)
                    .add("redirect_uri", "delivery://auth0/callback")
                    .build()
                
                val request = Request.Builder()
                    .url("${Auth0Config.ISSUER_BASE_URL}/oauth/token")
                    .post(formBody)
                    .build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(e)
                        }
                    }
                    
                    override fun onResponse(call: Call, response: Response) {
                        if (continuation.isActive) {
                            if (response.isSuccessful) {
                                response.body?.let { responseBody ->
                                    try {
                                        val tokenResponse = gson.fromJson(responseBody.string(), Auth0TokenResponse::class.java)
                                        continuation.resume(Result.success(tokenResponse))
                                    } catch (e: Exception) {
                                        continuation.resumeWithException(e)
                                    }
                                } ?: continuation.resumeWithException(Exception("Empty response body"))
                            } else {
                                continuation.resumeWithException(Exception("Token exchange failed: ${response.code}"))
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    // Get user info with access token
    suspend fun getUserInfo(accessToken: String): Result<Auth0UserInfo> {
        return suspendCancellableCoroutine { continuation ->
            try {
                val request = Request.Builder()
                    .url("${Auth0Config.ISSUER_BASE_URL}/userinfo")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .get()
                    .build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(e)
                        }
                    }
                    
                    override fun onResponse(call: Call, response: Response) {
                        if (continuation.isActive) {
                            if (response.isSuccessful) {
                                response.body?.let { responseBody ->
                                    try {
                                        val userInfo = gson.fromJson(responseBody.string(), Auth0UserInfo::class.java)
                                        continuation.resume(Result.success(userInfo))
                                    } catch (e: Exception) {
                                        continuation.resumeWithException(e)
                                    }
                                } ?: continuation.resumeWithException(Exception("Empty response body"))
                            } else {
                                continuation.resumeWithException(Exception("Failed to get user info: ${response.code}"))
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    // Open Universal Login in Chrome Custom Tab
    fun openUniversalLogin(onAuthCodeReceived: (String, String) -> Unit) {
        val state = generateState()
        val authUrl = buildUniversalLoginUrl(state)
        
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, android.net.Uri.parse(authUrl))
        
        // In a real implementation, you would need to handle the callback
        // This would typically involve a deep link or broadcast receiver
        // For now, this is a simplified version
    }
    
    fun logout() {
        // Clear stored tokens
        // In a real app, you would clear SharedPreferences or secure storage
    }
    
    fun isAuthenticated(): Boolean {
        // Check if we have stored tokens
        // In a real app, you would check SharedPreferences or secure storage
        return false
    }
}
