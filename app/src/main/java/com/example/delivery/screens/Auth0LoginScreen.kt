// Temporarily commented out since Auth0 dependency is disabled
/*
package com.example.delivery.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.result.Credentials
import com.example.delivery.auth.Auth0Manager
import com.example.delivery.navigation.Screen

@Composable
fun Auth0LoginScreen(navController: NavController, auth0Manager: Auth0Manager) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    // Trigger Auth0 login when screen is shown
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = ""
        
        auth0Manager.login(
            onSuccess = { credentials ->
                isLoading = false
                // Navigate to home screen on successful login
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
            onError = { error ->
                isLoading = false
                errorMessage = "Erreur de connexion: $error"
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Connexion en cours...",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Button(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text("Retour")
            }
        }
    }
}
*/
