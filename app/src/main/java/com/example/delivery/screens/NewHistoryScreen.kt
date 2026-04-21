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
import androidx.compose.material.icons.filled.Percent
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
import androidx.compose.ui.unit.sp
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
fun NewHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val userEmail = remember { authManager.getUserEmail() }
    val coroutineScope = rememberCoroutineScope()
    
    // États pour l'historique avec filtres améliorés
    var selectedPeriod by remember { mutableStateOf("Toutes les livraisons passées") }
    var selectedStatus by remember { mutableStateOf("Tous") }
    var searchQuery by remember { mutableStateOf("") }
    
    // États pour les dates personnalisées
    var customStartDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
    var customEndDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // DatePickerDialog pour date de début
    val startDatePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            customStartDate = java.time.LocalDate.of(year, month + 1, dayOfMonth)
        },
        java.time.LocalDate.now().year,
        java.time.LocalDate.now().monthValue - 1,
        java.time.LocalDate.now().dayOfMonth
    )
    
    // DatePickerDialog pour date de fin
    val endDatePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            customEndDate = java.time.LocalDate.of(year, month + 1, dayOfMonth)
        },
        java.time.LocalDate.now().year,
        java.time.LocalDate.now().monthValue - 1,
        java.time.LocalDate.now().dayOfMonth
    )
    
    // Afficher les DatePickerDialog quand demandé
    LaunchedEffect(showStartDatePicker) {
        if (showStartDatePicker) {
            startDatePickerDialog.show()
            showStartDatePicker = false
        }
    }
    
    LaunchedEffect(showEndDatePicker) {
        if (showEndDatePicker) {
            endDatePickerDialog.show()
            showEndDatePicker = false
        }
    }
    
    // Périodes prédéfinies
    val periodOptions = listOf(
        "Toutes les livraisons passées",
        "Dernière semaine",
        "30 derniers jours",
        "90 derniers jours", 
        "6 derniers mois",
        "Cette année",
        "Période personnalisée"
    )
    
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
                    onPeriodChange = { selectedPeriod = it },
                    periodOptions = periodOptions,
                    customStartDate = customStartDate,
                    customEndDate = customEndDate,
                    onCustomStartDateChange = { customStartDate = it },
                    onCustomEndDateChange = { customEndDate = it },
                    showStartDatePicker = showStartDatePicker,
                    showEndDatePicker = showEndDatePicker,
                    onShowStartDatePickerChange = { showStartDatePicker = it },
                    onShowEndDatePickerChange = { showEndDatePicker = it }
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
                // Filter history based on search query
                val searchFilteredHistory = if (searchQuery.isBlank()) {
                    history
                } else {
                    val query = searchQuery.lowercase()
                    history.filter { delivery ->
                        delivery.shipmentNumber?.lowercase()?.contains(query) == true ||
                        delivery.clientName?.lowercase()?.contains(query) == true ||
                        delivery.originName?.lowercase()?.contains(query) == true ||
                        delivery.destinationName?.lowercase()?.contains(query) == true ||
                        delivery.tripNumber?.lowercase()?.contains(query) == true
                    }
                }

                // Filter history based on selected period
                val periodFilteredHistory = filterByPeriod(searchFilteredHistory, selectedPeriod, customStartDate, customEndDate)

                if (periodFilteredHistory.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Aucun résultat pour \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = periodFilteredHistory,
                        key = { it.shipmentId }
                    ) { delivery ->
                        DeliveryHistoryCard(
                            delivery = delivery,
                            onClick = {
                                // Navigate to delivery details
                                userInfo?.driverId?.let { driverId ->
                                    val shipmentIdInt = delivery.shipmentId.toIntOrNull() ?: 0
                                    val driverIdInt = driverId.toIntOrNull() ?: 0
                                    navController.navigate(Screen.ShipmentDetail.createRoute(shipmentIdInt, driverIdInt))
                                }
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
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
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
                tint = Color(0xFF1976D2)
            )
            
            Text(
                text = "Aucun historique disponible",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                text = "Vos livraisons passées apparaîtront ici",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Badge informatif
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF0F4F8)
            ) {
                Text(
                    text = "Commencez vos livraisons pour voir l'historique",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color(0xFF1976D2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DriverStatsCard(stats: DriverStatsInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header avec titre et icône
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Statistiques de Performance",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section Statistiques avec fond clair
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F9FA)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NewStatItem(
                        label = "Trajets terminés",
                        value = stats.completedTrips.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF4CAF50)
                    )
                    
                    NewStatItem(
                        label = "Livraisons",
                        value = stats.deliveredShipments.toString(),
                        icon = Icons.Default.LocalShipping,
                        color = Color(0xFF1976D2)
                    )
                    
                    NewStatItem(
                        label = "Succès",
                        value = "${(stats.completedTrips.toFloat() / stats.totalTrips * 100).toInt()}%",
                        icon = Icons.Default.Percent,
                        color = Color(0xFF9C27B0)
                    )
                }
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilters(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit,
    periodOptions: List<String>,
    customStartDate: java.time.LocalDate?,
    customEndDate: java.time.LocalDate?,
    onCustomStartDateChange: (java.time.LocalDate?) -> Unit,
    onCustomEndDateChange: (java.time.LocalDate?) -> Unit,
    showStartDatePicker: Boolean,
    showEndDatePicker: Boolean,
    onShowStartDatePickerChange: (Boolean) -> Unit,
    onShowEndDatePickerChange: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header avec titre
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recherche et Filtres",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Search bar avec styling amélioré
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Rechercher une livraison...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Recherche")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedBorderColor = Color(0xFF1976D2).copy(alpha = 0.3f),
                    focusedBorderColor = Color(0xFF1976D2)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Section période avec dropdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0F4F8)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Période d'historique",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Dropdown pour la période
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedPeriod,
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                unfocusedBorderColor = Color(0xFF1976D2).copy(alpha = 0.3f),
                                focusedBorderColor = Color(0xFF1976D2)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            containerColor = Color.White
                        ) {
                            periodOptions.forEach { period ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = period,
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        ) 
                                    },
                                    onClick = {
                                        onPeriodChange(period)
                                        expanded = false
                                    },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    // Custom date pickers if "Période personnalisée" is selected
                    if (selectedPeriod == "Période personnalisée") {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Date de début
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customStartDate?.format(
                                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy", java.util.Locale.FRENCH)
                                ) ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Date de début") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color(0xFF1976D2).copy(alpha = 0.3f),
                                    focusedBorderColor = Color(0xFF1976D2)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onShowStartDatePickerChange(true) },
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("Choisir")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Date de fin
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customEndDate?.format(
                                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy", java.util.Locale.FRENCH)
                                ) ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Date de fin") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color(0xFF1976D2).copy(alpha = 0.3f),
                                    focusedBorderColor = Color(0xFF1976D2)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onShowEndDatePickerChange(true) },
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("Choisir")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryHistoryCard(delivery: DeliveryHistoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = formatDate(delivery.tripDate),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    delivery.tripNumber?.let { tripNo ->
                        Text(
                            text = "Trajet: $tripNo",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                StatusChip(status = delivery.shipmentStatus)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section informations avec fond clair
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F9FA)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Client information
                    delivery.clientName?.let { client ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = client,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Client",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    // Destination information
                    if (delivery.originName != null && delivery.destinationName != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "${delivery.originName} → ${delivery.destinationName}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Trajet",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Trip summary avec style Home
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoItem(
                    icon = Icons.Default.Inventory,
                    label = "Quantité",
                    value = "${delivery.quantity} ${delivery.uom}",
                    color = Color(0xFF1976D2)
                )
                InfoItem(
                    icon = Icons.Default.LocalShipping,
                    label = "Véhicule",
                    value = delivery.vehicleName ?: "N/A",
                    color = Color(0xFF4CAF50)
                )
                InfoItem(
                    icon = Icons.Default.CheckCircle,
                    label = "POD",
                    value = if (delivery.podDone == true) "Oui" else "Non",
                    color = if (delivery.podDone == true) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String?) {
    val statusUpper = status?.uppercase() ?: "UNKNOWN"
    val (color, text) = when (statusUpper) {
        "DELIVERED", "COMPLETED" -> Color(0xFF4CAF50) to "Livré"
        "EXPEDITION" -> Color(0xFF1976D2) to "Expédié"
        "TO_PLAN" -> Color(0xFFFF9800) to "À planifier"
        "CANCELLED" -> Color(0xFFD32F2F) to "Annulé"
        else -> Color(0xFF9E9E9E) to (status ?: "Inconnu")
    }
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String, color: Color = Color(0xFF1976D2)) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

// Helper functions
private fun formatDate(dateString: String): String {
    return try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val date = java.time.LocalDateTime.parse(dateString, formatter)
        val today = java.time.LocalDateTime.now()
        
        // Format de base
        val frenchFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH)
        val formattedDate = date.format(frenchFormatter)
        
        // Ajouter un indicateur si c'est aujourd'hui ou hier
        val daysDiff = java.time.Duration.between(date.toLocalDate().atStartOfDay(), today.toLocalDate().atStartOfDay()).toDays()
        
        when {
            daysDiff == 0L -> "Aujourd'hui"
            daysDiff == 1L -> "Hier"
            daysDiff <= 7L -> "Il y a ${daysDiff} jours"
            else -> formattedDate
        }
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

// Helper function to filter history by period
private fun filterByPeriod(
    history: List<DeliveryHistoryItem>,
    selectedPeriod: String,
    customStartDate: java.time.LocalDate?,
    customEndDate: java.time.LocalDate?
): List<DeliveryHistoryItem> {
    val now = java.time.LocalDate.now()
    
    println("🔍 DEBUG: Selected period: $selectedPeriod")
    println("🔍 DEBUG: Total history items: ${history.size}")
    
    return when (selectedPeriod) {
        "Période personnalisée" -> {
            if (customStartDate != null && customEndDate != null) {
                println("🔍 DEBUG: Custom range: $customStartDate to $customEndDate")
                history.filter { item ->
                    try {
                        val itemDate = parseDate(item.tripDate)
                        if (itemDate != null) {
                            val result = !itemDate.isBefore(customStartDate) && !itemDate.isAfter(customEndDate)
                            println("🔍 DEBUG: Item date $itemDate, in range: $result")
                            result
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        println("❌ DEBUG: Error parsing date ${item.tripDate}: ${e.message}")
                        false
                    }
                }
            } else {
                println("🔍 DEBUG: Custom dates not set, returning all")
                history
            }
        }
        "Dernière semaine" -> {
            val oneWeekAgo = now.minusWeeks(1)
            history.filter { item ->
                try {
                    println("🔍 DEBUG: Parsing tripDate: ${item.tripDate}")
                    val itemDate = parseDate(item.tripDate)
                    if (itemDate != null) {
                        val result = !itemDate.isBefore(oneWeekAgo)
                        println("🔍 DEBUG: Item date $itemDate, oneWeekAgo $oneWeekAgo, result: $result")
                        result
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    println("❌ DEBUG: Error parsing date ${item.tripDate}: ${e.message}")
                    false
                }
            }
        }
        "30 derniers jours" -> {
            val thirtyDaysAgo = now.minusDays(30)
            history.filter { item ->
                try {
                    val itemDate = parseDate(item.tripDate)
                    if (itemDate != null) {
                        !itemDate.isBefore(thirtyDaysAgo)
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
        }
        "90 derniers jours" -> {
            val ninetyDaysAgo = now.minusDays(90)
            history.filter { item ->
                try {
                    val itemDate = parseDate(item.tripDate)
                    if (itemDate != null) {
                        !itemDate.isBefore(ninetyDaysAgo)
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
        }
        "6 derniers mois" -> {
            val sixMonthsAgo = now.minusMonths(6)
            history.filter { item ->
                try {
                    val itemDate = parseDate(item.tripDate)
                    if (itemDate != null) {
                        !itemDate.isBefore(sixMonthsAgo)
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
        }
        "Cette année" -> {
            val startOfYear = java.time.LocalDate.of(now.year, 1, 1)
            history.filter { item ->
                try {
                    val itemDate = parseDate(item.tripDate)
                    if (itemDate != null) {
                        !itemDate.isBefore(startOfYear)
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
        }
        else -> {
            println("🔍 DEBUG: No filtering for: $selectedPeriod")
            history // "Toutes les livraisons passées" - no filtering
        }
    }
}

// Helper function to parse date with multiple formats
private fun parseDate(dateString: String): java.time.LocalDate? {
    val formats = listOf(
        "yyyy-MM-dd",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "dd/MM/yyyy",
        "MM/dd/yyyy"
    )
    
    for (format in formats) {
        try {
            val formatter = java.time.format.DateTimeFormatter.ofPattern(format)
            return java.time.LocalDate.parse(dateString, formatter)
        } catch (e: Exception) {
            // Try next format
        }
    }
    
    println("❌ DEBUG: Failed to parse date with all formats: $dateString")
    return null
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
