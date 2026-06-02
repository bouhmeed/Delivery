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
    
    // États pour la recherche
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
        
    // États pour les données Trip et User
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoadingTrips by remember { mutableStateOf(false) }
    var tripError by remember { mutableStateOf<String?>(null) }
    var currentDriver by remember { mutableStateOf<UserResponse?>(null) }
    var isLoadingUser by remember { mutableStateOf(false) }
    
    // États pour les expéditions
    var tripShipments by remember { mutableStateOf<Map<Int, List<com.example.delivery.models.delivery.ShipmentSearchDetail>>>(emptyMap()) }
    var isLoadingShipments by remember { mutableStateOf(false) }
    
    // Cache pour les véhicules et chauffeurs
    var vehicleCache by remember { mutableStateOf<Map<String, Vehicle>>(emptyMap()) }
    var driverCache by remember { mutableStateOf<Map<String, Driver>>(emptyMap()) }
    
    // Fonctions pour gérer le cache
    fun addToVehicleCache(id: String, vehicle: Vehicle) {
        vehicleCache = vehicleCache + (id to vehicle)
    }
    
    fun addToDriverCache(id: String, driver: Driver) {
        driverCache = driverCache + (id to driver)
    }
    
    // Fonction pour charger les expéditions d'un trip
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
    
    // Fonction pour charger les trips du driver connecté
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
    
    // Fonction pour charger les informations du driver connecté
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
    
    // Fonction pour recharger (utilise le driverId déjà chargé)
    fun refreshTrips() {
        val driverId = currentDriver?.driverId?.toString()
        if (driverId != null) {
            loadDriverTrips(driverId)
        } else {
            loadCurrentUser()
        }
    }
    
    // Charger l'utilisateur et ses trips au démarrage
    LaunchedEffect(Unit) {
        loadCurrentUser()
    }
    
    // Fonction pour extraire la date d'un trip
    fun getTripDate(trip: Trip): LocalDate? {
        return try {
            if (trip.tripDate.isNotEmpty()) {
                LocalDate.parse(trip.tripDate.substring(0, 10))
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    // Filtrer les trips par date sélectionnée ET recherche
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
    
    // Compter les trips par jour pour le calendrier
    val tripsByDate = remember(trips) {
        trips.groupBy { trip -> getTripDate(trip) }
            .mapValues { it.value.size }
            .filterKeys { it != null }
            .mapKeys { it.key!! }
    }
    
    // Calculer le taux de progression global
    val completionProgress = remember(tripsForSelectedDate) {
        if (tripsForSelectedDate.isEmpty()) 0f
        else {
            val completed = tripsForSelectedDate.count { it.status == "COMPLETED" }
            completed.toFloat() / tripsForSelectedDate.size.toFloat()
        }
    }
    
    Scaffold(
        topBar = {
            Column {
                // Sleek custom bar container matching 0xFFEBF4FF
                TopAppBar(
                    title = {
                        Text(
                            text = "Ma Tournée",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF102A43) // Deep navy
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF102A43)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { refreshTrips() }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh",
                                tint = Color(0xFF102A43)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
                // Thin linear progress bar right beneath TopAppBar representing trip completion percentage
                LinearProgressIndicator(
                    progress = completionProgress,
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = Color(0xFF1976D2),
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        // Précharger les expéditions pour les trips affichés
        LaunchedEffect(tripsForSelectedDate) {
            tripsForSelectedDate.forEach { trip ->
                loadTripShipments(trip.id.toInt())
            }
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7FAFC))
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // En-tête avec calendrier mensuel complet
            item {
                TourneeCalendarCard(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    toursByDate = tripsByDate,
                    onDateSelected = { date -> selectedDate = date },
                    onMonthChanged = { month -> currentMonth = month }
                )
            }
            
            // Barre de recherche
            item {
                TripSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchActiveChange = { isSearchActive = it },
                    onClearSearch = { searchQuery = "" },
                    placeholder = "Rechercher un trip..."
                )
            }
            
            // Section titre
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trips du ${selectedDate.dayOfMonth} ${selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${selectedDate.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF102A43),
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "${tripsForSelectedDate.size} résultat(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Afficher les trips du jour sélectionné
            if (tripsForSelectedDate.isNotEmpty()) {
                items(tripsForSelectedDate) { trip ->
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
            } else if (!isLoadingTrips && !isLoadingUser && tripError == null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
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
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Aucun trip prévu",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Profitez de cette journée ou sélectionnez une autre date.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// Modern Monthly Calendar Component
@Composable
fun TourneeCalendarCard(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    toursByDate: Map<LocalDate, Int>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Modern Month Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMonthChanged(currentMonth.minusMonths(1)) },
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF8FAFC))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Mois précédent",
                        tint = Color(0xFF102A43),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF102A43)
                )
                
                IconButton(
                    onClick = { onMonthChanged(currentMonth.plusMonths(1)) },
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF8FAFC))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Mois suivant",
                        tint = Color(0xFF102A43),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Days of the week header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
    
    // Correct calculation for first day of week (Monday = 0)
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value - 1) % 7
    
    // Create calendar days
    val calendarDays = mutableListOf<LocalDate?>()
    
    // Add empty days at the beginning
    repeat(firstDayOfWeek) {
        calendarDays.add(null)
    }
    
    // Add all days of the month
    for (day in 1..lastDayOfMonth.dayOfMonth) {
        calendarDays.add(currentMonth.atDay(day))
    }
    
    // Display in 7-column grid
    Column {
        var weekDays = mutableListOf<LocalDate?>()
        
        calendarDays.forEachIndexed { index, date ->
            weekDays.add(date)
            
            // Create a row each week (7 days)
            if ((index + 1) % 7 == 0 || index == calendarDays.lastIndex) {
                // Complete the week if necessary
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
            .size(44.dp)
            .clickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            // Selected day: solid blue circle
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1976D2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Regular day
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1E293B),
                        fontWeight = if (date == LocalDate.now()) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    // Tiny vibrant indicator dot for days with tours
                    if (hasTour && tourCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1976D2))
                        )
                    }
                }
            }
        }
    }
}

