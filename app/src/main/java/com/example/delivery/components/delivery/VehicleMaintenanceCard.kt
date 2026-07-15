package com.example.delivery.components.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delivery.repository.delivery.VehicleMaintenanceInfo
import com.airbnb.lottie.compose.*

private val CardBlue = Color(0xFF05204A)
private val CardBlueDark = Color(0xFF084A9E)
private val PureWhite = Color(0xFFFFFFFF)
private val WarningColor = Color(0xFFFF9F0A)

@Composable
fun VehicleMaintenanceCard(
    maintenanceInfo: VehicleMaintenanceInfo,
    modifier: Modifier = Modifier
) {
    // Lottie composition for gears animation
    val composition = rememberLottieComposition(LottieCompositionSpec.Asset("Gears Lottie Animation.json"))

    if (!maintenanceInfo.hasMaintenance) {
        // No maintenance data card
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Lottie animation as background
                LottieAnimation(
                    composition = composition.value,
                    iterations = LottieConstants.IterateForever,
                    isPlaying = true,
                    modifier = Modifier.matchParentSize()
                )

                // Gradient overlay for readability
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    CardBlue.copy(alpha = 0.85f),
                                    CardBlueDark.copy(alpha = 0.85f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = PureWhite.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Aucune maintenance planifiée",
                            fontSize = 14.sp,
                            color = PureWhite.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Lottie animation as background
            LottieAnimation(
                composition = composition.value,
                iterations = LottieConstants.IterateForever,
                isPlaying = true,
                modifier = Modifier.matchParentSize()
            )

            // Gradient overlay for readability
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                CardBlue.copy(alpha = 0.85f),
                                CardBlueDark.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with icon and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Maintenance Véhicule",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }

                    // Urgent warning badge
                    if (maintenanceInfo.isUrgent) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(
                                    color = WarningColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Urgent",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WarningColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Maintenance details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Type",
                            fontSize = 12.sp,
                            color = PureWhite.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = maintenanceInfo.maintenanceType,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PureWhite
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Prochaine maintenance",
                            fontSize = 12.sp,
                            color = PureWhite.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (maintenanceInfo.nextMaintenanceDate.isNotEmpty()) {
                                maintenanceInfo.nextMaintenanceDate
                            } else {
                                "Non planifiée"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (maintenanceInfo.isUrgent) WarningColor else PureWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Days until maintenance
                if (maintenanceInfo.nextMaintenanceDate.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when {
                                maintenanceInfo.daysUntilNextMaintenance < 0 -> "En retard de ${-maintenanceInfo.daysUntilNextMaintenance} jours"
                                maintenanceInfo.daysUntilNextMaintenance == 0 -> "Aujourd'hui"
                                maintenanceInfo.daysUntilNextMaintenance == 1 -> "Demain"
                                else -> "Dans ${maintenanceInfo.daysUntilNextMaintenance} jours"
                            },
                            fontSize = 14.sp,
                            color = if (maintenanceInfo.isUrgent) WarningColor else PureWhite.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
