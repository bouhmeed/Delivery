// TourneeScreen.kt – UI layer only, all data handled by HomeViewModel
package com.example.delivery.screens.delivery

import com.example.delivery.repository.Result

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.components.TripSearchBar
import com.example.delivery.components.CommonTopAppBar
import com.example.delivery.navigation.Screen
import com.example.delivery.network.config.ApiClient
import com.example.delivery.network.api.driver.TripApiService
import com.example.delivery.network.api.user.UserApiService
import com.example.delivery.network.api.vehicle.VehicleApiService
import com.example.delivery.network.api.driver.DriverApiService
import com.example.delivery.network.api.delivery.ShipmentApiService
import com.example.delivery.auth.AuthManager
import com.example.delivery.models.user.UserResponse
import com.example.delivery.models.vehicle.Vehicle
import com.example.delivery.models.driver.Driver
import com.example.delivery.models.driver.Trip
import android.widget.Toast
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────
// Figma UI Kit Color Palette
// ─────────────────────────────────────────────
private val FigmaBg           = Color(0xFFEAF2F8) // Soft sky-blue layout background
private val PureWhite         = Color(0xFFFFFFFF)
private val FigmaHeaderBlue   = Color(0xFF0C6BCE) // Royal blue
private val FigmaTextDark     = Color(0xFF1B2A4A) // Dark slate for primary text
private val FigmaTextMuted    = Color(0xFF8F9BB3) // Subtle grey for secondary label
private val FigmaAmber        = Color(0xFFFF9F0A)
private val FigmaRed          = Color(0xFFFF3B30)
private val FigmaGreen        = Color(0xFF34C759)
private val FigmaShadowColor  = Color(0xFF0C6BCE).copy(alpha = 0.08f)
private val FigmaLightGrey    = Color(0xFFF1F5F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourneeScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val directRepo = remember { com.example.delivery.repository.driver.DirectTourneeRepository() }
    val authManager = remember { AuthManager(context) }
    
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(LocalDate.now())) }
    var showStats by remember { mutableStateOf(false) }
    
    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
        
    // Data states
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoadingTrips by remember { mutableStateOf(false) }
    var tripError by remember { mutableStateOf<String?>(null) }
    var currentDriver by remember { mutableStateOf<UserResponse?>(null) }
    var isLoadingUser by remember { mutableStateOf(false) }
    
    // Shipments state
    var tripShipments by remember { mutableStateOf<Map<Int, List<com.example.delivery.models.delivery.ShipmentSearchDetail>>>(emptyMap()) }
    
    // Cache
    var vehicleCache by remember { mutableStateOf<Map<String, Vehicle>>(emptyMap()) }
    var driverCache by remember { mutableStateOf<Map<String, Driver>>(emptyMap()) }
    
    fun addToVehicleCache(id: String, vehicle: Vehicle) {
        vehicleCache = vehicleCache + (id to vehicle)
    }
    
    fun addToDriverCache(id: String, driver: Driver) {
        driverCache = driverCache + (id to driver)
    }
    
    fun loadTripShipments(tripId: Int) {
        if (!tripShipments.containsKey(tripId)) {
            coroutineScope.launch {
                try {
                    val result = directRepo.getShipmentsByTrip(tripId)
                    if (result.isSuccess) {
                        val mappedShipments = result.getOrNull() ?: emptyList()
                        tripShipments = tripShipments + (tripId to mappedShipments)
                    }
                } catch (e: Exception) {
                    // Fail silently
                }
            }
        }
    }
    
    fun loadDriverTrips(driverId: String) {
        coroutineScope.launch {
            isLoadingTrips = true
            tripError = null
            try {
                val result = directRepo.getTripsByDriver(driverId)
                if (result.isSuccess) {
                    trips = result.getOrNull() ?: emptyList()
                } else {
                    tripError = "Erreur: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                tripError = "Exception: ${e.message}"
            } finally {
                isLoadingTrips = false
            }
        }
    }
    
    fun loadCurrentUser() {
        coroutineScope.launch {
            isLoadingUser = true
            try {
                val userEmail = authManager.getUserEmail()
                if (userEmail != null) {
                    val result = directRepo.getUserByEmail(userEmail)
                    if (result.isSuccess) {
                        currentDriver = result.getOrNull()
                        val driverId = currentDriver?.driverId?.toString()
                        if (driverId != null) {
                            loadDriverTrips(driverId)
                        } else {
                            tripError = "Aucun driverId trouvé pour cet utilisateur"
                            isLoadingTrips = false
                        }
                    } else {
                        tripError = "Erreur chargement utilisateur: ${result.exceptionOrNull()?.message}"
                    }
                } else {
                    tripError = "Utilisateur non connecté"
                }
            } catch (e: Exception) {
                tripError = "Exception: ${e.message}"
            } finally {
                isLoadingUser = false
            }
        }
    }
    
    fun refreshTrips() {
        trips = emptyList()
        tripShipments = emptyMap()
        vehicleCache = emptyMap()
        driverCache = emptyMap()
        
        val driverId = currentDriver?.driverId?.toString()
        if (driverId != null) {
            loadDriverTrips(driverId)
        } else {
            loadCurrentUser()
        }
    }
    
    LaunchedEffect(Unit) {
        loadCurrentUser()
    }
    
    fun getTripDate(trip: Trip): LocalDate? {
        return try {
            if (trip.tripDate.isNotEmpty()) {
                LocalDate.parse(trip.tripDate.substring(0, 10))
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    val tripsForSelectedDate = remember(trips, selectedDate, searchQuery) {
        trips.filter { trip ->
            val tripDate = getTripDate(trip)
            val matchesDate = tripDate == selectedDate
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val query = searchQuery.lowercase()
                (trip.tripId?.lowercase()?.contains(query) == true) ||
                (trip.status?.lowercase()?.contains(query) == true) ||
                (trip.id.toString().contains(query))
            }
            matchesDate && matchesSearch
        }
    }
    
    val tripsByDate = remember(trips) {
        trips.groupBy { trip -> getTripDate(trip) }
            .mapValues { it.value.size }
            .filterKeys { it != null }
            .mapKeys { it.key!! }
    }
    
    val completionProgress = remember(tripsForSelectedDate) {
        if (tripsForSelectedDate.isEmpty()) 0f
        else {
            val completed = tripsForSelectedDate.count { it.status == "COMPLETED" }
            completed.toFloat() / tripsForSelectedDate.size.toFloat()
        }
    }
    
    Scaffold(
        containerColor = FigmaBg,
        topBar = {
            // ── Simple White Top Bar (As requested) ──
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, spotColor = FigmaShadowColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF05204A).copy(alpha = 0.85f),
                                        Color(0xFF084A9E).copy(alpha = 0.85f)
                                    )
                                )
                            )
                    ) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Ma Tournée",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { navController.navigate(Screen.Home.route) },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = PureWhite
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { refreshTrips() },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Refresh",
                                        tint = PureWhite
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = PureWhite
                            )
                        )
                    }
                }
                // Thin progress indicator underneath TopAppBar
                LinearProgressIndicator(
                    progress = completionProgress,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = FigmaHeaderBlue,
                    trackColor = FigmaLightGrey
                )
            }
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        LaunchedEffect(tripsForSelectedDate) {
            tripsForSelectedDate.forEach { trip ->
                loadTripShipments(trip.id.toInt())
            }
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FigmaBg)
                .padding(paddingValues)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Calendar View ──
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TourneeCalendarCard(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        toursByDate = tripsByDate,
                        onDateSelected = { date -> selectedDate = date },
                        onMonthChanged = { month -> currentMonth = month }
                    )
                }
            }
            
            // ── Search bar ──
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TripSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onSearchActiveChange = { isSearchActive = it },
                        onClearSearch = { searchQuery = "" },
                        placeholder = "Rechercher un trip..."
                    )
                }
            }
            
            // ── Title list view ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trips du ${selectedDate.dayOfMonth} ${selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${selectedDate.year}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaTextDark,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "${tripsForSelectedDate.size} résultat(s)",
                            fontSize = 13.sp,
                            color = FigmaHeaderBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Show trips lists
            if (tripsForSelectedDate.isNotEmpty()) {
                items(tripsForSelectedDate) { trip ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        TripDetailCardRedesigned(
                            trip = trip,
                            directRepo = directRepo,
                            vehicleCache = vehicleCache,
                            driverCache = driverCache,
                            onVehicleLoaded = { id, vehicle -> addToVehicleCache(id, vehicle) },
                            onDriverLoaded = { id, driver -> addToDriverCache(id, driver) },
                            onTripClick = { tripId -> 
                                val tripDateFormatted = trip.tripDate.substring(0, 10)
                                navController.navigate("delivery?date=$tripDateFormatted")
                            },
                            tripShipments = tripShipments,
                            onLoadShipments = { tripId -> loadTripShipments(tripId) },
                            navController = navController
                        )
                    }
                }
            } else if (!isLoadingTrips && !isLoadingUser && tripError == null) {
                item {
                    FigmaCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = "Aucun trip",
                                tint = FigmaTextMuted,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Aucun trip prévu",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FigmaTextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Profitez de cette journée ou sélectionnez une autre date.",
                                fontSize = 13.sp,
                                color = FigmaTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────
// Modern Monthly Calendar Component
// ─────────────────────────────────────────────
@Composable
fun TourneeCalendarCard(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    toursByDate: Map<LocalDate, Int>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit
) {
    FigmaCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Month navigation row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMonthChanged(currentMonth.minusMonths(1)) },
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(FigmaBg)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Mois précédent",
                        tint = FigmaTextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaTextDark
                )
                
                IconButton(
                    onClick = { onMonthChanged(currentMonth.plusMonths(1)) },
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(FigmaBg)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Mois suivant",
                        tint = FigmaTextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Days of the week header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaTextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Calendar Grid
            TourneeCalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                toursByDate = toursByDate,
                onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
fun TourneeCalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    toursByDate: Map<LocalDate, Int>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value - 1) % 7
    
    val calendarDays = mutableListOf<LocalDate?>()
    
    repeat(firstDayOfWeek) {
        calendarDays.add(null)
    }
    
    for (day in 1..lastDayOfMonth.dayOfMonth) {
        calendarDays.add(currentMonth.atDay(day))
    }
    
    Column {
        var weekDays = mutableListOf<LocalDate?>()
        
        calendarDays.forEachIndexed { index, date ->
            weekDays.add(date)
            
            if ((index + 1) % 7 == 0 || index == calendarDays.lastIndex) {
                while (weekDays.size < 7) {
                    weekDays.add(null)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weekDays.forEach { day ->
                        TourneeCalendarDay(
                            date = day,
                            isSelected = day == selectedDate,
                            hasTour = day != null && toursByDate.containsKey(day!!),
                            tourCount = day?.let { toursByDate[it] } ?: 0,
                            onClick = { 
                                if (day != null) {
                                    onDateSelected(day)
                                }
                            }
                        )
                    }
                }
                
                weekDays.clear()
                if (index < calendarDays.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TourneeCalendarDay(
    date: LocalDate?,
    isSelected: Boolean,
    hasTour: Boolean,
    tourCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FigmaHeaderBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 14.sp,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 14.sp,
                        color = FigmaTextDark,
                        fontWeight = if (date == LocalDate.now()) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    if (hasTour && tourCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(FigmaHeaderBlue)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Stops & Premium Card Details
// ─────────────────────────────────────────────
@Composable
fun TripDetailCardRedesigned(
    trip: Trip,
    directRepo: com.example.delivery.repository.driver.DirectTourneeRepository,
    vehicleCache: Map<String, Vehicle>,
    driverCache: Map<String, Driver>,
    onVehicleLoaded: (String, Vehicle) -> Unit,
    onDriverLoaded: (String, Driver) -> Unit,
    onTripClick: (String) -> Unit,
    tripShipments: Map<Int, List<com.example.delivery.models.delivery.ShipmentSearchDetail>>? = null,
    onLoadShipments: ((Int) -> Unit)? = null,
    navController: NavController
) {
    val tripId = trip.id
    val tripDate = trip.tripDate
    val driverId = trip.driverId ?: "N/A"
    val vehicleId = trip.vehicleId ?: "N/A"
    val status = trip.status
    val tripIdentifier = trip.tripId
    
    var vehicle by remember { mutableStateOf(vehicleCache[vehicleId]) }
    var driver by remember { mutableStateOf(driverCache[driverId]) }
    val initialVehicle = vehicleCache[vehicleId]
    val initialDriver = driverCache[driverId]
    var isLoadingDetails by remember { mutableStateOf(initialVehicle == null && vehicleId != "N/A" || initialDriver == null && driverId != "N/A") }
    
    var shipments by remember { mutableStateOf<List<com.example.delivery.models.delivery.ShipmentSearchDetail>>(emptyList()) }
    var isLoadingShipments by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(tripId) {
        val cachedShipments = tripShipments?.get(tripId.toInt())
        if (cachedShipments != null) {
            shipments = cachedShipments
        } else if (onLoadShipments != null) {
            isLoadingShipments = true
            onLoadShipments(tripId.toInt())
            kotlinx.coroutines.delay(500)
            val updatedShipments = tripShipments?.get(tripId.toInt())
            if (updatedShipments != null) {
                shipments = updatedShipments
            }
            isLoadingShipments = false
        }
    }
    
    LaunchedEffect(tripShipments) {
        val cachedShipments = tripShipments?.get(tripId.toInt())
        if (cachedShipments != null) {
            shipments = cachedShipments
        }
    }
    
    LaunchedEffect(vehicleId, driverId) {
        val currentVehicle = vehicle
        val currentDriver = driver
        if ((currentVehicle == null && vehicleId != "N/A") || (currentDriver == null && driverId != "N/A")) {
            isLoadingDetails = true
            coroutineScope.launch {
                try {
                    if (currentVehicle == null && vehicleId != "N/A") {
                        val result = directRepo.getVehicleById(vehicleId)
                        if (result.isSuccess) {
                            result.getOrNull()?.let { v ->
                                vehicle = v
                                onVehicleLoaded(vehicleId, v)
                            }
                        }
                    }
                    if (currentDriver == null && driverId != "N/A") {
                        val result = directRepo.getDriverById(driverId)
                        if (result.isSuccess) {
                            result.getOrNull()?.let { d ->
                                driver = d
                                onDriverLoaded(driverId, d)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                } finally {
                    isLoadingDetails = false
                }
            }
        }
    }
    
    val allCompleted = shipments.isNotEmpty() && shipments.all { 
        it.status.uppercase() == "DELIVERED" || 
        it.status.uppercase() == "COMPLETED"
    }
    
    FigmaCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !allCompleted) { onTripClick(tripId) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with Trip # & Modern Pill Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trip #$tripId",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaTextDark
                    )
                    if (tripIdentifier != null) {
                        Text(
                            text = tripIdentifier,
                            fontSize = 13.sp,
                            color = FigmaTextMuted
                        )
                    }
                }
                
                // Redesigned Pill Badge using Figma theme colors
                val (bgColor, textColor, label) = when (status) {
                    "COMPLETED" -> Triple(FigmaGreen.copy(alpha = 0.1f), FigmaGreen, "Terminé")
                    "IN_PROGRESS" -> Triple(FigmaHeaderBlue.copy(alpha = 0.1f), FigmaHeaderBlue, "En cours")
                    "READY" -> Triple(FigmaAmber.copy(alpha = 0.1f), FigmaAmber, "Prêt")
                    else -> Triple(FigmaLightGrey, FigmaTextDark, status)
                }
                
                Surface(
                    modifier = Modifier.clip(CircleShape),
                    color = bgColor
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Trip details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vehicle Details
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Véhicule",
                        tint = FigmaTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Véhicule", fontSize = 11.sp, color = FigmaTextMuted)
                        Text(
                            text = vehicle?.name ?: "ID: $vehicleId",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaTextDark
                        )
                    }
                }
                
                // Driver info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Driver",
                        tint = FigmaTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Chauffeur", fontSize = 11.sp, color = FigmaTextMuted)
                        Text(
                            text = driver?.name ?: "ID: $driverId",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaTextDark
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Timeline for stops
            Text(
                text = "Itinéraire & Livraisons",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = FigmaTextDark
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (shipments.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    shipments.forEachIndexed { index, shipment ->
                        val isLast = index == shipments.size - 1
                        val stopStatus = when (shipment.status) {
                            "DELIVERED" -> "FINISHED"
                            "IN_PROGRESS" -> "ACTIVE"
                            else -> "PENDING"
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left side: Prominent ETA / Time
                            Column(
                                modifier = Modifier.width(56.dp).padding(top = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Stop ${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FigmaTextMuted
                                )
                                Text(
                                    text = if (index == 0) "08:30" else if (index == 1) "10:15" else "11:45",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FigmaTextDark
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(14.dp))
                            
                            // Center: Visual indicator dot & connector line
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxHeight().width(24.dp)
                            ) {
                                when (stopStatus) {
                                    "FINISHED" -> {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Fini",
                                            tint = FigmaGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    "ACTIVE" -> {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(FigmaHeaderBlue)
                                                .shadow(
                                                    elevation = 4.dp,
                                                    shape = CircleShape,
                                                    ambientColor = FigmaHeaderBlue.copy(alpha = 0.3f),
                                                    spotColor = FigmaHeaderBlue.copy(alpha = 0.3f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                        }
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(FigmaTextMuted.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                        }
                                    }
                                }
                                
                                if (!isLast) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .weight(1f)
                                            .background(
                                                if (stopStatus == "FINISHED") FigmaGreen else FigmaLightGrey
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Right side: Shipment Details
                            Column(
                                modifier = Modifier.weight(1f).padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = shipment.shipmentNo ?: "N/A",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FigmaTextDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = FigmaHeaderBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = shipment.deliveryCity ?: "Ville inconnue",
                                        fontSize = 12.sp,
                                        color = FigmaTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (!isLoadingShipments) {
                Text(
                    text = "Aucune expédition associée.",
                    fontSize = 13.sp,
                    color = FigmaTextMuted
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Buttons matching Figma primary blue & outline style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val tripDateFormatted = tripDate.substring(0, 10)
                        navController.navigate("delivery?date=$tripDateFormatted")
                    },
                    modifier = Modifier.weight(1.5f).height(48.dp),
                    enabled = !allCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allCompleted) Color.Gray else FigmaHeaderBlue,
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Démarrer",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Démarrer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                
                OutlinedButton(
                    onClick = {
                        navController.navigate("${Screen.TripDetail.route}/${tripId}")
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = BorderStroke(1.dp, FigmaTextMuted.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FigmaTextDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Infos",
                        modifier = Modifier.size(18.dp),
                        tint = FigmaTextDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Infos", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Figma White Card Container
// ─────────────────────────────────────────────
@Composable
private fun FigmaCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                spotColor = FigmaShadowColor,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(PureWhite)
    ) {
        content()
    }
}
