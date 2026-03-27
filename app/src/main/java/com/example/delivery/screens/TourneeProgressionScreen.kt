package com.example.delivery.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.delivery.components.*
import com.example.delivery.models.UserResponse
import com.example.delivery.viewmodel.ProgressionViewModel
import com.example.delivery.viewmodel.ProgressionUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourneeProgressionScreen(
    progressionViewModel: ProgressionViewModel = ProgressionViewModel()
) {
    val uiState by progressionViewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Mock current driver - in real app, get from auth/session
    val currentDriver = remember { 
        UserResponse(
            id = "5",
            tenantId = "1",
            email = "driver@example.com",
            firstName = "Pierre",
            lastName = "Bernard",
            driverId = "5",
            role = "DRIVER",
            isActive = true,
            createdAt = "2026-03-26T10:00:00Z",
            updatedAt = "2026-03-26T10:00:00Z"
        )
    }
    
    // Set driver ID when screen loads
    LaunchedEffect(currentDriver) {
        currentDriver.driverId?.let { driverId ->
            progressionViewModel.setDriverId(driverId.toInt())
        }
    }
    
    // Content without outer Column (already wrapped in parent Card)
    // Pas de header - contenu direct dans la Card parente blanche
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Progression Section
    val currentState = uiState
    when (currentState) {
        is ProgressionUiState.Loading -> {
            ProgressLoadingSection()
        }
        
        is ProgressionUiState.Success -> {
            ProgressSection(
                progress = currentState.tripWithProgress.progress
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Additional trip details
            TripDetailsSection(trip = currentState.tripWithProgress.trip)
        }
        
        is ProgressionUiState.NoTripToday -> {
            NoTripTodaySection()
        }
        
        is ProgressionUiState.Error -> {
            ProgressErrorSection(
                errorMessage = currentState.message,
                onRetry = {
                    progressionViewModel.refreshProgress()
                }
            )
        }
    }
}

@Composable
private fun TripDetailsSection(trip: com.example.delivery.models.Trip) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📋 Détails de la Tournée",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DetailRow("ID Tournée:", trip.tripId ?: "N/A")
        DetailRow("Statut:", getStatusText(trip.status))
        DetailRow("Date:", trip.tripDate?.let { formatDate(it) } ?: "N/A")
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActionButtons() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { /* Navigate to shipments list */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voir les Livraisons")
        }
        
        OutlinedButton(
            onClick = { /* Navigate to map */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voir sur la Carte")
        }
    }
}

private fun getStatusText(status: String): String {
    return when (status) {
        "PLANNING" -> "Planifiée"
        "IN_PROGRESS" -> "En cours"
        "COMPLETED" -> "Terminée"
        else -> status
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            .parse(dateString)
        val formatted = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
        formatted.format(date ?: java.util.Date())
    } catch (e: Exception) {
        dateString
    }
}
