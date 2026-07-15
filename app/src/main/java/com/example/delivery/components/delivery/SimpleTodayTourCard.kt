package com.example.delivery.components.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.delivery.models.delivery.TourInfo
import com.example.delivery.models.delivery.TourStatistics

private val AccentBlue = Color(0xFF2F6FED)
private val AccentBlueDark = Color(0xFF1E4FBF)
private val PureWhite = Color(0xFFFFFFFF)

@Composable
fun SimpleTodayTourCard(
    tourInfo: TourInfo,
    statistics: TourStatistics,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Calender.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { navController.navigate("tournee") }
            .clip(RoundedCornerShape(16.dp))
    ) {
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
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AccentBlue.copy(alpha = 0.85f),
                            AccentBlueDark.copy(alpha = 0.85f)
                        )
                    )
                )
        ) {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Tournée du jour",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Text(
                                text = tourInfo.tripId,
                                fontSize = 12.sp,
                                color = PureWhite.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    // Status badge
                    Surface(
                        color = PureWhite.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "En cours",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Statistics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${statistics.completedShipments}/${statistics.totalShipments}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Text(
                            text = "Livraisons",
                            fontSize = 12.sp,
                            color = PureWhite.copy(alpha = 0.8f)
                        )
                    }
                    
                    // Progress bar
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text(
                            text = "${statistics.completionPercentage}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { statistics.completionPercentage / 100f },
                            color = PureWhite,
                            trackColor = PureWhite.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
