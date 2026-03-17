package com.example.delivery.auth

import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.example.delivery.config.Auth0Config

class AuthManager(private val context: Context) {
    private val account = Auth0(Auth0Config.CLIENT_ID, Auth0Config.DOMAIN)

    fun login(onSuccess: (Credentials) -> Unit, onFailure: (AuthenticationException) -> Unit) {
        WebAuthProvider.login(account)
            .withScheme(Auth0Config.SCHEME)
            .withScope("openid profile email")
            .start(context, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    onSuccess(result)
                }

                override fun onFailure(error: AuthenticationException) {
                    onFailure(error)
                }
            })
    }

    fun logout(onSuccess: () -> Unit, onFailure: (AuthenticationException) -> Unit) {
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
