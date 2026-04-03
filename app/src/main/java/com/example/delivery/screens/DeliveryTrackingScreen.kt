package com.example.delivery.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.delivery.components.*
import com.example.delivery.components.DeliveryItemCard
import com.example.delivery.components.DeliveryStatsCard
import com.example.delivery.models.DeliveryItem
import com.example.delivery.screens.NewShipmentDetailScreen
import com.example.delivery.viewmodel.DeliveryTrackingViewModel
import com.example.delivery.viewmodel.DeliveryStats
import com.example.delivery.viewmodel.OperationState
import com.example.delivery.viewmodel.TripWithDeliveriesState
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryTrackingScreen(
    driverId: Int,
    onNavigateToDelivery: (DeliveryItem) -> Unit = {},
    onNavigateToMap: (DeliveryItem) -> Unit = {},
    onBackPressed: () -> Unit = {},
    onValidationClick: (DeliveryItem) -> Unit = {},
    onCallClick: (DeliveryItem) -> Unit = {},
    navController: NavController = rememberNavController(),
    viewModel: DeliveryTrackingViewModel = viewModel()
) {
    val tripState by viewModel.tripWithDeliveriesState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Load data on first composition and when date changes
    LaunchedEffect(driverId, selectedDate) {
        viewModel.loadTripForDate(driverId, selectedDate)
    }
    
    // Handle operation state changes
    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is OperationState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.clearOperationState()
                    // Refresh data for current selected date
                    viewModel.refresh(driverId)
                }
            }
            is OperationState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.clearOperationState()
                }
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suivi de mes Tournées") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh(driverId) }
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualiser"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date Filter Row
            DateFilterRow(
                selectedDate = selectedDate,
                onDateSelected = { newDate ->
                    viewModel.setSelectedDate(newDate)
                    // Data will be reloaded by LaunchedEffect
                },
                onPreviousDay = { viewModel.goToPreviousDay(driverId) },
                onNextDay = { viewModel.goToNextDay(driverId) },
                onTodayClick = { viewModel.goToToday(driverId) }
            )
            
            // Content based on state
            when (val state = tripState) {
                is TripWithDeliveriesState.Loading -> {
                    LoadingContent()
                }
                is TripWithDeliveriesState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.refresh(driverId) }
                    )
                }
                is TripWithDeliveriesState.Success -> {
                    if (state.data.trip == null) {
                        NoTripTodayContent(
                            onRefresh = { viewModel.refresh(driverId) }
                        )
                    } else {
                        TripContent(
                            trip = state.data.trip,
                            deliveries = state.data.deliveries,
                            onNavigateToDelivery = onNavigateToDelivery,
                            onNavigateToMap = onNavigateToMap,
                            onValidationClick = onValidationClick,
                            onCallClick = onCallClick,
                            onCompleteDelivery = { delivery ->
                                viewModel.completeDelivery(delivery.shipmentId)
                            },
                            onNavigateToDetails = onNavigateToDelivery,
                            onStatusChange = { delivery, newStatus ->
                                // Utiliser le tripShipmentLinkId si disponible, sinon le shipmentId
                                val tripShipmentLinkId = delivery.tripShipmentLinkId ?: delivery.shipmentId
                                viewModel.updateTripShipmentStatus(tripShipmentLinkId, newStatus, driverId)
                            }
                        )
                    }
                }
                else -> {
                    // Handle any other states
                    ErrorContent(
                        message = "État inconnu",
                        onRetry = { viewModel.refresh(driverId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Chargement des livraisons...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TripContent(
    trip: com.example.delivery.models.Trip,
    deliveries: List<DeliveryItem>,
    onNavigateToDelivery: (DeliveryItem) -> Unit,
    onNavigateToMap: (DeliveryItem) -> Unit,
    onValidationClick: (DeliveryItem) -> Unit,
    onCallClick: (DeliveryItem) -> Unit,
    onCompleteDelivery: (DeliveryItem) -> Unit,
    onNavigateToDetails: (DeliveryItem) -> Unit,
    onStatusChange: (DeliveryItem, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Stats card
        item {
            DeliveryStatsCard(
                stats = DeliveryStats(
                    total = deliveries.size,
                    completed = deliveries.count { it.podDone },
                    inProgress = deliveries.count { !it.podDone && it.status == "EN_COURS" },
                    notStarted = deliveries.count { !it.podDone && (it.status == "NON_DEMARRE" || it.status == "ASSIGNED") },
                    completionPercentage = if (deliveries.isNotEmpty()) {
                        (deliveries.count { it.podDone } * 100 / deliveries.size)
                    } else {
                        0
                    }
                )
            )
        }
        
        // Delivery items
        items(deliveries) { delivery ->
            DeliveryItemCard(
                delivery = delivery,
                onItemClick = onNavigateToDetails,
                onCompleteClick = onCompleteDelivery,
                onNavigateClick = onNavigateToMap,
                onValidationClick = onValidationClick,
                onCallClick = onCallClick,
                onStatusChange = onStatusChange
            )
        }
    }
}

@Composable
private fun TripInfoCard(
    trip: com.example.delivery.models.Trip,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Tournée du jour",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            trip.tripId?.let { tripId ->
                Text(
                    text = "ID: $tripId",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "Statut: ${getTripStatusText(trip.status)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Erreur",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = "Erreur de chargement",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Réessayer",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Réessayer")
            }
        }
    }
}

@Composable
private fun NoTripTodayContent(
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = "Aucune tournée",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = "Aucune tournée aujourd'hui",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Vous n'avez pas de livraisons prévues pour cette date.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            OutlinedButton(
                onClick = onRefresh
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualiser",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Actualiser")
            }
        }
    }
}

