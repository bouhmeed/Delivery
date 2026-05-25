package com.example.delivery.components

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
import java.time.LocalDate

@Composable
fun DayIndicatorCard(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    driverInfo: Any?,
    userInfo: Any?,
    isLoading: Boolean
) {
    val today = LocalDate.now()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Semaine",
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Semaine",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${today.get(java.time.temporal.WeekFields.ISO.weekOfYear())}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            HorizontalDivider(
                modifier = Modifier
                    .height(50.dp)
                    .width(1.dp),
                color = Color.LightGray
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = "Mois",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mois",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = today.month.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            HorizontalDivider(
                modifier = Modifier
                    .height(50.dp)
                    .width(1.dp),
                color = Color.LightGray
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Today,
                    contentDescription = "Jour",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Jour",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${today.dayOfMonth}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
