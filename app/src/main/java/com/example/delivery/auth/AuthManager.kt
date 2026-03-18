package com.example.delivery.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.example.delivery.config.Auth0Config
import org.json.JSONObject

data class UserInfo(
    val email: String?,
    val name: String?,
    val nickname: String?
)

class AuthManager(private val context: Context) {
    private val account = Auth0(Auth0Config.CLIENT_ID, Auth0Config.DOMAIN)
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun login(onSuccess: (Credentials, UserInfo) -> Unit, onFailure: (AuthenticationException) -> Unit) {
        WebAuthProvider.login(account)
            .withScheme(Auth0Config.SCHEME)
            .withScope("openid profile email")
            .start(context, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    // Extract user info from ID token
                    val userInfo = extractUserInfoFromToken(result.idToken ?: result.accessToken)
                    
                    // Save user info locally
                    saveUserInfo(userInfo)
                    
                    onSuccess(result, userInfo)
                }

                override fun onFailure(error: AuthenticationException) {
                    onFailure(error)
                }
            })
    }

    private fun extractUserInfoFromToken(token: String): UserInfo {
        return try {
            // Decode JWT token to get user info
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING))
                val json = JSONObject(payload)
                
                UserInfo(
                    email = json.optString("email", null),
                    name = json.optString("name", null),
                    nickname = json.optString("nickname", null)
                )
            } else {
                UserInfo(
                    email = "chauffeur@delivery.com",
                    name = null,
                    nickname = null
                )
            }
        } catch (e: Exception) {
            UserInfo(
                email = "chauffeur@delivery.com",
                name = null,
                nickname = null
            )
        }
    }

    private fun saveUserInfo(userInfo: UserInfo) {
        prefs.edit().apply {
            putString("user_email", userInfo.email)
            putString("user_name", userInfo.name)
            putString("user_nickname", userInfo.nickname)
            apply()
        }
    }

    fun getUserEmail(): String? {
        return prefs.getString("user_email", null)
    }

    fun getUserName(): String? {
        return prefs.getString("user_name", null)
    }

    fun logout(onSuccess: () -> Unit, onFailure: (AuthenticationException) -> Unit) {
        // Clear saved user info
        prefs.edit().clear().apply()
        
        WebAuthProvider.logout(account)
            .withScheme(Auth0Config.SCHEME)
            .start(context, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(result: Void?) {
                    onSuccess()
                }

                override fun onFailure(error: AuthenticationException) {
                    onFailure(error)
                }
            })
    }
}
