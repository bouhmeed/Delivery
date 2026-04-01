package com.example.delivery.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import com.example.delivery.navigation.Screen
import com.example.delivery.network.ApiClient
import com.example.delivery.network.TripApiService
import com.example.delivery.network.UserApiService
import com.example.delivery.network.VehicleApiService
import com.example.delivery.network.DriverApiService
import com.example.delivery.network.ShipmentApiService
import com.example.delivery.auth.AuthManager
import com.example.delivery.models.UserResponse
import com.example.delivery.models.Vehicle
import com.example.delivery.models.Driver
import com.example.delivery.models.Trip
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
    val tripApiService = ApiClient.instance.create(TripApiService::class.java)
    val userApiService = ApiClient.instance.create(UserApiService::class.java)
    val vehicleApiService = ApiClient.instance.create(VehicleApiService::class.java)
    val driverApiService = ApiClient.instance.create(DriverApiService::class.java)
    val shipmentApiService = ApiClient.instance.create(ShipmentApiService::class.java)
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
    var tripShipments by remember { mutableStateOf<Map<Int, List<com.example.delivery.models.ShipmentSearchDetail>>>(emptyMap()) }
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
                    val response: Response<List<com.example.delivery.models.ShipmentSearchDetail>> = shipmentApiService.getShipmentsByTrip(tripId)
                    if (response.isSuccessful) {
                        val rawShipments = response.body() ?: emptyList()
                        // Mapper la réponse brute vers ShipmentSearchDetail
                        val mappedShipments = rawShipments.map { raw ->
                            com.example.delivery.models.ShipmentSearchDetail(
                                id = raw.id,
                                shipmentNo = raw.shipmentNo,
                                trackingNumber = raw.trackingNumber,
                                status = raw.status,
                                description = raw.description ?: "",
                                quantity = 1, // Valeur par défaut
                                deliveryAddress = "", // Valeur par défaut
                                deliveryCity = raw.deliveryCity ?: "",
                                deliveryZipCode = "", // Valeur par défaut
                                customerId = 0, // Valeur par défaut
                                priority = "NORMAL", // Valeur par défaut
                                plannedStart = null,
                                plannedEnd = null
                            )
                        }
                        tripShipments = tripShipments + (tripId to mappedShipments)
                        Toast.makeText(context, "✅ ${mappedShipments.size} expéditions chargées pour trip $tripId", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ Erreur chargement expéditions trip $tripId: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "❌ Exception chargement expéditions trip $tripId: ${e.message}", Toast.LENGTH_SHORT).show()
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
                val response: Response<List<Trip>> = tripApiService.getTripsByDriver(driverId)
                if (response.isSuccessful) {
                    trips = response.body() ?: emptyList()
                    Toast.makeText(context, "✅ ${trips.size} trajets chargés pour le driver", Toast.LENGTH_SHORT).show()
                } else {
                    tripError = "Erreur ${response.code()}: ${response.message()}"
                    Toast.makeText(context, tripError, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                tripError = "Exception: ${e.message}"
                Toast.makeText(context, tripError, Toast.LENGTH_LONG).show()
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
                    val response = userApiService.getUserByEmail(userEmail)
                    if (response.isSuccessful) {
                        currentDriver = response.body()
                        // Une fois le driver chargé, charger ses trips
                        val driverId = response.body()?.driverId?.toString()
                        if (driverId != null) {
                            loadDriverTrips(driverId)
                        } else {
                            tripError = "Aucun driverId trouvé pour cet utilisateur"
                            isLoadingTrips = false
                        }
                    } else {
                        tripError = "Erreur chargement utilisateur: ${response.code()}"
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
            loadCurrentUser() // Recharger depuis le début
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
                // Parser la date ISO 8601
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
                // Rechercher dans tripId, statut, et autres champs pertinents
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
    
    // Utiliser uniquement les données réelles des trips
    val realToursByDate = remember(tripsByDate) {
        tripsByDate
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Ma Tournee", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { showStats = !showStats }) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "Statistiques",
                            tint = if (showStats) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
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
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // En-tête avec calendrier uniquement
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Calendrier des tournées
                    TourneeCalendarCard(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        toursByDate = realToursByDate,
                        onDateSelected = { date -> selectedDate = date },
                        onMonthChanged = { month -> currentMonth = month }
                    )
                }
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
            
            // Trips du jour sélectionné avec résultats de recherche
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trips du ${selectedDate.dayOfMonth} ${selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${selectedDate.year}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (searchQuery.isNotBlank()) {
                            Text(
                                text = "${tripsForSelectedDate.size} résultat(s)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "Recherche: '$searchQuery'",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Afficher les trips du jour sélectionné
            if (tripsForSelectedDate.isNotEmpty()) {
                items(tripsForSelectedDate) { trip ->
                    TripDetailCard(
                        trip = trip,
                        vehicleApiService = vehicleApiService,
                        driverApiService = driverApiService,
                        vehicleCache = vehicleCache,
                        driverCache = driverCache,
                        onVehicleLoaded = { id, vehicle -> addToVehicleCache(id, vehicle) },
                        onDriverLoaded = { id, driver -> addToDriverCache(id, driver) },
                        onTripClick = { tripId -> 
                            // Naviguer vers le suivi des tournées avec la date spécifique de la tournée
                            val tripDateFormatted = trip.tripDate.substring(0, 10) // Extraire YYYY-MM-DD
                            navController.navigate("delivery?date=$tripDateFormatted")
                        },
                        shipmentApiService = shipmentApiService,
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
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = "Aucun trip",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aucun trip prévu",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Pour le ${selectedDate.dayOfMonth} ${selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}


// Composants pour le calendrier des tournées
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
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // En-tête du calendrier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMonthChanged(currentMonth.minusMonths(1)) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft, 
                        contentDescription = "Mois précédent",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(
                    onClick = { onMonthChanged(currentMonth.plusMonths(1)) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                        contentDescription = "Mois suivant",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Jours de la semaine
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Grille du calendrier
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
    
    // Corriger le calcul du premier jour de la semaine
    // En Java/Kotlin: Monday=1, Tuesday=2, ..., Sunday=7
    // On veut que Monday=0, Tuesday=1, ..., Sunday=6 pour commencer par lundi
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value - 1) % 7
    
    // Créer les jours du calendrier
    val calendarDays = mutableListOf<LocalDate?>()
    
    // Ajouter les jours vides du début
    repeat(firstDayOfWeek) {
        calendarDays.add(null)
    }
    
    // Ajouter tous les jours du mois
    for (day in 1..lastDayOfMonth.dayOfMonth) {
        calendarDays.add(currentMonth.atDay(day))
    }
    
    // Afficher en grille de 7 colonnes
    Column {
        var weekDays = mutableListOf<LocalDate?>()
        
        calendarDays.forEachIndexed { index, date ->
            weekDays.add(date)
            
            // Créer une ligne chaque semaine (7 jours)
            if ((index + 1) % 7 == 0 || index == calendarDays.lastIndex) {
                // Compléter la semaine si nécessaire
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
                    Spacer(modifier = Modifier.height(4.dp))
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
    Card(
        modifier = Modifier
            .size(40.dp)
            .clickable(enabled = date != null, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                hasTour -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (date != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    if (hasTour && tourCount > 0) {
                        Text(
                            text = tourCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}




// Composant pour afficher une expédition
@Composable
fun ShipmentItemRow(shipment: com.example.delivery.models.ShipmentSearchDetail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Numéro d'expédition en gras
                Text(
                    text = shipment.shipmentNo ?: "N/A",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Ville de livraison avec icône
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ville",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = shipment.deliveryCity ?: "Ville inconnue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Statut de l'expédition
            val statusColor = when (shipment.status) {
                "DELIVERED" -> MaterialTheme.colorScheme.tertiary
                "IN_PROGRESS" -> MaterialTheme.colorScheme.primary
                "PENDING" -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.outline
            }
            
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = shipment.status,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
@Composable
fun TripDetailCard(
    trip: Trip,
    vehicleApiService: VehicleApiService,
    driverApiService: DriverApiService,
    vehicleCache: Map<String, Vehicle>,
    driverCache: Map<String, Driver>,
    onVehicleLoaded: (String, Vehicle) -> Unit,
    onDriverLoaded: (String, Driver) -> Unit,
    onTripClick: (String) -> Unit,
    shipmentApiService: ShipmentApiService? = null,
    tripShipments: Map<Int, List<com.example.delivery.models.ShipmentSearchDetail>>? = null,
    onLoadShipments: ((Int) -> Unit)? = null,
    navController: NavController
) {
    val tripId = trip.id
    val tripDate = trip.tripDate
    val driverId = trip.driverId ?: "N/A"
    val vehicleId = trip.vehicleId ?: "N/A"
    val status = trip.status
    val tripIdentifier = trip.tripId
    
    // États pour les données récupérées
    var vehicle by remember { mutableStateOf(vehicleCache[vehicleId]) }
    var driver by remember { mutableStateOf(driverCache[driverId]) }
    val initialVehicle = vehicleCache[vehicleId]
    val initialDriver = driverCache[driverId]
    var isLoadingDetails by remember { mutableStateOf(initialVehicle == null && vehicleId != "N/A" || initialDriver == null && driverId != "N/A") }
    
    // États pour les expéditions
    var shipments by remember { mutableStateOf<List<com.example.delivery.models.ShipmentSearchDetail>>(emptyList()) }
    var isLoadingShipments by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Charger les expéditions depuis le cache ou l'API
    LaunchedEffect(tripId) {
        // D'abord vérifier si les expéditions sont déjà dans le cache
        val cachedShipments = tripShipments?.get(tripId.toInt())
        if (cachedShipments != null) {
            shipments = cachedShipments
        } else if (onLoadShipments != null) {
            // Sinon les charger via l'API
            isLoadingShipments = true
            onLoadShipments(tripId.toInt())
            // Attendre un peu que les données se chargent dans le cache
            kotlinx.coroutines.delay(500)
            // Vérifier à nouveau si les données sont maintenant dans le cache
            val updatedShipments = tripShipments?.get(tripId.toInt())
            if (updatedShipments != null) {
                shipments = updatedShipments
            }
            isLoadingShipments = false
        }
    }
    
    // Mettre à jour les expéditions si le cache est mis à jour
    LaunchedEffect(tripShipments) {
        val cachedShipments = tripShipments?.get(tripId.toInt())
        if (cachedShipments != null) {
            shipments = cachedShipments
        }
    }
    
    // Charger les détails uniquement si pas dans le cache
    LaunchedEffect(vehicleId, driverId) {
        val currentVehicle = vehicle
        val currentDriver = driver
        
        if ((currentVehicle == null && vehicleId != "N/A") || (currentDriver == null && driverId != "N/A")) {
            isLoadingDetails = true
            coroutineScope.launch {
                try {
                    // Récupérer les infos du véhicule si pas en cache
                    if (currentVehicle == null && vehicleId != "N/A") {
                        val vehicleResponse = vehicleApiService.getVehicleById(vehicleId)
                        if (vehicleResponse.isSuccessful) {
                            vehicleResponse.body()?.let { v ->
                                vehicle = v
                                onVehicleLoaded(vehicleId, v)
                            }
                        }
                    }
                    
                    // Récupérer les infos du chauffeur si pas en cache
                    if (currentDriver == null && driverId != "N/A") {
                        val driverResponse = driverApiService.getDriverById(driverId)
                        if (driverResponse.isSuccessful) {
                            driverResponse.body()?.let { d ->
                                driver = d
                                onDriverLoaded(driverId, d)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Erreur silencieuse pour ne pas bloquer l'UI
                } finally {
                    isLoadingDetails = false
                }
            }
        }
    }
    
    // Formater la date
    val formattedDate = try {
        if (tripDate.isNotEmpty()) {
            val date = LocalDate.parse(tripDate.substring(0, 10))
            DateTimeFormatter.ofPattern("dd MMMM yyyy").format(date)
        } else "Date inconnue"
    } catch (e: Exception) {
        "Date inconnue"
    }
    
    // Couleur du statut
    val statusColor = when (status) {
        "READY" -> MaterialTheme.colorScheme.secondary
        "COMPLETED" -> MaterialTheme.colorScheme.tertiary
        "IN_PROGRESS" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    
    // Icône du statut
    val statusIcon = when (status) {
        "READY" -> Icons.Default.PlayArrow
        "COMPLETED" -> Icons.Default.CheckCircle
        "IN_PROGRESS" -> Icons.Default.DirectionsCar
        else -> Icons.AutoMirrored.Filled.Help
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { onTripClick(tripId) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // En-tête avec ID et statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trip #$tripId",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (tripIdentifier != null) {
                        Text(
                            text = tripIdentifier,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = status,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Date",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Informations véhicule et driver avec noms réels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Véhicule",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Véhicule",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val currentVehicleForLoading = vehicle
                        val currentDriverForLoading = driver
                        if (isLoadingDetails && currentVehicleForLoading == null) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            val currentVehicle = vehicle
                            if (currentVehicle != null) {
                                Text(
                                    text = currentVehicle.name ?: "Véhicule inconnu (ID: $vehicleId)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (currentVehicle.registration != null) {
                                    Text(
                                        text = "Immat: ${currentVehicle.registration}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(
                                    text = "Véhicule inconnu (ID: $vehicleId)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Driver",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Chauffeur",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val currentDriverForLoading = driver
                        if (isLoadingDetails && currentDriverForLoading == null) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            val currentDriver = driver
                            if (currentDriver != null) {
                                Text(
                                    text = currentDriver.name ?: "Chauffeur inconnu (ID: $driverId)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (currentDriver.licenseNumber != null) {
                                    Text(
                                        text = "Permis: ${currentDriver.licenseNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(
                                    text = "Chauffeur inconnu (ID: $driverId)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Section Expéditions
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = "Expéditions",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Expéditions",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        if (isLoadingShipments) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "${shipments.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    if (shipments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Afficher les 2 premières expéditions avec shipmentNo et deliveryCity
                        shipments.take(2).forEach { shipment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = shipment.shipmentNo ?: "N/A",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Ville",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = shipment.deliveryCity ?: "Ville inconnue",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                // Statut de l'expédition
                                val statusColor = when (shipment.status) {
                                    "DELIVERED" -> MaterialTheme.colorScheme.tertiary
                                    "IN_PROGRESS" -> MaterialTheme.colorScheme.primary
                                    "PENDING" -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.outline
                                }
                                
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                                    color = statusColor.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = shipment.status,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        
                        if (shipments.size > 2) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "... et ${shipments.size - 2} autre(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    } else if (!isLoadingShipments) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aucune expédition",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Boutons d'action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                // Naviguer vers le suivi des tournées avec la date spécifique de la tournée
                                val tripDateFormatted = tripDate.substring(0, 10) // Extraire YYYY-MM-DD
                                navController.navigate("delivery?date=$tripDateFormatted")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Validé",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Validé")
                        }
                        
                        OutlinedButton(
                            onClick = { 
                                // Naviguer vers les détails du trajet
                                navController.navigate("${Screen.TripDetail.route}/${tripId}")
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Détail",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Détail")
                        }
                    }
                }
            }
        }
    }
}
