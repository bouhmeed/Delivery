// HomeScreen.kt – UI layer only, all data handled by HomeViewModel
package com.example.delivery.screens.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons 
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.delivery.components.TodayTourCard
import com.example.delivery.components.VehicleMaintenanceSection
import com.example.delivery.components.CommonTopAppBar
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.auth.AuthManager
import com.example.delivery.viewmodel.home.HomeViewModel
import com.example.delivery.viewmodel.home.HomeUiState
import com.example.delivery.viewmodel.delivery.TodayTourViewModel
import com.example.delivery.viewmodel.delivery.TodayTourState
import com.example.delivery.viewmodel.delivery.ShipmentSearchViewModel
import com.example.delivery.models.delivery.ShipmentSearchState
import com.example.delivery.models.delivery.ShipmentSearchData
import com.example.delivery.screens.components.ManualEntryDialog
import com.example.delivery.screens.delivery.ShipmentDetailScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    // Authentication
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val userEmail = authManager.getUserEmail()

    // ViewModels
    val homeViewModel: HomeViewModel = viewModel()
    val unreadCount by homeViewModel.unreadCount.collectAsStateWithLifecycle()
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    
    val todayTourViewModel: TodayTourViewModel = viewModel()

    // Trigger loading of today tour when driver info becomes available
    LaunchedEffect((uiState as? HomeUiState.Success)?.driverInfo?.id) {
        val driverIdString = (uiState as? HomeUiState.Success)?.driverInfo?.id
        driverIdString?.toIntOrNull()?.let { id ->
            todayTourViewModel.setDriverId(id)
        }
    }

    val todayTourState by todayTourViewModel.todayTourState.collectAsStateWithLifecycle()
    val shipmentSearchViewModel: ShipmentSearchViewModel = viewModel()
    val shipmentSearchState by shipmentSearchViewModel.searchState.collectAsStateWithLifecycle()

    // Load data when email becomes available
    LaunchedEffect(userEmail) {
        userEmail?.let { email ->
            homeViewModel.loadData(email)
            homeViewModel.loadUnreadCount()
        }
    }

    // UI state for dialogs
    var showManualEntryDialog by remember { mutableStateOf(false) }
    var showShipmentDetail by remember { mutableStateOf(false) }
    var selectedShipmentData: ShipmentSearchData? by remember { mutableStateOf(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Accueil",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF102A43) // deep navy blue
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            userEmail?.let { email ->
                                homeViewModel.loadData(email)
                                homeViewModel.loadUnreadCount()
                            }
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .shadow(elevation = 2.dp, shape = CircleShape)
                            .background(
                                color = Color.White, // floating white circular background
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFF1976D2) // primary brand blue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White, // white background as requested
                    titleContentColor = Color(0xFF102A43)
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top spacer for separation from TopAppBar
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. Modern CurrentDayCard with glassmorphism effect
            item {
                val successState = uiState as? HomeUiState.Success
                val firstName = successState?.userInfo?.firstName ?: ""
                val lastName = successState?.userInfo?.lastName ?: ""
                val driverName = if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                    "$firstName $lastName".trim()
                } else {
                    "Chauffeur"
                }
                val todayDateString = remember {
                    SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH).format(Date()).replaceFirstChar { it.uppercase() }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(
                            elevation = 12.dp,
                            spotColor = Color(0xFF1976D2).copy(alpha = 0.3f),
                            ambientColor = Color(0xFF1976D2).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFE3F2FD), // very pale blue
                                        Color(0xFFBBDEFB), // light blue
                                        Color(0xFF90CAF9)  // medium light blue
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Bonjour,",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF102A43).copy(alpha = 0.7f)
                            )
                            Text(
                                text = driverName,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF102A43)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = todayDateString,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF102A43).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Loading / Error / Success Body content
            when (uiState) {
                is HomeUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is HomeUiState.Error -> {
                    val message = (uiState as HomeUiState.Error).message
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Erreur de chargement",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFFD32F2F)
                                )
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFB71C1C)
                                )
                            }
                        }
                    }
                }
                is HomeUiState.Success -> {
                    val data = uiState as HomeUiState.Success

                    // 3. Elegant Soft-Amber Alert Banner (Replaces MaintenanceAlertCard)
                    if (data.maintenanceAlert != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFAF0)),
                                border = BorderStroke(1.dp, Color(0xFFEA580C).copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = Color(0xFFEA580C),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Maintenance ${data.maintenanceAlert.type}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFFC2410C)
                                        )
                                        data.maintenanceAlert.vehicleName?.let { name ->
                                            Text(
                                                text = "$name • ${data.maintenanceAlert.registration ?: ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFEA580C)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Tournée du jour / Progress Hub
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            when (val state = todayTourState) {
                                is TodayTourState.Loading -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                                is TodayTourState.Success -> {
                                    TodayTourCard(
                                        tourInfo = state.tourInfo,
                                        statistics = state.statistics,
                                        navController = navController,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(24.dp)),
                                        onStartTour = { navController.navigate("tournee") },
                                        onViewDetails = { navController.navigate("tournee") },
                                        onCompleteShipment = { /* TODO */ }
                                    )
                                }
                                is TodayTourState.NoTour -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalShipping,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = Color(0xFF1976D2).copy(alpha = 0.2f)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Aucune tournée planifiée",
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                color = Color.Black
                                            )
                                            Text(
                                                text = "Aucune livraison n'est prévue pour aujourd'hui",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                is TodayTourState.Error -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = null,
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Erreur de la tournée",
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFFD32F2F)
                                            )
                                            Text(
                                                text = state.message,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFFB71C1C)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Vehicle Maintenance Section
                    if (data.vehicleMaintenance.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                VehicleMaintenanceSection(
                                    maintenance = data.vehicleMaintenance
                                )
                            }
                        }
                    }

                    // 5. Quick Actions Card (Keep modern look but optimized for standard quick search layout)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "Rechercher une Expédition",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1976D2)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showManualEntryDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1976D2)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = "Entrée manuelle",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Entrer Manuellement / Scanner",
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Extra bottom padding
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Shipment search handling
    LaunchedEffect(shipmentSearchState) {
        when (val current = shipmentSearchState) {
            is ShipmentSearchState.Success -> {
                selectedShipmentData = current.data
                showShipmentDetail = true
                showManualEntryDialog = false
            }
            is ShipmentSearchState.Error -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(current.message) }
                showManualEntryDialog = false
            }
            else -> {}
        }
    }

    if (showManualEntryDialog) {
        ManualEntryDialog(
            isVisible = showManualEntryDialog,
            onClose = {
                showManualEntryDialog = false
                shipmentSearchViewModel.clearSearchState()
            },
            onSearch = { barcode -> shipmentSearchViewModel.searchManually(barcode) },
            isLoading = shipmentSearchState is ShipmentSearchState.Loading
        )
    }

    if (showShipmentDetail && selectedShipmentData != null) {
        ShipmentDetailScreen(
            shipmentData = selectedShipmentData!!,
            onNavigateBack = {
                showShipmentDetail = false
                selectedShipmentData = null
                shipmentSearchViewModel.clearSearchState()
            },
            onMarkAsDelivered = {
                selectedShipmentData?.let { data ->
                    shipmentSearchViewModel.markAsDelivered(data.shipment.id)
                }
            },
            onNavigate = { /* TODO: GPS navigation */ },
            navController = navController
        )
    }
}
