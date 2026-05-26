// HomeScreen.kt – UI layer only, all data handled by HomeViewModel
package com.example.delivery.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons 
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
// Added import for FontWeight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext

// Updated lambda for onMarkAsDelivered to use selectedShipmentData

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavController

import com.example.delivery.components.*
import com.example.delivery.auth.AuthManager
import com.example.delivery.viewmodel.home.HomeViewModel
import com.example.delivery.viewmodel.home.HomeUiState
import com.example.delivery.viewmodel.delivery.TodayTourViewModel
import com.example.delivery.viewmodel.delivery.TodayTourState
import com.example.delivery.viewmodel.delivery.ShipmentSearchViewModel
import com.example.delivery.models.delivery.ShipmentSearchState
import com.example.delivery.models.delivery.ShipmentSearchData
import androidx.compose.runtime.*
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
    // Observe unread notification count
    val unreadCount by homeViewModel.unreadCount.collectAsStateWithLifecycle()
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    // ViewModel for today tour
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
            // Load unread notifications once user data is available
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
            CommonTopAppBar(
                title = "Accueil",
                showBack = false,
                onBack = {},
                showRefresh = true,
                onRefresh = {
                    // Trigger data reload
                    userEmail?.let { email ->
                        homeViewModel.loadData(email)
                        homeViewModel.loadUnreadCount()
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
            when (uiState) {
                is HomeUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
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
            // Trigger loading of today tour when driver info is available
// Removed inline LaunchedEffect; driver ID loading moved to top-level
                    // Current Day Card
                    item {
                        CurrentDayCard(
                            userInfo = data.userInfo,
                            driverInfo = data.driverInfo,
                            isLoading = false
                        )
                    }
                    // Maintenance Alert Card
                    item {
                        MaintenanceAlertCard(alert = data.maintenanceAlert)
                    }
                    // Today Tour Card (uses todayTourState)
                    item {
                        when (val state = todayTourState) {
                            is TodayTourState.Loading -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text("Chargement de la tournée…")
                                    }
                                }
                            }
                            is TodayTourState.Success -> {
                                TodayTourCard(
                                    tourInfo = state.tourInfo,
                                    statistics = state.statistics,
                                    navController = navController,
                                    onStartTour = { navController.navigate("tournee") },
                                    onViewDetails = { navController.navigate("tournee") },
                                    onCompleteShipment = { /* TODO */ }
                                )
                            }
                            is TodayTourState.NoTour -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = Color(0xFF1976D2)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Aucune tournée prévue",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "Aucune livraison n'est prévue pour aujourd'hui",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
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
                                            text = "Erreur de chargement",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
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
                    // Vehicle Maintenance Section
                    if (data.vehicleMaintenance.isNotEmpty()) {
                        item {
                            VehicleMaintenanceSection(maintenance = data.vehicleMaintenance)
                        }
                    }
                    // Quick Actions Card
                    item {
                        QuickActionsCard(onManualEntry = { showManualEntryDialog = true })
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
