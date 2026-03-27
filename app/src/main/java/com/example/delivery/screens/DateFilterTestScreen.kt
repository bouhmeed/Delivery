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
import com.example.delivery.components.DateFilterRow
import com.example.delivery.viewmodel.DeliveryTrackingViewModel
import com.example.delivery.viewmodel.TripWithDeliveriesState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterTestScreen(
    onBackPressed: () -> Unit = {},
    viewModel: DeliveryTrackingViewModel = viewModel()
) {
    val tripState by viewModel.tripWithDeliveriesState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    // Test with driver ID 3
    val testDriverId = 3
    
    LaunchedEffect(Unit) {
        viewModel.loadTripForDate(testDriverId, selectedDate)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Filtre Date") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
        ) {
            // Date Filter Component
            DateFilterRow(
                selectedDate = selectedDate,
                onDateSelected = { newDate ->
                    viewModel.setSelectedDate(newDate)
                    // Data will be reloaded by LaunchedEffect
                },
                onPreviousDay = { viewModel.goToPreviousDay(testDriverId) },
                onNextDay = { viewModel.goToNextDay(testDriverId) },
                onTodayClick = { viewModel.goToToday(testDriverId) }
            )
            
            // Test Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Test Driver ID: $testDriverId",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = "Selected Date: ${viewModel.formatDateForDisplay(selectedDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Is Today: ${viewModel.isToday(selectedDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Is Past: ${viewModel.isPast(selectedDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Results
            when (val state = tripState) {
                is TripWithDeliveriesState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                        Text("Chargement...")
                    }
                }
                
                is TripWithDeliveriesState.Success -> {
                    val data = state.data
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Résultats pour ${viewModel.formatDateForDisplay(selectedDate)}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Text(
                                text = "Tournée trouvée: ${data.trip != null}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Text(
                                text = "Nombre de livraisons: ${data.deliveries.size}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            if (data.trip != null) {
                                Text("Trip ID: ${data.trip.id}")
                                Text("Status: ${data.trip.status}")
                            }
                        }
                    }
                }
                
                is TripWithDeliveriesState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Erreur: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                is TripWithDeliveriesState.NoTripToday -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucune tournée pour cette date")
                    }
                }
            }
        }
    }
}
