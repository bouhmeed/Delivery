package com.example.delivery.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.activity.ComponentActivity
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.navigation.Screen
import com.example.delivery.auth.AuthManager
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(navController: NavController) {
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        val settingsItems = remember {
            listOf(
                SettingsItem("Notifications", "Gérer les notifications", Icons.Default.Notifications),
                SettingsItem("Langue", "Français", Icons.Default.Language),
                SettingsItem("Thème", "Clair", Icons.Default.Palette),
                SettingsItem("Confidentialité", "Gérer la confidentialité", Icons.Default.Security),
                SettingsItem("À propos", "Version 1.0.0", Icons.Default.Info),
                SettingsItem("Aide", "Centre d'aide", Icons.Default.Help),
                SettingsItem("Signaler un problème", "Contacter le support", Icons.Default.BugReport)
            )
        }

        val context = LocalContext.current
        val authManager = remember { AuthManager(context) }
        var isLoggingOut by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(settingsItems) { item ->
                SettingsCard(item = item, navController = navController)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoggingOut) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Button(
                        onClick = {
                            isLoggingOut = true
                            authManager.logout(
                                onSuccess = {
                                    isLoggingOut = false
                                    Toast.makeText(context, "Déconnexion réussie", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onFailure = { error ->
                                    isLoggingOut = false
                                    Toast.makeText(context, "Erreur: ${error.message}", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Déconnexion")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCard(item: SettingsItem, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Handle click */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsItemRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.Default.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class SettingsItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
