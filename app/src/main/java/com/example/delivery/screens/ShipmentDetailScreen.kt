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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.delivery.models.ShipmentSearchData
import com.example.delivery.models.ShipmentDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentDetailScreen(
    shipmentData: ShipmentSearchData,
    onNavigateBack: () -> Unit,
    onMarkAsDelivered: () -> Unit,
    onNavigate: () -> Unit,
    navController: NavController
) {
    val shipment = shipmentData.shipment
    val client = shipmentData.client
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du colis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Carte principale du colis
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header avec statut
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = shipment.shipmentNo,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                            if (shipment.trackingNumber != null) {
                                Text(
                                    text = "N° suivi: ${shipment.trackingNumber}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        // Badge de statut
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (shipment.status) {
                                "DELIVERED" -> Color(0xFF4CAF50)
                                "EXPEDITION" -> Color(0xFF2196F3)
                                "TO_PLAN" -> Color.Gray
                                else -> Color.Gray
                            }
                        ) {
                            Text(
                                text = when (shipment.status) {
                                    "DELIVERED" -> "Livré"
                                    "EXPEDITION" -> "En expédition"
                                    "TO_PLAN" -> "À planifier"
                                    else -> shipment.status
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Informations du colis
                    ShipmentInfoRow(
                        icon = Icons.Default.Description,
                        label = "Description",
                        value = shipment.description
                    )
                    
                    ShipmentInfoRow(
                        icon = Icons.Default.Inventory,
                        label = "Quantité",
                        value = "${shipment.quantity} unités"
                    )
                    
                    ShipmentInfoRow(
                        icon = Icons.Default.PriorityHigh,
                        label = "Priorité",
                        value = shipment.priority
                    )
                    
                    if (shipmentData.belongsToCurrentTour && shipmentData.tourSequence != null) {
                        ShipmentInfoRow(
                            icon = Icons.Default.Numbers,
                            label = "Position dans la tournée",
                            value = "Arrêt n°${shipmentData.tourSequence}"
                        )
                    }
                }
            }
            
            // Carte d'adresse de livraison
            if (client != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "📍 Adresse de livraison",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        ShipmentInfoRow(
                            icon = Icons.Default.Person,
                            label = "Client",
                            value = client.name
                        )
                        
                        ShipmentInfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Adresse",
                            value = shipment.deliveryAddress
                        )
                        
                        ShipmentInfoRow(
                            icon = Icons.Default.LocationCity,
                            label = "Ville",
                            value = "${shipment.deliveryCity} ${shipment.deliveryZipCode}"
                        )
                        
                        if (client.phone != null) {
                            ShipmentInfoRow(
                                icon = Icons.Default.Phone,
                                label = "Téléphone",
                                value = client.phone
                            )
                        }
                    }
                }
            }
            
            // Message si le colis n'appartient pas à la tournée
            if (!shipmentData.belongsToCurrentTour) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3CD)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Ce colis n'est pas assigné à votre tournée",
                            color = Color(0xFF856404),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Boutons d'action
            if (shipmentData.belongsToCurrentTour) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (shipment.status != "DELIVERED") {
                        Button(
                            onClick = onMarkAsDelivered,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("✅ Marquer comme livré")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onNavigate,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🗺️ Naviguer")
                    }
                }
            }
            
            // Bouton retour
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("❌ Retour")
            }
        }
    }
}

@Composable
private fun ShipmentInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF1976D2),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}
