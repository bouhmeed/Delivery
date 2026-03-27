package com.example.delivery.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.content.Context
import kotlin.Result
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.components.TodayTourCard
import com.example.delivery.components.QuickActionsCard
import com.example.delivery.components.DayIndicatorCard
import com.example.delivery.components.CurrentDayCard
import com.example.delivery.navigation.Screen
import com.example.delivery.data.DeliveryData
import com.example.delivery.auth.AuthManager
import com.example.delivery.repository.UserRepository
import com.example.delivery.repository.DriverRepository
import com.example.delivery.repository.VehicleRepository
import com.example.delivery.models.UserResponse
import com.example.delivery.models.Driver
import com.example.delivery.models.Vehicle
import com.example.delivery.network.ApiClient
import com.example.delivery.viewmodel.TodayTourViewModel
import com.example.delivery.viewmodel.TodayTourState
import com.example.delivery.viewmodel.BarcodeScannerViewModel
import com.example.delivery.models.ScanResult
import com.example.delivery.viewmodel.ShipmentSearchViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.delivery.models.ShipmentSearchState
import com.example.delivery.models.ShipmentSearchData
import com.example.delivery.screens.ManualEntryDialog
import com.example.delivery.screens.ShipmentDetailScreen
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var selectedDate: LocalDate by remember { mutableStateOf(LocalDate.now()) }
    
    // Auth et récupération des données utilisateur
    val context: Context = LocalContext.current
    val authManager: AuthManager = remember { AuthManager(context) }
    val userEmail: String? = authManager.getUserEmail()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    
    // États pour les données utilisateur, chauffeur et véhicule
    var userInfo: UserResponse? by remember { mutableStateOf(null) }
    var driverInfo: Driver? by remember { mutableStateOf(null) }
    var vehicleInfo: Vehicle? by remember { mutableStateOf(null) }
    var isLoading: Boolean by remember { mutableStateOf(false) }
    
    // ViewModel pour la tournée du jour
    val todayTourViewModel: TodayTourViewModel = viewModel()
    val todayTourState: TodayTourState by todayTourViewModel.todayTourState.collectAsStateWithLifecycle()
    
    // Shipment Search ViewModel
    val shipmentSearchViewModel: ShipmentSearchViewModel = viewModel()
    val shipmentSearchState: ShipmentSearchState by shipmentSearchViewModel.searchState.collectAsStateWithLifecycle()
    
    // États pour les dialogues et navigation
    var showManualEntryDialog: Boolean by remember { mutableStateOf(false) }
    var showShipmentDetail: Boolean by remember { mutableStateOf(false) }
    var selectedShipmentData: ShipmentSearchData? by remember { mutableStateOf(null) }
    var showBarcodeScanner: Boolean by remember { mutableStateOf(false) }
    
    // Barcode Scanner ViewModel
    val barcodeScannerViewModel: BarcodeScannerViewModel = viewModel()
    val scanResult by barcodeScannerViewModel.scanResult
    
    // Récupérer les données utilisateur et chauffeur
    LaunchedEffect(userEmail) {
        userEmail?.let { email ->
            isLoading = true
            coroutineScope.launch {
                // Récupérer l'utilisateur
                val userRepository = UserRepository()
                userRepository.getUserByEmail(email)
                    .onSuccess { user ->
                        userInfo = user
                        
                        // Si l'utilisateur a un driverId, récupérer les infos du chauffeur
                        user.driverId?.let { driverId ->
                            try {
                                val driverIdInt = driverId.toInt()
                                // Configurer le driverId pour le ViewModel de la tournée
                                todayTourViewModel.setDriverId(driverIdInt)
                                shipmentSearchViewModel.setDriverId(driverIdInt)
                                
                                val driverRepository = DriverRepository()
                                driverRepository.getDriverById(driverId)
                                    .onSuccess { driver ->
                                        driverInfo = driver
                                        
                                        // Récupérer le véhicule du chauffeur
                                        val vehicleRepository = VehicleRepository()
                                        vehicleRepository.getVehicleByDriverId(driverId)
                                            .onSuccess { vehicle ->
                                                vehicleInfo = vehicle
                                            }
                                            .onFailure { error ->
                                                println("Erreur lors de la récupération du véhicule: ${error.message}")
                                            }
                                    }
                                    .onFailure { error ->
                                        println("Erreur lors de la récupération du chauffeur: ${error.message}")
                                    }
                            } catch (e: NumberFormatException) {
                                println("Erreur: driverId n'est pas un nombre valide: $driverId")
                            }
                        }
                    }
                    .onFailure { error ->
                        println("Erreur lors de la récupération de l'utilisateur: ${error.message}")
                    }
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tableau de bord") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                ),
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.Black)
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
            // Box Journée Actuelle - PREMIÈRE CARTE
            item {
                CurrentDayCard(
                    userInfo = userInfo,
                    driverInfo = driverInfo,
                    isLoading = isLoading
                )
            }
            
            // Tournée du jour
            item {
                val currentState = todayTourState
                when (currentState) {
                    is TodayTourState.Loading -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                                Text("Chargement de la tournée...")
                            }
                        }
                    }
                    is TodayTourState.Success -> {
                        TodayTourCard(
                            tourInfo = currentState.tourInfo,
                            statistics = currentState.statistics,
                            onStartTour = {
                                // Naviguer vers l'écran de démarrage de tournée
                                navController.navigate("tournee")
                            },
                            onViewDetails = {
                                // Naviguer vers les détails de la tournée
                                navController.navigate("tournee")
                            },
                            onCompleteShipment = {
                                // Marquer une livraison comme complétée
                                // TODO: Implémenter la logique de complétion de livraison
                                println("Marquer livraison comme complétée")
                            }
                        )
                    }
                    is TodayTourState.NoTour -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Aucune livraison n'est prévue pour aujourd'hui",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    is TodayTourState.Error -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
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
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFFD32F2F)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Erreur de chargement",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                )
                                Text(
                                    text = currentState.message,
                                    fontSize = 14.sp,
                                    color = Color(0xFFB71C1C)
                                )
                            }
                        }
                    }
                }
            }
            
            // Actions Rapides - DERNIÈRE CARTE
            item {
                QuickActionsCard(
                    onScanBarcode = {
                        showBarcodeScanner = true
                        barcodeScannerViewModel.startScanning()
                    },
                    onManualEntry = {
                        showManualEntryDialog = true
                    }
                )
            }
        }
    }
    
    // Gérer les résultats du scan
    LaunchedEffect(scanResult) {
        when (val result = scanResult) {
            is ScanResult.Success -> {
                showBarcodeScanner = false
                // TODO: Naviguer vers les détails du colis scanné
                println("Colis scanné: ${result.shipment}")
            }
            is ScanResult.Error -> {
                showBarcodeScanner = false
                // TODO: Afficher message d'erreur
                println("Erreur scan: ${result.message}")
            }
            else -> {}
        }
    }
    
    // Gérer les résultats de recherche
    LaunchedEffect(shipmentSearchState) {
        val currentState = shipmentSearchState
        when (currentState) {
            is ShipmentSearchState.Success -> {
                selectedShipmentData = currentState.data
                showShipmentDetail = true
                showManualEntryDialog = false
            }
            is ShipmentSearchState.Error -> {
                // Afficher un message d'erreur (TODO: Snackbar)
                println("Erreur recherche: ${currentState.message}")
                showManualEntryDialog = false
            }
            else -> {}
        }
    }
    
    // Dialog d'entrée manuelle
    if (showManualEntryDialog) {
        ManualEntryDialog(
            isVisible = showManualEntryDialog,
            onClose = {
                showManualEntryDialog = false
                shipmentSearchViewModel.clearSearchState()
            },
            onSearch = { barcode ->
                shipmentSearchViewModel.searchManually(barcode)
            },
            isLoading = shipmentSearchState is ShipmentSearchState.Loading
        )
    }
    
    // Écran de détails du colis
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
            onNavigate = {
                // TODO: Implémenter la navigation vers GPS
                println("Navigation vers l'adresse - TODO: GPS")
            },
            navController = navController
        )
    }
    
    // Écran de scan de codes-barres
    if (showBarcodeScanner) {
        BarcodeScannerScreen(
            onBarcodeScanned = { barcode ->
                barcodeScannerViewModel.onBarcodeScanned(barcode)
            },
            onClose = {
                showBarcodeScanner = false
                barcodeScannerViewModel.stopScanning()
            }
        )
    }
}
