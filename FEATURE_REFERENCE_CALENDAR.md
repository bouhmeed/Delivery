# Calendar Feature Reference - Tournee Page

## Overview
Custom calendar component with red indicators for days with shipments, used in Tournee page (Icon 🚗).

## File Location
`app/src/main/java/com/example/delivery/presentation/components/CustomCalendar.kt`

## Component Details

### CustomCalendar Component
- **Lines**: 386-499
- **Component**: `CustomCalendar`

### Key Features
- Month navigation with ArrowBack/ArrowForward icons
- French weekday headers: "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"
- French month formatting: `MMMM yyyy` with `Locale.FRENCH`
- Grid layout with 7 columns (LazyVerticalGrid with GridCells.Fixed(7))
- 6 rows x 7 days = 42 total cells with padding for incomplete weeks

### Red Indicators (Points Rouges)
- **Component**: `CalendarDay` (lines 502-553)
- **Implementation**: Red dot indicator on days with shipments

```kotlin
if (hasShipments && !isSelected) {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.Red)
    )
}
```

- **Logic**: Checks if date is in `shipmentDates` list
- **Visual**: 4.dp rounded red box displayed below day number

### Date-Shipments Linkage
- **Data Source**: `shipmentDates: List<String>` (ISO format: "yyyy-MM-dd")
- **API Endpoint**: `getShipmentDates(driverId: Int)` in multiple repositories
- **Loading**: Called in ViewModels via `loadShipmentDates(driverId)`
- **State**: Stored in `_shipmentDates` MutableStateFlow in ViewModels

### Calendar Day Styling
- Selected date: Green background `Color(0xFF4CAF50)`, white text
- Days with shipments: Light green background `Color(0xFFE8F5E9)`, black text
- Current month: Black text
- Other months: Light gray text
- Rounded corners: 8.dp

### DateFilterRow Component
- **File**: `app/src/main/java/com/example/delivery/presentation/components/CustomCalendar.kt`
- **Component**: `DateFilterRow` (lines 604-756)
- **Description**: Date navigation bar with calendar popup

### Key Features
- Previous/Next day buttons with rounded gray backgrounds
- Clickable date display that opens calendar popup
- Green indicator dot (8.dp) if selected date has shipments
- "Aujourd'hui" button shown only when not on today's date
- Date type text: "Aujourd'hui", "Hier", "Demain", "Passé", "Futur"
- Date format: `yyyy/MM/dd` for display

### Calendar Popup Behavior
- Triggered by clicking on date display
- Shows CustomCalendar dialog
- Dismisses on date selection or cancel
- Automatically reloads data when date changes

## Implementation Steps

### Step 1: Create CustomCalendar Component
```kotlin
@Composable
fun CustomCalendar(
    selectedDate: LocalDate,
    shipmentDates: List<String>,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // Implementation with month navigation, grid layout, and day cells
}
```

### Step 2: Create CalendarDay Component
```kotlin
@Composable
fun CalendarDay(
    date: LocalDate,
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    hasShipments: Boolean,
    onClick: () -> Unit
) {
    // Implementation with red indicator for shipments
}
```

### Step 3: Create DateFilterRow Component
```kotlin
@Composable
fun DateFilterRow(
    selectedDate: LocalDate,
    shipmentDates: List<String>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onTodayClick: () -> Unit
) {
    // Implementation with calendar popup trigger
}
```

### Step 4: Add API Endpoint
```kotlin
interface ShipmentApiService {
    @GET("api/shipments/dates/{driverId}")
    suspend fun getShipmentDates(@Path("driverId") driverId: Int): Response<ShipmentDatesResponse>
}
```

### Step 5: Add ViewModel State
```kotlin
class TourneeViewModel : ViewModel() {
    private val _shipmentDates = MutableStateFlow<List<String>>(emptyList())
    val shipmentDates: StateFlow<List<String>> = _shipmentDates.asStateFlow()
    
    fun loadShipmentDates(driverId: Int) {
        viewModelScope.launch {
            repository.getShipmentDates(driverId)
        }
    }
}
```

### Step 6: Integrate in TourneeScreen
```kotlin
@Composable
fun TourneeScreen(driverId: Int) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val shipmentDates by viewModel.shipmentDates.collectAsState()
    
    LaunchedEffect(driverId) {
        viewModel.loadShipmentDates(driverId)
    }
    
    DateFilterRow(
        selectedDate = selectedDate,
        shipmentDates = shipmentDates,
        onDateSelected = { viewModel.setSelectedDate(it) }
    )
}
```

## Notes
- Use French localization for all text
- Red indicators are 4.dp rounded boxes
- Green background for selected date
- Light green background for days with shipments
- Calendar popup triggers on date display click
