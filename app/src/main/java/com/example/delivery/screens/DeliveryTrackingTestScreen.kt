package com.example.delivery.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.delivery.viewmodel.DeliveryTrackingViewModel
import com.example.delivery.viewmodel.TripWithDeliveriesState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryTrackingTestScreen(
    onBackPressed: () -> Unit = {},
    viewModel: DeliveryTrackingViewModel = viewModel()
) {
    val tripState by viewModel.tripWithDeliveriesState.collectAsState()
    
    // Test with driver ID 3 (from database example)
    val testDriverId = 3
    
    LaunchedEffect(Unit) {
        viewModel.loadTodayTripWithDeliveries(testDriverId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test - Suivi Tournées") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Test Driver ID: $testDriverId",
                style = MaterialTheme.typography.titleMedium
            )
            
            when (val state = tripState) {
                is TripWithDeliveriesState.Loading -> {
                    CircularProgressIndicator()
                    Text("Chargement...")
                }
                
                is TripWithDeliveriesState.Success -> {
                    val data = state.data
                    
                    Text(
                        text = "Tournée trouvée: ${data.trip != null}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    if (data.trip != null) {
                        Text("Trip ID: ${data.trip.id}")
                        Text("Status: ${data.trip.status}")
                        Text("Trip Identifier: ${data.trip.tripId}")
                    }
                    
                    Text(
                        text = "Nombre de livraisons: ${data.deliveries.size}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    data.deliveries.take(3).forEach { delivery ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text("Séquence: ${delivery.sequence}")
                                Text("Client: ${delivery.clientName}")
                                Text("Adresse: ${delivery.deliveryAddress}")
                                Text("Statut: ${delivery.status}")
                                Text("POD: ${delivery.podDone}")
                            }
                        }
                    }
                }
                
                is TripWithDeliveriesState.Error -> {
                    Text(
                        text = "Erreur: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                is TripWithDeliveriesState.NoTripToday -> {
                    Text("Aucune tournée aujourd'hui")
                }
            }
            
            Button(
                onClick = {
                    viewModel.loadTodayTripWithDeliveries(testDriverId)
                }
            ) {
                Text("Recharger")
            }
        }
    }
}
