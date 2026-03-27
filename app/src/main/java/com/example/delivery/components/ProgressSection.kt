package com.example.delivery.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delivery.models.TripProgress

@Composable
fun ProgressSection(
    progress: TripProgress,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.percentage / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress_animation"
    )
    
    // Pas de Card - contenu direct dans la Card parente blanche
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp) // Pas de padding supplémentaire
    ) {
        // Title
        Text(
            text = "🎯 PROGRESSION",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
            
            Spacer(modifier = Modifier.height(8.dp))
        
        // Progress text
        Text(
            text = progress.progressText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Progress bar
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when {
                progress.isCompleted -> Color(0xFF4CAF50) // Green
                progress.percentage >= 75 -> Color(0xFF2196F3) // Blue
                progress.percentage >= 50 -> Color(0xFFFF9800) // Orange
                else -> Color(0xFFFF5722) // Red
            },
            trackColor = Color(0xFFE0E0E0), // Gris clair pour fond blanc
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Percentage
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = progress.percentageText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )
            
            if (progress.isCompleted) {
                Text(
                    text = "Tournée terminée 🎉",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NoTripTodaySection(
    modifier: Modifier = Modifier
) {
    // Pas de Card - contenu direct dans la Card parente blanche
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎯 PROGRESSION",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Aucune tournée aujourd'hui",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProgressErrorSection(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pas de Card - contenu direct dans la Card parente blanche
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎯 PROGRESSION",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Erreur de chargement",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Réessayer")
        }
    }
}

@Composable
fun ProgressLoadingSection(
    modifier: Modifier = Modifier
) {
    // Pas de Card - contenu direct dans la Card parente blanche
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎯 PROGRESSION",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Color.Black,
            strokeWidth = 2.dp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Chargement...",
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )
    }
}
