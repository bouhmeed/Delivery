package com.example.delivery.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.delivery.models.TourInfo
import com.example.delivery.models.TourStatistics

@Composable
fun TodayTourCard(
    tourInfo: TourInfo,
    statistics: TourStatistics,
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    onStartTour: () -> Unit = {},
    onViewDetails: () -> Unit = {},
    onCompleteShipment: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = statistics.completionPercentage / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header avec titre et statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tournée du Jour",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = "#${tourInfo.tripId}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
                
                // Badge de statut
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (tourInfo.status) {
                        "IN_PROGRESS" -> Color(0xFF4CAF50)
                        "COMPLETED" -> Color(0xFF2196F3)
                        else -> Color(0xFF1976D2)
                    }
                ) {
                    Text(
                        text = when (tourInfo.status) {
                            "IN_PROGRESS" -> "En cours"
                            "COMPLETED" -> "Terminée"
                            else -> "Planifiée"
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section Statistiques
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F9FA)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = Icons.Default.Inventory,
                        label = "Total",
                        value = statistics.totalShipments.toString(),
                        color = Color(0xFF1976D2)
                    )
                    StatItem(
                        icon = Icons.Default.CheckCircle,
                        label = "Complétées",
                        value = statistics.completedShipments.toString(),
                        color = Color(0xFF4CAF50)
                    )
                    StatItem(
                        icon = Icons.Default.Pending,
                        label = "Restantes",
                        value = statistics.remainingShipments.toString(),
                        color = Color(0xFFFF9800)
                    )
                    StatItem(
                        icon = Icons.Default.Percent,
                        label = "Progression",
                        value = "${statistics.completionPercentage}%",
                        color = Color(0xFF9C27B0)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section Progression
            Column {
                Text(
                    text = "Progression de la tournée",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Barre de progression améliorée
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(
                            Color(0xFFF0F0F0),
                            RoundedCornerShape(14.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .background(
                                when {
                                    statistics.completionPercentage >= 80 -> Color(0xFF4CAF50)
                                    statistics.completionPercentage >= 50 -> Color(0xFF2196F3)
                                    else -> Color(0xFFFF9800)
                                },
                                RoundedCornerShape(14.dp)
                            )
                    )
                    
                    // Texte de pourcentage
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${statistics.completedShipments}/${statistics.totalShipments} livraisons",
                            color = if (animatedProgress > 0.3f) Color.White else Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        // Naviguer vers le suivi des tournées avec la date spécifique de la tournée
                        navController?.navigate("delivery?date=${tourInfo.date.substring(0, 10)}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Validé",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Validée")
                }
                
                OutlinedButton(
                    onClick = {
                        // Naviguer vers les détails du trajet
                        navController?.navigate("tripDetail/${tourInfo.id}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF1976D2)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFF1976D2)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Détail",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Détail")
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
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
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}