@Composable
fun DeliveryTrackingScreenWithDetails(
    driverId: Int,
    onNavigateToDelivery: (DeliveryItem) -> Unit = {},
    onNavigateToMap: (DeliveryItem) -> Unit = {},
    onBackPressed: () -> Unit = {},
    onValidationClick: (DeliveryItem) -> Unit = {},
    onCallClick: (DeliveryItem) -> Unit = {},
    navController: NavController = rememberNavController(),
    viewModel: DeliveryTrackingViewModel = viewModel(),
    selectedDate: String? = null
) {
    // État pour gérer l'affichage des détails de livraison
    var showShipmentDetails by remember { mutableStateOf(false) }
    var selectedShipmentId by remember { mutableIntStateOf(0) }
    
    // État pour gérer l'affichage de la carte de navigation
    var showDriverMap by remember { mutableStateOf(false) }
    var selectedDeliveryForNavigation by remember { mutableStateOf<DeliveryItem?>(null) }
    
    // Si une date spécifique est fournie, la définir dans le ViewModel
    LaunchedEffect(selectedDate) {
        selectedDate?.let { dateString ->
            try {
                val localDate = LocalDate.parse(dateString)
                viewModel.setSelectedDate(localDate)
            } catch (e: Exception) {
                // En cas d'erreur de parsing, utiliser la date du jour
                println("Erreur de parsing de la date: $dateString")
            }
        }
    }
    
    // Fonction pour naviguer vers les détails
    val handleNavigateToDelivery: (DeliveryItem) -> Unit = { delivery ->
        showShipmentDetails = true
        selectedShipmentId = delivery.shipmentId
    }
    
    // Fonction pour naviguer vers la carte interne
    val handleNavigateToMap: (DeliveryItem) -> Unit = { delivery ->
        selectedDeliveryForNavigation = delivery
        showDriverMap = true
    }
    
    Box {
        DeliveryTrackingScreen(
            driverId = driverId,
            onNavigateToDelivery = handleNavigateToDelivery,
            onNavigateToMap = handleNavigateToMap,
            onBackPressed = onBackPressed,
            onValidationClick = onValidationClick,
            onCallClick = onCallClick,
            navController = navController,
            viewModel = viewModel
        )
        
        // Afficher l'écran de détails de livraison si nécessaire
        if (showShipmentDetails) {
            NewShipmentDetailScreen(
                shipmentId = selectedShipmentId,
                driverId = driverId,
                onNavigateBack = {
                    showShipmentDetails = false
                    selectedShipmentId = 0
                },
                onNavigateToMap = { address ->
                    // Utiliser la navigation interne avec la carte
                    selectedDeliveryForNavigation?.let { delivery ->
                        showDriverMap = true
                    }
                },
                navController = navController
            )
        }
        
        // Afficher l'écran de navigation interne si nécessaire
        if (showDriverMap && selectedDeliveryForNavigation != null) {
            DriverMapScreen(
                delivery = selectedDeliveryForNavigation!!,
                onBackPressed = {
                    showDriverMap = false
                    selectedDeliveryForNavigation = null
                },
                navController = navController
            )
        }
    }
}

private fun getTripStatusText(status: String): String {
    return when (status) {
        "PLANNING" -> "Planifiée"
        "IN_PROGRESS" -> "En cours"
        "COMPLETED" -> "Terminée"
        else -> status
    }
}
