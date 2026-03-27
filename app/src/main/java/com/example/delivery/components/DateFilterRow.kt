package com.example.delivery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterRow(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000L
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            onDateSelected(localDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous day button
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Color(0xFFF5F5F5),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Jour précédent",
                    tint = Color(0xFF666666)
                )
            }
            
            // Date display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { showDatePicker = true }
            ) {
                Text(
                    text = formatDateForDisplay(selectedDate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Sélectionner une date",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = getDateTypeText(selectedDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
            
            // Next day button
            IconButton(
                onClick = onNextDay,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Color(0xFFF5F5F5),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Jour suivant",
                    tint = Color(0xFF666666)
                )
            }
        }
        
        // Today button (only show if not today)
        if (!isToday(selectedDate)) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onTodayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = "Aujourd'hui",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Format date for display (dd/MM/yyyy)
 */
private fun formatDateForDisplay(date: LocalDate): String {
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthValue.toString().padStart(2, '0')
    val year = date.year
    return "$day/$month/$year"
}

/**
 * Get text describing the date type (Aujourd'hui, Hier, Demain, or date)
 */
private fun getDateTypeText(date: LocalDate): String {
    val today = LocalDate.now()
    return when {
        date.isEqual(today) -> "Aujourd'hui"
        date.isEqual(today.minusDays(1)) -> "Hier"
        date.isEqual(today.plusDays(1)) -> "Demain"
        date.isBefore(today) -> "Passé"
        date.isAfter(today) -> "Futur"
        else -> formatDateForDisplay(date)
    }
}

/**
 * Check if date is today
 */
private fun isToday(date: LocalDate): Boolean {
    return date.isEqual(LocalDate.now())
}
