package com.example.delivery.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.delivery.models.ShipmentDetail

@Composable
fun ShipmentDetailCard(
    shipment: ShipmentDetail,
    onDeliver: () -> Unit
) {
    val statusColor = when (shipment.status.uppercase()) {
        "DELIVERED" -> MaterialTheme.colorScheme.tertiary
        "IN_TRANSIT" -> MaterialTheme.colorScheme.secondary
        "PENDING" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    
    val statusText = when (shipment.status.uppercase()) {
        "DELIVERED" -> "Livrée"
        "IN_TRANSIT" -> "En transit"
        "PENDING" -> "En attente"
        else -> shipment.status
    }
    
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
            // Header avec ID et statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expédition #${shipment.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Informations sur l'origine et la destination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Origine
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Origine",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = shipment.origin?.address ?: "Adresse non spécifiée",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }
                
                // Flèche
                Icon(
                    Icons.Default.East,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 8.dp)
                )
                
                // Destination
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Destination",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = shipment.destination?.address ?: "Adresse non spécifiée",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Détails supplémentaires
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Client
                Column {
                    Text(
                        text = "Client",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = shipment.customerName ?: "Client #${shipment.customerId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Poids
                shipment.weight?.let { weight ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Poids",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${weight} kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Description si présente
            if (shipment.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = shipment.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
        }
    }
}
