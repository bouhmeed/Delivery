package com.example.delivery.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.navigation.Screen
import com.example.delivery.auth.AuthManager
import com.example.delivery.models.UserResponse
import com.example.delivery.models.TripHistory
import com.example.delivery.models.DriverStats
import com.example.delivery.models.DriverStatsInfo
import com.example.delivery.models.DeliveryHistoryItem
import com.example.delivery.network.ApiClient
import com.example.delivery.network.HistoryApiService
import android.widget.Toast
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.ConnectException
import java.net.UnknownHostException
import androidx.compose.material.icons.filled.Refresh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val userEmail = remember { authManager.getUserEmail() }
    val coroutineScope = rememberCoroutineScope()
    
    // États pour l'historique
    var selectedPeriod by remember { mutableStateOf("30 derniers jours") }
    var selectedStatus by remember { mutableStateOf("Tous") }
    var searchQuery by remember { mutableStateOf("") }
    
    // États pour les données
    var driverHistory by remember { mutableStateOf<TripHistory?>(null) }
    var driverStats by remember { mutableStateOf<DriverStats?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Services API
    val historyApi = remember { ApiClient.instance.create(HistoryApiService::class.java) }
    
    // Récupérer les informations de l'utilisateur pour obtenir le driverId
    var userInfo by remember { mutableStateOf<com.example.delivery.models.UserResponse?>(null) }
    
    LaunchedEffect(userEmail) {
        userEmail?.let { email ->
            isLoading = true
            coroutineScope.launch {
                try {
                    // Récupérer les infos utilisateur avec l'API existante
                    val userApi = ApiClient.instance.create(com.example.delivery.network.UserApiService::class.java)
                    val userResponse = userApi.getUserByEmail(email)
                    
                    if (userResponse.isSuccessful) {
                        userInfo = userResponse.body()
                        userInfo?.driverId?.let { driverId ->
                            // Charger l'historique et les stats
                            loadDriverHistory(historyApi, driverId, { history ->
                                driverHistory = history
                            }, { error -> errorMessage = error })
                            
                            loadDriverStats(historyApi, driverId, { stats ->
                                driverStats = stats
                            }, { error -> errorMessage = error })
                        }
                    } else {
                        errorMessage = "Erreur utilisateur: ${userResponse.code()}"
                    }
                } catch (e: Exception) {
                    errorMessage = "Erreur lors du chargement: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historique") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            userEmail?.let { email ->
                                userInfo?.driverId?.let { driverId ->
                                    coroutineScope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        
                                        loadDriverHistory(historyApi, driverId, { history ->
                                            driverHistory = history
                                        }, { error -> errorMessage = error })
                                        
                                        loadDriverStats(historyApi, driverId, { stats ->
                                            driverStats = stats
                                        }, { error -> errorMessage = error })
                                        
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Summary
            driverStats?.let { stats ->
                item {
                    DriverStatsCard(stats = stats.stats)
                }
            }
            
            // Search and filters
            item {
                SearchAndFilters(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = { selectedPeriod = it }
                )
            }
            
            // Loading indicator
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Error message
            errorMessage?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // History list
            driverHistory?.history?.let { history ->
                if (history.isEmpty()) {
                    item {
                        EmptyHistoryState()
                    }
                } else {
                    items(
                        items = history,
                        key = { it.shipmentId }
                    ) { delivery ->
                        DeliveryHistoryCard(
                            delivery = delivery,
                            onClick = {
                                // TODO: Navigate to delivery details
                                Toast.makeText(context, "Détails de la livraison ${delivery.shipmentNumber}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Aucun historique",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Vos livraisons passées apparaîtront ici",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DriverStatsCard(stats: DriverStatsInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Statistiques",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NewStatItem(
                    label = "Trajets terminés",
                    value = stats.completedTrips.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary
                )
                
                NewStatItem(
                    label = "Livraisons",
                    value = stats.deliveredShipments.toString(),
                    icon = Icons.Default.LocalShipping,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                NewStatItem(
                    label = "Succès",
                    value = "${(stats.completedTrips.toFloat() / stats.totalTrips * 100).toInt()}%",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun NewStatItem(label: String, value: String, icon: ImageVector, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SearchAndFilters(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Rechercher...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Recherche")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            
            // Period filter
            Text(
                text = "Période: $selectedPeriod",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DeliveryHistoryCard(delivery: DeliveryHistoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with shipment number and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    delivery.shipmentNumber?.let { shipmentNo ->
                        Text(
                            text = shipmentNo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = formatDate(delivery.tripDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    delivery.tripNumber?.let { tripNo ->
                        Text(
                            text = "Trajet: $tripNo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                StatusChip(status = delivery.shipmentStatus)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Client and destination information
            delivery.clientName?.let { client ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = client,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Destination information
            if (delivery.originName != null && delivery.destinationName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${delivery.originName} → ${delivery.destinationName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Trip summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    icon = Icons.Default.Inventory,
                    label = "Quantité",
                    value = "${delivery.quantity} ${delivery.uom}"
                )
                InfoItem(
                    icon = Icons.Default.LocalShipping,
                    label = "Véhicule",
                    value = delivery.vehicleName ?: "N/A"
                )
                InfoItem(
                    icon = Icons.Default.CheckCircle,
                    label = "POD",
                    value = if (delivery.podDone == true) "Oui" else "Non"
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String?) {
    val statusUpper = status?.uppercase() ?: "UNKNOWN"
    val (color, text) = when (statusUpper) {
        "DELIVERED", "COMPLETED" -> MaterialTheme.colorScheme.primary to "Livré"
        "EXPEDITION" -> MaterialTheme.colorScheme.secondary to "Expédié"
        "TO_PLAN" -> MaterialTheme.colorScheme.tertiary to "À planifier"
        "CANCELLED" -> MaterialTheme.colorScheme.error to "Annulé"
        else -> MaterialTheme.colorScheme.surfaceVariant to (status ?: "Inconnu")
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper functions
private fun formatDate(dateString: String): String {
    return try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val date = java.time.LocalDateTime.parse(dateString, formatter)
        val frenchFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH)
        date.format(frenchFormatter)
    } catch (e: Exception) {
        dateString
    }
}

private fun getStatusText(status: String?): String {
    val statusUpper = status?.uppercase() ?: "UNKNOWN"
    return when (statusUpper) {
        "DELIVERED", "COMPLETED" -> "Livré"
        "EXPEDITION" -> "Expédié"
        "TO_PLAN" -> "À planifier"
        "CANCELLED" -> "Annulé"
        else -> status ?: "Inconnu"
    }
}

// Helper functions for API calls
private suspend fun loadDriverHistory(
    api: HistoryApiService,
    driverId: String,
    onSuccess: (TripHistory) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val response = api.getDriverHistory(driverId)
        if (response.isSuccessful) {
            response.body()?.let { onSuccess(it) }
        } else {
            onError("Erreur HTTP: ${response.code()}")
        }
    } catch (e: SocketTimeoutException) {
        onError("Timeout: Le serveur ne répond pas")
    } catch (e: ConnectException) {
        onError("Connexion refusée")
    } catch (e: UnknownHostException) {
        onError("Hôte inconnu")
    } catch (e: Exception) {
        onError("Erreur: ${e.message}")
    }
}

private suspend fun loadDriverStats(
    api: HistoryApiService,
    driverId: String,
    onSuccess: (DriverStats) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val response = api.getDriverStats(driverId)
        if (response.isSuccessful) {
            response.body()?.let { onSuccess(it) }
        } else {
            onError("Erreur HTTP: ${response.code()}")
        }
    } catch (e: Exception) {
        onError("Erreur stats: ${e.message}")
    }
}
