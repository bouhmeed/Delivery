package com.example.delivery.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.navigation.Screen
import com.example.delivery.auth.AuthManager
import com.example.delivery.models.*
import com.example.delivery.network.ApiClient
import com.example.delivery.network.HistoryApiService
import com.example.delivery.screens.NewShipmentDetailScreen
import android.widget.Toast
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.ConnectException
import java.net.UnknownHostException
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.FilterList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImprovedHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val userEmail = remember { authManager.getUserEmail() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    // États pour les filtres avancés
    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("30") }
    var selectedStatus by remember { mutableStateOf("Tous") }
    var selectedVehicleType by remember { mutableStateOf("Tous") }
    var showFilters by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("date") } // date, status, client, quantity
    
    // États pour les données
    var driverHistory by remember { mutableStateOf<TripHistory?>(null) }
    var driverStats by remember { mutableStateOf<DriverStats?>(null) }
    var filteredHistory by remember { mutableStateOf<List<DeliveryHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // États pour la navigation vers les détails de livraison
    var showShipmentDetails by remember { mutableStateOf(false) }
    var selectedShipmentId by remember { mutableIntStateOf(0) }
    
    // Services API
    val historyApi = remember { ApiClient.instance.create(HistoryApiService::class.java) }
    
    // Récupérer les informations de l'utilisateur
    var userInfo by remember { mutableStateOf<UserResponse?>(null) }
    
    // Filtrer l'historique lorsque les filtres changent
    LaunchedEffect(driverHistory, searchQuery, selectedStatus, selectedVehicleType, sortBy) {
        driverHistory?.let { history ->
            filteredHistory = filterAndSortHistory(history.history, searchQuery, selectedStatus, selectedVehicleType, sortBy)
        }
    }
    
    LaunchedEffect(userEmail) {
        userEmail?.let { email ->
            isLoading = true
            coroutineScope.launch {
                try {
                    val userApi = ApiClient.instance.create(com.example.delivery.network.UserApiService::class.java)
                    val userResponse = userApi.getUserByEmail(email)
                    
                    if (userResponse.isSuccessful) {
                        userInfo = userResponse.body()
                        userInfo?.driverId?.let { driverId ->
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
                title = { Text("Historique Organisé") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtres")
                    }
                    IconButton(
                        onClick = {
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
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 📊 Tableau de Bord Statistiques
            driverStats?.let { stats ->
                item {
                    DashboardSection(stats = stats)
                }
            }
            
            // 🔍 Filtres Avancés
            item {
                FilterSection(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = { selectedPeriod = it },
                    selectedStatus = selectedStatus,
                    onStatusChange = { selectedStatus = it },
                    selectedVehicleType = selectedVehicleType,
                    onVehicleTypeChange = { selectedVehicleType = it },
                    sortBy = sortBy,
                    onSortChange = { sortBy = it },
                    showFilters = showFilters,
                    onToggleFilters = { showFilters = !showFilters }
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
                    ErrorCard(error = error) {
                        errorMessage = null
                    }
                }
            }
            
            // 📋 Historique Organisé
            if (filteredHistory.isNotEmpty()) {
                // Regrouper par trajet
                val groupedHistory = groupHistoryByTrip(filteredHistory)
                
                items(groupedHistory.entries.toList(), key = { it.key }) { (tripKey, deliveries) ->
                    TripSection(
                        tripKey = tripKey,
                        deliveries = deliveries,
                        onItemClick = { delivery ->
                            selectedShipmentId = delivery.shipmentId.toIntOrNull() ?: 0
                            showShipmentDetails = true
                        }
                    )
                }
            } else if (!isLoading && driverHistory != null) {
                item {
                    EmptyHistoryCard()
                }
            }
        }
    }
    
    // Écran de détails de livraison
    if (showShipmentDetails && userInfo?.driverId != null) {
        NewShipmentDetailScreen(
            shipmentId = selectedShipmentId,
            driverId = userInfo?.driverId?.toIntOrNull() ?: 0,
            onNavigateBack = {
                showShipmentDetails = false
                selectedShipmentId = 0
            },
            onNavigateToMap = { address ->
                // TODO: Implement map navigation if needed
            },
            navController = navController
        )
    }
}

@Composable
fun DashboardSection(stats: DriverStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Tableau de Bord",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Badge de performance
                Surface(
                    color = when {
                        stats.stats.successRate >= 90 -> Color(0xFF4CAF50)
                        stats.stats.successRate >= 75 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${stats.stats.successRate}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Métriques principales en grille
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricCard(
                    icon = Icons.Default.CheckCircle,
                    value = stats.stats.completedTrips.toString(),
                    label = "Trajets\nTerminés",
                    color = Color(0xFF4CAF50)
                )
                
                MetricCard(
                    icon = Icons.Default.LocalShipping,
                    value = stats.stats.deliveredShipments.toString(),
                    label = "Livraisons\nEffectuées",
                    color = Color(0xFF2196F3)
                )
                
                MetricCard(
                    icon = Icons.Default.Inventory,
                    value = "${stats.stats.totalQuantity.toInt()}",
                    label = "Total\nQuantité",
                    color = Color(0xFF9C27B0)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tendances mensuelles
            if (stats.monthlyTrends.isNotEmpty()) {
                Text(
                    text = "📈 Tendances Mensuelles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(stats.monthlyTrends.take(3)) { trend ->
                        TrendItem(trend = trend)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(icon: ImageVector, value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TrendItem(trend: MonthlyTrend) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = trend.month,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "${trend.trips} trajets",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "${trend.deliveries} livraisons",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        
        Surface(
            color = when {
                trend.successRate >= 90 -> Color(0xFF4CAF50)
                trend.successRate >= 75 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "${trend.successRate.toInt()}%",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun FilterSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    selectedVehicleType: String,
    onVehicleTypeChange: (String) -> Unit,
    sortBy: String,
    onSortChange: (String) -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header avec recherche et bouton filtres
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Rechercher...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                
                IconButton(onClick = onToggleFilters) {
                    Icon(
                        if (showFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Filtres"
                    )
                }
            }
            
            // Filtres avancés
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider()
                    
                    // Période
                    FilterRow(
                        label = "Période:",
                        options = listOf("7", "30", "90", "365"),
                        selectedOption = selectedPeriod,
                        onOptionSelected = onPeriodChange,
                        optionLabels = mapOf(
                            "7" to "7 jours",
                            "30" to "30 jours",
                            "90" to "3 mois",
                            "365" to "1 an"
                        )
                    )
                    
                    // Statut
                    FilterRow(
                        label = "Statut:",
                        options = listOf("Tous", "DELIVERED", "EXPEDITION", "TO_PLAN"),
                        selectedOption = selectedStatus,
                        onOptionSelected = onStatusChange,
                        optionLabels = mapOf(
                            "Tous" to "Tous",
                            "DELIVERED" to "Livrées",
                            "EXPEDITION" to "En expédition",
                            "TO_PLAN" to "À planifier"
                        )
                    )
                    
                    // Type de véhicule
                    FilterRow(
                        label = "Véhicule:",
                        options = listOf("Tous", "CAMION", "VAN", "UTILITAIRE"),
                        selectedOption = selectedVehicleType,
                        onOptionSelected = onVehicleTypeChange,
                        optionLabels = mapOf(
                            "Tous" to "Tous",
                            "CAMION" to "Camion",
                            "VAN" to "Van",
                            "UTILITAIRE" to "Utilitaire"
                        )
                    )
                    
                    // Tri
                    FilterRow(
                        label = "Trier par:",
                        options = listOf("date", "status", "client", "quantity"),
                        selectedOption = sortBy,
                        onOptionSelected = onSortChange,
                        optionLabels = mapOf(
                            "date" to "Date",
                            "status" to "Statut",
                            "client" to "Client",
                            "quantity" to "Quantité"
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FilterRow(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    optionLabels: Map<String, String>
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selectedOption,
                    onClick = { onOptionSelected(option) },
                    label = { Text(optionLabels[option] ?: option) }
                )
            }
        }
    }
}

@Composable
fun TripSection(
    tripKey: String,
    deliveries: List<DeliveryHistoryItem>,
    onItemClick: (DeliveryHistoryItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Header du trajet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tripKey,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${deliveries.size} livraison(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Livraisons du trajet
            deliveries.forEach { delivery ->
                SimplifiedDeliveryCard(
                    delivery = delivery,
                    onClick = { onItemClick(delivery) }
                )
                if (delivery != deliveries.last()) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SimplifiedDeliveryCard(delivery: DeliveryHistoryItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = delivery.shipmentNumber ?: "N/A",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            delivery.clientName?.let { client ->
                Text(
                    text = client,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${delivery.quantity} ${delivery.uom}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                delivery.vehicleName?.let { vehicle ->
                    Text(
                        text = "• $vehicle",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        DeliveryStatusChip(status = delivery.shipmentStatus)
    }
}

@Composable
fun ErrorCard(error: String, onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = error,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun DeliveryStatusChip(status: String?) {
    val statusUpper = status?.uppercase() ?: "UNKNOWN"
    val (color, text) = when (statusUpper) {
        "DELIVERED" -> MaterialTheme.colorScheme.primary to "Livrée"
        "EXPEDITION" -> MaterialTheme.colorScheme.secondary to "En expédition"
        "TO_PLAN" -> MaterialTheme.colorScheme.tertiary to "À planifier"
        "CANCELLED" -> MaterialTheme.colorScheme.error to "Annulée"
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
fun EmptyHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                text = "Aucun résultat",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Essayez de modifier vos filtres",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Fonctions utilitaires
private fun filterAndSortHistory(
    history: List<DeliveryHistoryItem>,
    searchQuery: String,
    selectedStatus: String,
    selectedVehicleType: String,
    sortBy: String
): List<DeliveryHistoryItem> {
    var filtered = history
    
    // Filtrer par recherche
    if (searchQuery.isNotBlank()) {
        filtered = filtered.filter { item ->
            item.shipmentNumber?.contains(searchQuery, ignoreCase = true) == true ||
            item.clientName?.contains(searchQuery, ignoreCase = true) == true ||
            item.destinationName?.contains(searchQuery, ignoreCase = true) == true
        }
    }
    
    // Filtrer par statut
    if (selectedStatus != "Tous") {
        filtered = filtered.filter { it.shipmentStatus == selectedStatus }
    }
    
    // Filtrer par type de véhicule
    if (selectedVehicleType != "Tous") {
        filtered = filtered.filter { it.vehicleType == selectedVehicleType }
    }
    
    // Trier
    filtered = when (sortBy) {
        "date" -> filtered.sortedByDescending { it.tripDate }
        "status" -> filtered.sortedBy { it.shipmentStatus }
        "client" -> filtered.sortedBy { it.clientName }
        "quantity" -> filtered.sortedByDescending { it.quantity }
        else -> filtered
    }
    
    return filtered
}

private fun groupHistoryByTrip(history: List<DeliveryHistoryItem>): Map<String, List<DeliveryHistoryItem>> {
    return history.groupBy { item ->
        val date = formatDate(item.tripDate)
        val tripNumber = item.tripNumber ?: "Trajet inconnu"
        "$date - $tripNumber"
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val date = java.time.LocalDateTime.parse(dateString, formatter)
        val frenchFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM", java.util.Locale.FRENCH)
        date.format(frenchFormatter)
    } catch (e: Exception) {
        dateString
    }
}

// Fonctions API (réutilisées de l'original)
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
