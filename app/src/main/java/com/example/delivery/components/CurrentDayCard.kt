package com.example.delivery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun CurrentDayCard(
    userInfo: Any?,
    driverInfo: Any?,
    isLoading: Boolean
) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)
    val formattedDate = today.format(formatter).replaceFirstChar { it.uppercase() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header avec titre et date
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
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Journée Actuelle",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = formattedDate,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
                
                // Badge "AUJOURD'HUI"
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1976D2)
                ) {
                    Text(
                        text = "AUJOURD'HUI",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section Informations Utilisateur
            if (!isLoading && userInfo != null) {
                val userName = when (userInfo) {
                    is com.example.delivery.models.UserResponse -> "${userInfo.firstName} ${userInfo.lastName}"
                    else -> "Utilisateur"
                }
                
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Bonjour $userName!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Section Indicateurs de Date
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0F4F8)
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
                        // Semaine
                        DateIndicator(
                            label = "Semaine",
                            value = "${today.get(java.time.temporal.WeekFields.ISO.weekOfYear())}",
                            color = Color(0xFF1976D2)
                        )
                        
                        // Mois
                        DateIndicator(
                            label = "Mois",
                            value = today.month.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = Color(0xFF4CAF50)
                        )
                        
                        // Jour
                        DateIndicator(
                            label = "Jour",
                            value = "${today.dayOfMonth}",
                            color = Color(0xFF9C27B0)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Section Informations Chauffeur
                if (driverInfo != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E8)
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
                            // Type d'emploi
                            DriverInfoItem(
                                icon = Icons.Default.Work,
                                label = "Type",
                                value = when (driverInfo) {
                                    is com.example.delivery.models.Driver -> driverInfo.employmentType ?: "FULL_TIME"
                                    else -> "FULL_TIME"
                                },
                                color = Color(0xFF1976D2)
                            )
                            
                            // Statut
                            DriverInfoItem(
                                icon = Icons.Default.Person,
                                label = "Statut",
                                value = when (driverInfo) {
                                    is com.example.delivery.models.Driver -> driverInfo.status ?: "ACTIF"
                                    else -> "ACTIF"
                                },
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateIndicator(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
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
private fun DriverInfoItem(
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
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}
