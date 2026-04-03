package com.example.delivery.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.delivery.models.DeliveryItem
import com.example.delivery.services.NavigationService
import com.example.delivery.ui.DesignSystem
import com.example.delivery.viewmodel.DriverMapViewModel
// TEMPORARY: Using stub classes instead of real TomTom SDK due to missing Maven credentials
// import com.tomtom.sdk.maps.display.TomtomMap
// import com.tomtom.sdk.maps.display.TomtomMapCallback

/**
 * Driver Map Screen with integrated TomTom navigation - TEMPORARY STUB IMPLEMENTATION
 * Displays map, driver location, and route to delivery destination
 * Note: Using stub classes due to missing TomTom SDK Maven credentials
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverMapScreen(
    delivery: DeliveryItem,
    onBackPressed: () -> Unit,
    navController: NavController,
    viewModel: DriverMapViewModel = viewModel()
) {
    val context = LocalContext.current
    val navigationService = remember { NavigationService(context) }
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle()
    val routeInfo by viewModel.routeInfo.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    // Initialize navigation service and viewmodel
    LaunchedEffect(Unit) {
        viewModel.initializeNavigation(navigationService)
        viewModel.setDeliveryDestination(delivery)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Navigation") },
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
                        onClick = { viewModel.clearRoute() },
                        enabled = viewModel.isNavigationActive()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Effacer l'itinéraire"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // TomTom Map View - TEMPORARY STUB IMPLEMENTATION
            AndroidView(
                factory = { ctx ->
                    com.tomtom.sdk.map.display.MapView(ctx).apply {
                        getMapAsync { map ->
                            navigationService.initializeMap(map)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Route Information Overlay
            RouteInfoOverlay(
                routeInfo = routeInfo,
                isLoading = isLoading,
                navigationState = navigationState,
                deliveryAddress = viewModel.getCurrentDeliveryAddress(),
                onRetry = { viewModel.retryNavigation() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
            
            // Error Message Overlay
            errorMessage?.let { error ->
                ErrorMessageOverlay(
                    message = error,
                    onDismiss = { viewModel.clearError() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            
            // Navigation Controls
            NavigationControls(
                navigationState = navigationState,
                onStartNavigation = { viewModel.startNavigation() },
                onStopNavigation = { viewModel.stopNavigation() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

/**
 * Route information overlay showing distance, time, and status
 */
@Composable
private fun RouteInfoOverlay(
    routeInfo: com.example.delivery.services.RouteInfo?,
    isLoading: Boolean,
    navigationState: com.example.delivery.services.NavigationState,
    deliveryAddress: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DesignSystem.Colors.SURFACE_WHITE
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status and Address
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (navigationState) {
                            is com.example.delivery.services.NavigationState.Idle -> "Navigation inactive"
                            is com.example.delivery.services.NavigationState.MapReady -> "Carte prête"
                            is com.example.delivery.services.NavigationState.TrackingLocation -> "Localisation..."
                            is com.example.delivery.services.NavigationState.CalculatingRoute -> "Calcul de l'itinéraire..."
                            is com.example.delivery.services.NavigationState.RouteReady -> "Itinéraire prêt"
                            is com.example.delivery.services.NavigationState.Error -> "Erreur"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = deliveryAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else if (navigationState is com.example.delivery.services.NavigationState.Error) {
                    IconButton(onClick = onRetry) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Réessayer",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Route Information
            if (routeInfo != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RouteInfoItem(
                        icon = Icons.Default.DirectionsCar,
                        label = "Distance",
                        value = routeInfo.formatDistance()
                    )
                    
                    RouteInfoItem(
                        icon = Icons.Default.Schedule,
                        label = "Durée",
                        value = routeInfo.formatTime()
                    )
                    
                    RouteInfoItem(
                        icon = Icons.Default.TrendingUp,
                        label = "Trafic",
                        value = if (routeInfo.trafficDelayInSeconds > 0) {
                            "+${routeInfo.trafficDelayInSeconds / 60} min"
                        } else {
                            "Fluide"
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual route information item
 */
@Composable
private fun RouteInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Error message overlay
 */
@Composable
private fun ErrorMessageOverlay(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Erreur",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Navigation control buttons
 */
@Composable
private fun NavigationControls(
    navigationState: com.example.delivery.services.NavigationState,
    onStartNavigation: () -> Unit,
    onStopNavigation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (navigationState) {
                is com.example.delivery.services.NavigationState.MapReady,
                is com.example.delivery.services.NavigationState.TrackingLocation -> {
                    Button(
                        onClick = onStartNavigation,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DesignSystem.Colors.NAVIGATION_GREEN
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Démarrer",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Démarrer")
                    }
                }
                
                is com.example.delivery.services.NavigationState.RouteReady -> {
                    Button(
                        onClick = onStopNavigation,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DesignSystem.Colors.WARNING_ORANGE
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Arrêter",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Arrêter")
                    }
                }
                
                else -> {
                    // No controls for other states
                }
            }
        }
    }
}
