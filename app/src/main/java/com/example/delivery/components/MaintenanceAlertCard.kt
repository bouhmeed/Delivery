package com.example.delivery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.delivery.models.MaintenanceAlert
import com.example.delivery.models.WarningLevel
import com.example.delivery.ui.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceAlertCard(alert: MaintenanceAlert?) {
    if (alert == null) return
    
    val backgroundColor: Color
    val borderColor: Color
    val icon: ImageVector
    val iconColor: Color
    
    when (alert.warningLevel) {
        WarningLevel.URGENT -> {
            backgroundColor = Color(0xFFFFF5F5)
            borderColor = Color(0xFFDC2626)
            icon = Icons.Default.Warning
            iconColor = DesignSystem.Colors.DANGER_RED
        }
        WarningLevel.WARNING -> {
            backgroundColor = Color(0xFFFFFAF0)
            borderColor = Color(0xFFEA580C)
            icon = Icons.Default.Build
            iconColor = DesignSystem.Colors.WARNING_ORANGE
        }
        WarningLevel.NORMAL -> {
            backgroundColor = Color(0xFFF0FDF4)
            borderColor = Color(0xFF16A34A)
            icon = Icons.Default.CheckCircle
            iconColor = DesignSystem.Colors.SUCCESS_GREEN
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Maintenance Alert",
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Maintenance ${alert.type}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (alert.daysRemaining != null) {
                    val daysText = when {
                        alert.daysRemaining <= 0 -> "Aujourd'hui"
                        alert.daysRemaining == 1 -> "Demain"
                        else -> "Dans ${alert.daysRemaining} jours"
                    }
                    
                    Text(
                        text = daysText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                alert.vehicleName?.let { vehicleName ->
                    Text(
                        text = "$vehicleName ${alert.registration ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            when (alert.warningLevel) {
                WarningLevel.URGENT -> {
                    Text(
                        text = "URGENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DesignSystem.Colors.DANGER_RED,
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFF5F5),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                WarningLevel.WARNING -> {
                    Text(
                        text = "BIENTÔT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DesignSystem.Colors.WARNING_ORANGE,
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFFAF0),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                WarningLevel.NORMAL -> {
                    // No badge for normal status
                }
            }
        }
    }
}
