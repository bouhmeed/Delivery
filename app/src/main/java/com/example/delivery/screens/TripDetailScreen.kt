package com.example.delivery.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.delivery.models.*
import com.example.delivery.components.TripStopCard
import com.example.delivery.components.ShipmentDetailCard
import com.example.delivery.network.DeliverShipmentRequest
import com.example.delivery.viewmodel.TripDetailViewModel
import com.example.delivery.viewmodel.TripDetailState
import com.example.delivery.viewmodel.TripListState
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: Int,
    navController: NavController,
    viewModel: TripDetailViewModel = viewModel()
) {
    val tripDetailState by viewModel.tripDetailState.collectAsState()
    
    // Charger les détails au démarrage
    LaunchedEffect(tripId) {
        viewModel.loadTripDetails(tripId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Détails du Trajet", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.loadTripDetails(tripId)
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualiser",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = tripDetailState) {
            is TripDetailState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chargement des détails...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            is TripDetailState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Erreur",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Erreur de chargement",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.loadTripDetails(tripId) },
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Réessayer")
                        }
                    }
                }
            }
            
            is TripDetailState.Success -> {
                TripDetailContent(
                    tripDetail = state.tripDetail,
                    paddingValues = paddingValues,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            
            TripDetailState.Idle -> {
                // Ne rien afficher
            }
        }
    }
}

@Composable
fun TripDetailContent(
    tripDetail: TripDetailData,
    paddingValues: PaddingValues,
    navController: NavController,
    viewModel: TripDetailViewModel
) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header avec informations principales
        item {
            TripHeaderCard(trip = tripDetail.trip)
        }
        
        // Carte de progression
        item {
            TripProgressCard(
                trip = tripDetail.trip,
                shipments = tripDetail.shipments
            )
        }
        
        // Actions rapides
        item {
            TripActionsCard(
                trip = tripDetail.trip,
                onStartTrip = { viewModel.startTrip(tripDetail.trip.id) },
                onCompleteTrip = { viewModel.completeTrip(tripDetail.trip.id) }
            )
        }
        
        // Informations chauffeur et véhicule
        item {
            DriverVehicleCard(
                driver = tripDetail.driver,
                vehicle = tripDetail.vehicle
            )
        }
        
        // Arrêts du trajet
        item {
            Text(
                text = "Arrêts du trajet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(tripDetail.stops.sortedBy { it.sequence }) { stop ->
            TripStopCard(
                stop = stop,
                shipments = tripDetail.shipments.filter { shipment ->
                    // Filtrer les shipments pour cet arrêt
                    when (stop.stopType.uppercase()) {
                        "PICKUP" -> shipment.originId == stop.locationId
                        "DELIVERY" -> shipment.destinationId == stop.locationId
                        else -> false
                    }
                }
            )
        }
        
        // Expéditions détaillées
        item {
            Text(
                text = "Expéditions (${tripDetail.shipments.size})",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(tripDetail.shipments) { shipment ->
            ShipmentDetailCard(
                shipment = shipment,
                onDeliver = { 
                    // TODO: Implémenter la livraison
                    viewModel.deliverShipment(
                        tripDetail.trip.id, 
                        shipment.id, 
                        DeliverShipmentRequest(
                            deliveryTime = java.time.Instant.now().toString(),
                            recipientName = "Client",
                            deliveryNotes = "Livré avec succès"
                        )
                    )
                }
            )
        }
        
        // Espace en bas
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TripHeaderCard(trip: TripDetail) {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH)
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.FRENCH)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // ID et statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trajet ${trip.tripId}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                TripStatusBadge(status = trip.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = try {
                        val date = java.time.LocalDate.parse(trip.tripDate.substring(0, 10))
                        date.format(formatter)
                    } catch (e: Exception) {
                        trip.tripDate.substring(0, 10)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Statistiques
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TripDetailStatItem(
                    icon = Icons.Default.LocalShipping,
                    label = "Expéditions",
                    value = "${trip.completedShipments}/${trip.totalShipments}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                TripDetailStatItem(
                    icon = Icons.Default.Route,
                    label = "Distance",
                    value = "${trip.totalDistance.toInt()} km",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                TripDetailStatItem(
                    icon = Icons.Default.Schedule,
                    label = "Durée",
                    value = "${trip.estimatedDuration} min",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun TripProgressCard(
    trip: TripDetail,
    shipments: List<ShipmentDetail>
) {
    val completionPercentage = if (trip.totalShipments > 0) {
        (trip.completedShipments.toFloat() / trip.totalShipments.toFloat() * 100).toInt()
    } else 0
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progression",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "$completionPercentage%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Barre de progression
            LinearProgressIndicator(
                progress = { completionPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Détails des expéditions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShipmentStatItem(
                    label = "Total",
                    value = trip.totalShipments.toString(),
                    color = MaterialTheme.colorScheme.outline
                )
                ShipmentStatItem(
                    label = "Livrées",
                    value = trip.completedShipments.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                ShipmentStatItem(
                    label = "Restantes",
                    value = (trip.totalShipments - trip.completedShipments).toString(),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun TripActionsCard(
    trip: TripDetail,
    onStartTrip: () -> Unit,
    onCompleteTrip: () -> Unit
) {
    val canStart = trip.status == "READY"
    val canComplete = trip.status == "IN_PROGRESS"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (canStart) {
                Button(
                    onClick = onStartTrip,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Démarrer")
                }
            }
            
            if (canComplete) {
                Button(
                    onClick = onCompleteTrip,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (canStart) 8.dp else 0.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Terminer")
                }
            }
            
            if (!canStart && !canComplete) {
                // Afficher le statut actuel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (trip.status) {
                            "PLANNING" -> "En planification"
                            "COMPLETED" -> "Terminé"
                            "CANCELLED" -> "Annulé"
                            else -> trip.status
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DriverVehicleCard(
    driver: DriverDetail,
    vehicle: VehicleDetail
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Équipe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Chauffeur
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = driver.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Chauffeur",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Véhicule
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = vehicle.registration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TripStatusBadge(status: String) {
    val (backgroundColor, contentColor, displayText) = when (status.uppercase()) {
        "PLANNING" -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Planification"
        )
        "READY" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Prêt"
        )
        "IN_PROGRESS" -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "En cours"
        )
        "COMPLETED" -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Terminé"
        )
        "CANCELLED" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Annulé"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            status
        )
    }
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Text(
            text = displayText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
fun TripDetailStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ShipmentStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}
