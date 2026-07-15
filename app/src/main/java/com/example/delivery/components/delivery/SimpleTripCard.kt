package com.example.delivery.components.delivery

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.delivery.repository.delivery.SimpleTripInfo

private val CardBlue = Color(0xFF05204A)
private val CardBlueDark = Color(0xFF084A9E)
private val PureWhite = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8A93A6)

@Composable
fun SimpleTripCard(
    tripInfo: SimpleTripInfo,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    if (!tripInfo.hasTrip) {
        // No trip today - with Lottie background
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Calender.json"))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            isPlaying = true
        )
        
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Lottie animation background
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.matchParentSize()
                )
                
                // Gradient overlay
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
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Aucune tournée pour aujourd'hui",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = PureWhite
                        )
                    }
                }
            }
        }
    } else {
        // Has trip today - with Lottie background and better contrast
        var isPressed by remember { mutableStateOf(false) }
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Calender.json"))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            isPlaying = true
        )
        
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp)
                .alpha(if (isPressed) 0.7f else 1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Lottie animation background
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.matchParentSize()
                )
                
                // Gradient overlay for better text contrast
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
                ) {
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
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tournée du Jour",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                }
                                tripInfo.tripNumber?.let {
                                    Text(
                                        text = "#$it",
                                        fontSize = 14.sp,
                                        color = PureWhite.copy(alpha = 0.9f),
                                        modifier = Modifier.padding(start = 32.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Statistics section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = PureWhite.copy(alpha = 0.25f)
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
                                    label = "Livraisons",
                                    value = tripInfo.shipmentCount.toString(),
                                    color = PureWhite
                                )
                                StatItem(
                                    icon = Icons.Default.Person,
                                    label = "Clients",
                                    value = tripInfo.clientNames.size.toString(),
                                    color = PureWhite
                                )
                                StatItem(
                                    icon = Icons.Default.LocationOn,
                                    label = "Villes",
                                    value = tripInfo.destinationCities.size.toString(),
                                    color = PureWhite
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Client and destination info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Clients",
                                    fontSize = 12.sp,
                                    color = PureWhite.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                                val clientText = if (tripInfo.clientNames.isEmpty()) {
                                    "Aucun client"
                                } else {
                                    tripInfo.clientNames.take(2).joinToString(", ") + 
                                    if (tripInfo.clientNames.size > 2) " (+${tripInfo.clientNames.size - 2})" else ""
                                }
                                Text(
                                    text = clientText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PureWhite,
                                    maxLines = 2
                                )
                            }
                            
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Destinations",
                                    fontSize = 12.sp,
                                    color = PureWhite.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                                val cityText = if (tripInfo.destinationCities.isEmpty()) {
                                    "Aucune destination"
                                } else {
                                    tripInfo.destinationCities.take(2).joinToString(", ") + 
                                    if (tripInfo.destinationCities.size > 2) " (+${tripInfo.destinationCities.size - 2})" else ""
                                }
                                Text(
                                    text = cityText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PureWhite,
                                    maxLines = 2
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // View trip button (disabled)
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(12.dp),
                            enabled = false
                        ) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = CardBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Voir la tournée",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CardBlue
                            )
                        }
                    }
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
            color = PureWhite.copy(alpha = 0.8f)
        )
    }
}