// 2. TIMELINE LAYOUT FOR STOPS & PREMIUM CARD DETAILS
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
    
    val formattedDate = try {
        if (tripDate.isNotEmpty()) {
            val date = LocalDate.parse(tripDate.substring(0, 10))
            DateTimeFormatter.ofPattern("dd MMMM yyyy").format(date)
        } else "Date inconnue"
    } catch (e: Exception) {
        "Date inconnue"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .clickable { onTripClick(tripId) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF102A43)
                    )
                    if (tripIdentifier != null) {
                        Text(
                            text = tripIdentifier,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF627D98)
                        )
                    }
                }
                
                // Redesigned Pill Badge
                val (bgColor, textColor, label) = when (status) {
                    "COMPLETED" -> Triple(Color(0xFFE6F4EA), Color(0xFF137333), "Terminé")
                    "IN_PROGRESS" -> Triple(Color(0xFFE8F0FE), Color(0xFF1A73E8), "En cours")
                    "READY" -> Triple(Color(0xFFFEF7E0), Color(0xFFB06000), "Prêt")
                    else -> Triple(Color(0xFFF1F3F4), Color(0xFF5F6368), status)
                }
                
                Surface(
                    modifier = Modifier.clip(CircleShape),
                    color = bgColor
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
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
                        tint = Color(0xFF627D98),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Véhicule", style = MaterialTheme.typography.bodySmall, color = Color(0xFF829AB1))
                        Text(
                            text = vehicle?.name ?: "ID: $vehicleId",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF102A43)
                        )
                    }
                }
                
                // Driver info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Driver",
                        tint = Color(0xFF627D98),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Chauffeur", style = MaterialTheme.typography.bodySmall, color = Color(0xFF829AB1))
                        Text(
                            text = driver?.name ?: "ID: $driverId",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF102A43)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 2. TIMELINE LAYOUT FOR STOPS
            Text(
                text = "Itinéraire & Livraisons",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF102A43)
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
                                modifier = Modifier.width(60.dp).padding(top = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Stop ${index + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF829AB1)
                                )
                                Text(
                                    text = if (index == 0) "08:30" else if (index == 1) "10:15" else "11:45",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF102A43)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
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
                                            tint = Color(0xFF137333),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    "ACTIVE" -> {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1A73E8))
                                                .shadow(
                                                    elevation = 8.dp,
                                                    shape = CircleShape,
                                                    ambientColor = Color(0xFF1A73E8).copy(alpha = 0.4f),
                                                    spotColor = Color(0xFF1A73E8).copy(alpha = 0.4f)
                                                ),
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
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFBDC1C6)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
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
                                                if (stopStatus == "FINISHED") Color(0xFF137333) else Color(0xFFE2E8F0)
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
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF102A43)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF1976D2),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = shipment.deliveryCity ?: "Ville inconnue",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF627D98)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (!isLoadingShipments) {
                Text(
                    text = "Aucune expédition associée.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF829AB1)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Redesigned Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val tripDateFormatted = tripDate.substring(0, 10)
                        navController.navigate("delivery?date=$tripDateFormatted")
                    },
                    modifier = Modifier.weight(1.5f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Démarrer",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Démarrer", fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = {
                        navController.navigate("${Screen.TripDetail.route}/${tripId}")
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Infos",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Infos", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
