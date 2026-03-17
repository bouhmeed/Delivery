package com.example.delivery.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.delivery.R
import com.example.delivery.auth.AuthManager
import com.example.delivery.navigation.Screen

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circular Logo
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.logo_alma_track_bleu),
            contentDescription = "Delivery App Logo",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .padding(bottom = 24.dp),
            contentScale = ContentScale.Crop
        )

        Text(
            text = "Bienvenue dans Delivery App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    isLoading = true
                    authManager.login(
                        onSuccess = { credentials ->
                            isLoading = false
                            Toast.makeText(context, "Connexion réussie", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onFailure = { error ->
                            isLoading = false
                            Toast.makeText(context, "Erreur: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Se connecter")
            }
        }
    }
}
