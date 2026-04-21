package com.example.delivery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCalendar(
    selectedDate: LocalDate,
    shipmentDates: List<String>,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Month navigation header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentMonth = currentMonth.minusMonths(1) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Mois précédent",
                            tint = Color(0xFF666666)
                        )
                    }
                    
                    Text(
                        text = formatMonth(currentMonth),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    
                    IconButton(
                        onClick = { currentMonth = currentMonth.plusMonths(1) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Mois suivant",
                            tint = Color(0xFF666666)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Weekday headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Calendar grid
                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    shipmentDates = shipmentDates,
                    onDateSelected = { date ->
                        onDateSelected(date)
                        onDismiss()
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cancel button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF666666),
                        contentColor = Color.White
                    )
                ) {
                    Text("Annuler")
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    shipmentDates: List<String>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()
    
    // Calculate first day of week (Monday = 0)
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value - 1) % 7
    
    // Create calendar days
    val calendarDays = mutableListOf<LocalDate?>()
    
    // Add empty cells for days before first day
    repeat(firstDayOfWeek) {
        calendarDays.add(null)
    }
    
    // Add all days of the month
    for (day in 1..lastDayOfMonth.dayOfMonth) {
        calendarDays.add(currentMonth.atDay(day))
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(42) { index ->
            val date = calendarDays.getOrNull(index)
            CalendarDay(
                date = date,
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                shipmentDates = shipmentDates,
                onClick = {
                    date?.let { onDateSelected(it) }
                }
            )
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    shipmentDates: List<String>,
    onClick: () -> Unit
) {
    val isSelected = date == selectedDate
    val isCurrentMonth = date?.let { YearMonth.from(it) == currentMonth } ?: false
    val hasShipments = date?.let { 
        shipmentDates.contains(it.toString())
    } ?: false
    
    val backgroundColor = when {
        isSelected -> Color(0xFF4CAF50)
        hasShipments -> Color(0xFFE8F5E9)
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> Color.White
        !isCurrentMonth -> Color(0xFFCCCCCC)
        else -> Color(0xFF333333)
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (date != null) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = textColor
                )
                
                // Red indicator for shipments
                if (hasShipments && !isSelected) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.Red)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterRow(
    selectedDate: LocalDate,
    shipmentDates: List<String>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCalendar by remember { mutableStateOf(false) }
    
    if (showCalendar) {
        CustomCalendar(
            selectedDate = selectedDate,
            shipmentDates = shipmentDates,
            onDateSelected = onDateSelected,
            onDismiss = { showCalendar = false }
        )
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous day button
                IconButton(
                    onClick = onPreviousDay,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5))
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Jour précédent",
                        tint = Color(0xFF666666)
                    )
                }
                
                // Date display (clickable)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showCalendar = true }
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
                        // Green indicator if selected date has shipments
                        val hasShipmentsOnSelected = shipmentDates.contains(selectedDate.toString())
                        if (hasShipmentsOnSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF4CAF50))
                            )
                        }
                        
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
                        .background(Color(0xFFF5F5F5))
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Jour suivant",
                        tint = Color(0xFF666666)
                    )
                }
            }
            
            // Today button (only if not today)
            if (!isToday(selectedDate)) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onTodayClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
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
}

private fun formatMonth(yearMonth: YearMonth): String {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)
    return yearMonth.format(formatter).replaceFirstChar { it.uppercase() }
}

private fun formatDateForDisplay(date: LocalDate): String {
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthValue.toString().padStart(2, '0')
    val year = date.year
    return "$day/$month/$year"
}

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

private fun isToday(date: LocalDate): Boolean {
    return date.isEqual(LocalDate.now())
}
