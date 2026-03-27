package com.example.delivery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.delivery.models.Trip

@Composable
fun TripSelectionCard(
    trips: List<Trip>,
    selectedTripId: String?,
    onTripSelected: (Trip) -> Unit,
    modifier: Modifier = Modifier
) {
    if (trips.isEmpty()) return
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tournées du jour",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                Text(
                    text = "${trips.size} tournée${if (trips.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Trip list
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                trips.forEach { trip ->
                    TripItem(
                        trip = trip,
                        isSelected = selectedTripId != null && selectedTripId == trip.id,
                        onClick = { onTripSelected(trip) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TripItem(
    trip: Trip,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Color(0xFFE8F5E8)
    } else {
        Color(0xFFF5F5F5)
    }
    
    val borderColor = if (isSelected) {
        Color(0xFF4CAF50)
    } else {
        Color.Transparent
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trip info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = trip.tripId ?: "Tournée #${trip.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Vehicle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Véhicule",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Véhicule ${trip.vehicleId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                    
                    // Status
                    StatusBadge(status = trip.status)
                }
            }
            
            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Sélectionné",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (text, color, backgroundColor) = when (status) {
        "COMPLETED" -> Triple("Terminé", Color.White, Color(0xFF4CAF50))
        "IN_PROGRESS" -> Triple("En cours", Color.White, Color(0xFFFF9800))
        "READY" -> Triple("Prête", Color.White, Color(0xFF2196F3))
        "PLANNING" -> Triple("Planifiée", Color.White, Color(0xFF9E9E9E))
        else -> Triple(status, Color.White, Color(0xFF9E9E9E))
    }
    
    Box(
        modifier = Modifier
            .background(
                backgroundColor,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
