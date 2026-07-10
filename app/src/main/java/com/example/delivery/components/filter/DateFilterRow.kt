package com.example.delivery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.CalendarToday
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
    
    // Seamless background matching TopAppBar
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFEBF4FF))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous day pill chip
        FilterChip(
            selected = false,
            onClick = onPreviousDay,
            label = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Jour précédent",
                    tint = Color(0xFF102A43),
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = Color.White,
                selectedContainerColor = Color(0xFF102A43),
                selectedLabelColor = Color.White,
                labelColor = Color(0xFF102A43)
            ),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        )
        
        // Date display pill chip
        FilterChip(
            selected = true,
            onClick = { showDatePicker = true },
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Sélectionner une date",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatDateForDisplay(selectedDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = Color(0xFF102A43),
                selectedContainerColor = Color(0xFF102A43),
                selectedLabelColor = Color.White,
                labelColor = Color.White
            )
        )
        
        // Next day pill chip
        FilterChip(
            selected = false,
            onClick = onNextDay,
            label = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Jour suivant",
                    tint = Color(0xFF102A43),
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = Color.White,
                selectedContainerColor = Color(0xFF102A43),
                selectedLabelColor = Color.White,
                labelColor = Color(0xFF102A43)
            ),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        )
        
        // Today pill chip (only show if not today)
        if (!isToday(selectedDate)) {
            FilterChip(
                selected = false,
                onClick = onTodayClick,
                label = {
                    Text(
                        text = "Aujourd'hui",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    selectedContainerColor = Color(0xFF102A43),
                    selectedLabelColor = Color.White,
                    labelColor = Color(0xFF102A43)
                ),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            )
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
