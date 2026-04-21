# Delivery Tracking Screen Feature Reference - Suivi mes Tournées

## Overview
Delivery tracking screen with date filtering, calendar integration, shipment-based logic, and status management. Icon 🚚.

## File Location
`app/src/main/java/com/example/delivery/presentation/screens/DeliveryTrackingScreen.kt`

## Component Details

### Date Filtering
- **Component**: `DeliveryTrackingScreen` (lines 272-420)

### Implementation
```kotlin
val selectedDate by viewModel.selectedDate.collectAsState()
val shipmentDates by viewModel.shipmentDates.collectAsState()

// Date Filter Row at top of screen
DateFilterRow(
    selectedDate = selectedDate,
    onDateSelected = { newDate ->
        viewModel.setSelectedDate(newDate)
    },
    onPreviousDay = { viewModel.goToPreviousDay(driverId) },
    onNextDay = { viewModel.goToNextDay(driverId) },
    onTodayClick = { viewModel.goToToday(driverId) },
    shipmentDates = shipmentDates
)

// Reload data when date changes
LaunchedEffect(driverId, selectedDate) {
    viewModel.loadTripForDate(driverId, selectedDate)
    viewModel.loadShipmentDates(driverId)
}
```

### ViewModel State Management
- `selectedDate`: MutableStateFlow<LocalDate> initialized to LocalDate.now()
- `shipmentDates`: MutableStateFlow<List<String>> loaded from API
- Methods: `setSelectedDate()`, `goToPreviousDay()`, `goToNextDay()`, `goToToday()`
- Automatic data refresh via LaunchedEffect

### Shipment-Based Logic
- **Change**: Uses `shipmentId` instead of `TripShipmentLink` for primary identification
- **Fallback**: Uses `tripShipmentLinkId ?: shipmentId` when both available
- **Status Source**: Reads from `shipmentStatus` field instead of `linkStatus`

### Status Management
- **Status Values**:
  - `TO_PLAN` - "À planifier" (not started)
  - `EXPEDITION` - "En expédition" (in progress)
  - `DELIVERED` - "Livrée" (completed)
- **Free Movement**: Status dropdown allows changing between any states
- **No Restrictions**: Can move forward or backward between statuses

### Status Change Callback
```kotlin
onStatusChange = { delivery, newStatus ->
    val tripShipmentLinkId = delivery.tripShipmentLinkId ?: delivery.shipmentId
    viewModel.updateTripShipmentStatus(tripShipmentLinkId, newStatus, driverId)
}
```

### Stats Calculation
```kotlin
DeliveryStats(
    total = deliveries.size,
    completed = deliveries.count { it.podDone || it.shipmentStatus == "DELIVERED" },
    inProgress = deliveries.count { !it.podDone && (it.shipmentStatus == "EXPEDITION" || it.linkStatus == "EN_COURS") },
    notStarted = deliveries.count { !it.podDone && (it.shipmentStatus == "TO_PLAN" || it.linkStatus == "NON_DEMARRE" || it.linkStatus == "ASSIGNED") },
    completionPercentage = if (deliveries.isNotEmpty()) {
        (deliveries.count { it.podDone || it.shipmentStatus == "DELIVERED" } * 100 / deliveries.size)
    } else {
        0
    }
)
```

### UI Improvements
- **DateFilterRow**: Card with 16.dp rounded corners, white background, 2.dp elevation
- **DeliveryStatsCard**: Shows completion percentage with visual progress
- **DeliveryItemCard**: Redesigned with status dropdown, action buttons
- **Loading States**: CircularProgressIndicator with centered text
- **Error States**: Error content with retry button
- **Snackbar Feedback**: Success/error messages after operations

## Implementation Steps

### Step 1: Add Date Filtering to ViewModel
```kotlin
class DeliveryTrackingViewModel : ViewModel() {
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    
    private val _shipmentDates = MutableStateFlow<List<String>>(emptyList())
    val shipmentDates: StateFlow<List<String>> = _shipmentDates.asStateFlow()
    
    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        _selectedTripId.value = null
    }
    
    fun goToPreviousDay(driverId: Int) {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        loadTripForDate(driverId, _selectedDate.value)
    }
    
    fun goToNextDay(driverId: Int) {
        _selectedDate.value = _selectedDate.value.plusDays(1)
        loadTripForDate(driverId, _selectedDate.value)
    }
    
    fun goToToday(driverId: Int) {
        _selectedDate.value = LocalDate.now()
        loadTripForDate(driverId, _selectedDate.value)
    }
    
    fun loadShipmentDates(driverId: Int) {
        viewModelScope.launch {
            repository.getShipmentDates(driverId)
        }
    }
}
```

### Step 2: Add Shipment-Based Logic
```kotlin
// In DeliveryItem data class
data class DeliveryItem(
    val shipmentId: Int,
    val tripShipmentLinkId: Int?,
    val shipmentStatus: String,
    val linkStatus: String?,
    val podDone: Boolean
)

// Use shipmentId as primary identifier
val id = delivery.tripShipmentLinkId ?: delivery.shipmentId
val status = delivery.shipmentStatus ?: delivery.linkStatus
```

### Step 3: Add Status Dropdown with Free Movement
```kotlin
@Composable
fun DeliveryItemCard(
    delivery: DeliveryItem,
    onStatusChange: (DeliveryItem, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("TO_PLAN", "EXPEDITION", "DELIVERED")
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextButton(onClick = { expanded = true }) {
            Text(getStatusLabel(delivery.shipmentStatus))
            Icon(Icons.Default.ArrowDropDown)
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statusOptions.forEach { status ->
                DropdownMenuItem(
                    text = { Text(getStatusLabel(status)) },
                    onClick = {
                        onStatusChange(delivery, status)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun getStatusLabel(status: String): String {
    return when (status) {
        "TO_PLAN" -> "À planifier"
        "EXPEDITION" -> "En expédition"
        "DELIVERED" -> "Livrée"
        else -> status
    }
}
```

### Step 4: Update Card Styling
```kotlin
Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
        containerColor = Color.White
    ),
    elevation = CardDefaults.cardElevation(
        defaultElevation = 8.dp
    )
) {
    // Card content
}
```

### Step 5: Update Action Button Colors
```kotlin
// Validation button - Blue
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF237FDA),
        contentColor = Color.White
    )
) {
    Text("Valider")
}

// Returns button - Blue
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF237FDA),
        contentColor = Color.White
    )
) {
    Text("Retours")
}

// Navigation button - Green
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF4CAF50),
        contentColor = Color.White
    )
) {
    Text("Naviguer")
}
```

### Step 6: Add Stats Display
```kotlin
@Composable
fun DeliveryStatsCard(stats: DeliveryStats) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Statistiques", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                StatItem("Total", stats.total.toString())
                StatItem("Complétées", stats.completed.toString())
                StatItem("En cours", stats.inProgress.toString())
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = stats.completionPercentage / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Text("${stats.completionPercentage}% complété")
        }
    }
}
```

## API Endpoints Required
- `GET /api/shipments/dates/{driverId}` - Get shipment dates for calendar
- `PUT /api/delivery-tracking/shipments/{shipmentId}/status` - Update shipment status
- `GET /api/delivery-tracking/trips/{driverId}/{date}` - Get trip for specific date

## Notes
- Use shipmentId as primary identifier
- Status changes can move freely between all states
- Calendar shows red indicators on days with shipments
- Date filtering reloads data automatically
- Stats calculation uses shipmentStatus primarily
