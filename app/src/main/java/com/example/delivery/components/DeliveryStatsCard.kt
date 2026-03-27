package com.example.delivery.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delivery.viewmodel.DeliveryStats

@Composable
fun DeliveryStatsCard(
    stats: DeliveryStats,
    modifier: Modifier = Modifier
) {
    var animatedProgress by remember { mutableStateOf(0f) }
    
    // Animate progress
    LaunchedEffect(stats.completionPercentage) {
        animate(
            initialValue = 0f,
            targetValue = stats.completionPercentage.toFloat() / 100f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = EaseOutCubic
            )
        ) { value, _ ->
            animatedProgress = value
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progression du jour",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                Text(
                    text = "${stats.completed}/${stats.total}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stats.completionPercentage}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "${stats.completed} livraisons",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFF0F0F0),
                    strokeCap = StrokeCap.Round
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Terminées",
                    value = stats.completed.toString(),
                    iconColor = Color(0xFF4CAF50),
                    backgroundColor = Color(0xFFE8F5E8),
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    icon = Icons.Default.PlayCircle,
                    label = "En cours",
                    value = stats.inProgress.toString(),
                    iconColor = Color(0xFFFF9800),
                    backgroundColor = Color(0xFFFFF3E0),
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    icon = Icons.Default.Schedule,
                    label = "À faire",
                    value = stats.notStarted.toString(),
                    iconColor = Color(0xFF2196F3),
                    backgroundColor = Color(0xFFE3F2FD),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    backgroundColor,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF666666)
        )
    }
}
