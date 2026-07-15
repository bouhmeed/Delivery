package com.example.delivery.screens.delivery

import com.example.delivery.repository.Result
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.delivery.auth.AuthManager
import com.example.delivery.components.*
import com.example.delivery.components.DeliveryItemCard
import com.example.delivery.components.DeliveryStatsCard
import com.example.delivery.components.DateFilterRow
import com.example.delivery.components.FilterSectionCard
import com.example.delivery.models.delivery.DeliveryItem
import com.example.delivery.screens.delivery.NewShipmentDetailScreen
import com.example.delivery.viewmodel.delivery.DeliveryTrackingViewModel
import com.example.delivery.viewmodel.delivery.DeliveryStats
import com.example.delivery.viewmodel.delivery.OperationState
import com.example.delivery.viewmodel.delivery.TripWithDeliveriesState
import com.example.delivery.viewmodel.delivery.MultipleTripsState
import com.example.delivery.viewmodel.delivery.FilterState
import com.example.delivery.viewmodel.delivery.SortOption
import com.example.delivery.viewmodel.delivery.SortOrder
import com.example.delivery.repository.user.DirectUserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

// ─────────────────────────────────────────────
// Figma UI Kit Color Palette
// ─────────────────────────────────────────────
private val FigmaBg           = Color(0xFFEAF2F8)
private val PureWhite         = Color(0xFFFFFFFF)
private val FigmaHeaderBlue   = Color(0xFF0C6BCE)
private val FigmaTextDark     = Color(0xFF1B2A4A)
private val FigmaTextMuted    = Color(0xFF8F9BB3)
private val FigmaShadowColor  = Color(0xFF0C6BCE).copy(alpha = 0.08f)
private val FigmaLightGrey    = Color(0xFFF1F5F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryTrackingScreen(
    driverId: Int,
    onNavigateToDelivery: (DeliveryItem) -> Unit = {},
    onNavigateToMap: (DeliveryItem) -> Unit = {},
    onBackPressed: () -> Unit = {},
    onValidationClick: (DeliveryItem) -> Unit = {},
    onCallClick: (DeliveryItem) -> Unit = {},
    navController: NavController = rememberNavController(),
    viewModel: DeliveryTrackingViewModel = viewModel()
) {
    println("🔍 Screen: DeliveryTrackingScreen called with driverId=$driverId")
    val multipleTripsState by viewModel.multipleTripsState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val shipmentDates by viewModel.shipmentDates.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val filteredDeliveries by viewModel.filteredDeliveries.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filtersExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    // Helper function to calculate active filters count
    fun calculateActiveFiltersCount(filterState: FilterState): Int {
        var count = 0
        if (filterState.selectedStatuses.isNotEmpty()) count++
        if (filterState.selectedTypes.isNotEmpty()) count++
        if (filterState.customerQuery.isNotBlank()) count++
        if (filterState.sortBy != com.example.delivery.viewmodel.delivery.SortOption.SEQUENCE) count++
        return count
    }

    // Fonction pour ouvrir TomTom avec tous les points de livraison du jour - AVEC DISTANCE UNIFIÉE
    fun openTomTomAppWithMultipleDestinations(deliveries: List<DeliveryItem>) {
        val geocodingService = com.example.delivery.services.TomTomGeocodingService()
        val packageManager = context.packageManager
        println("🗺️ DEBUG: Received ${deliveries.size} deliveries for map view")
        // Try to find TomTom app
        val installedApps = packageManager.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
        val tomtomApp = installedApps.find {
            it.packageName.lowercase().contains("tomtom")
        }
        if (tomtomApp == null) {
            android.widget.Toast.makeText(context, "App TomTom non trouvée", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            // Limit deliveries to avoid too many points
            val deliveriesForMap = if (deliveries.size > 10) deliveries.take(10) else deliveries
            if (deliveries.size > 10) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(
                        context,
                        "Trop de livraisons (${deliveries.size}). Affichage des 10 premières.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
            // Geocode all delivery destinations
            val geocodedDeliveries = mutableListOf<Pair<Double, Double>>()
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                for (delivery in deliveriesForMap) {
                    val destAddress = delivery.deliveryAddress ?: delivery.fullAddress
                    val destCity = delivery.deliveryCity ?: delivery.locationCity
                    val destZipCode = delivery.deliveryZipCode ?: delivery.locationPostalCode
                    if (!destAddress.isNullOrBlank() && !destCity.isNullOrBlank()) {
                        val result = geocodingService.geocodeAddress(
                            address = destAddress,
                            city = destCity,
                            postalCode = destZipCode,
                            country = delivery.deliveryCountry ?: "France"
                        )
                        if (result != null) {
                            geocodedDeliveries.add(Pair(result.latitude, result.longitude))
                            println("✅ Geocoded: ${delivery.deliveryAddress} -> ${result.latitude}, ${result.longitude}")
                        }
                    }
                }
            }
            if (geocodedDeliveries.isEmpty()) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Aucune adresse géocodée", android.widget.Toast.LENGTH_SHORT).show()
                    // Fallback: just open TomTom app
                    val intent = packageManager.getLaunchIntentForPackage(tomtomApp.packageName)
                    intent?.let { context.startActivity(it) }
                }
                return@launch
            }
            // Open TomTom with first destination (TomTom geo URI doesn't support multiple waypoints well)
            // For multiple destinations, we'll open with the first one and user can add more
            val firstDest = geocodedDeliveries.first()
            val geoUri = android.net.Uri.parse("geo:${firstDest.first},${firstDest.second}?q=${firstDest.first},${firstDest.second}")
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, geoUri)
            intent.setPackage(tomtomApp.packageName)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(
                    context,
                    "Navigation vers ${geocodedDeliveries.size} destinations (première affichée)",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Fonction pour ouvrir TomTom avec navigation simple (comme le bouton "Naviguer")
    fun openTomTomNavigationSimple(delivery: DeliveryItem) {
        println("🗺️ BOUTON NAVIGUER CLIQUÉ!")
        val address = delivery.deliveryAddress ?: delivery.fullAddress
        val city = delivery.deliveryCity ?: delivery.locationCity
        val zipCode = delivery.deliveryZipCode ?: delivery.locationPostalCode
        println("🗺️ Adresse: $address, $city $zipCode")
        
        scope.launch {
            try {
                val fullAddress = "$address, $city $zipCode"
                println("🌐 URL TomTom: $fullAddress")
                
                // Géocoder l'adresse pour obtenir les coordonnées
                val geocodingService = com.example.delivery.services.TomTomGeocodingService()
                val geocodedResult = withContext(Dispatchers.IO) {
                    geocodingService.geocodeAddress(
                        address = address,
                        city = city,
                        postalCode = zipCode,
                        country = delivery.deliveryCountry ?: "France"
                    )
                }
                
                if (geocodedResult != null) {
                    println("✅ Geocoded: ${geocodedResult.latitude}, ${geocodedResult.longitude}")
                    
                    // Check if TomTom app is installed
                    val tomtomPackage = "com.tomtom.speedcams.android.map"
                    val tomtomInstalled = try {
                        context.packageManager.getPackageInfo(tomtomPackage, 0)
                        true
                    } catch (e: Exception) {
                        false
                    }

                    if (tomtomInstalled) {
                        println("✅ TomTom app installed, opening with coordinates")
                        // Utiliser geo URI avec coordonnées et package TomTom
                        val geoUri = "geo:${geocodedResult.latitude},${geocodedResult.longitude}?q=$fullAddress"
                        val geoIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(geoUri))
                        geoIntent.setPackage(tomtomPackage)
                        context.startActivity(geoIntent)
                    } else {
                        println("❌ TomTom app not installed, opening with default map")
                        // Ouvrir avec l'application de carte par défaut
                        val geoUri = "geo:${geocodedResult.latitude},${geocodedResult.longitude}?q=$fullAddress"
                        val geoIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(geoUri))
                        context.startActivity(geoIntent)
                    }
                } else {
                    println("❌ Geocoding failed")
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        android.widget.Toast.makeText(context, "Impossible de géocoder l'adresse", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                println("❌ Erreur: ${e.message}")
                e.printStackTrace()
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Erreur lors de l'ouverture de TomTom", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun openTomTomMapsWithAllDeliveries(deliveries: List<DeliveryItem>) {
        val geocodingService = com.example.delivery.services.TomTomGeocodingService()
        println("🗺️ DEBUG: Received ${deliveries.size} deliveries for map view")
        println("🗺️ DEBUG: Deliveries list:")
        deliveries.forEachIndexed { index, delivery ->
            println("   [$index] ID: ${delivery.shipmentId}, Address: ${delivery.deliveryAddress}, City: ${delivery.deliveryCity}, Status: ${delivery.status}")
        }
        scope.launch {
            // Limiter le nombre de points pour éviter une URL TomTom invalide/trop longue
            val deliveriesForMap = if (deliveries.size > 20) deliveries.take(20) else deliveries
            if (deliveries.size > 20) {
                android.widget.Toast.makeText(
                    context,
                    "Trop de livraisons (${deliveries.size}). Affichage des 20 premières sur la carte.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            // 🎯 CALCULER LES DISTANCES UNIFIÉES POUR TOUTES LES LIVRAISONS
            val geocodedDeliveries = mutableListOf<Pair<Double, Double>>()
            var totalUnifiedDistance = 0.0
            var calculatedDistances = 0
            withContext(Dispatchers.IO) {
                for (delivery in deliveriesForMap) {
                    println("🗺️ DEBUG: Processing delivery ${delivery.shipmentNo} - ${delivery.deliveryAddress}, ${delivery.deliveryCity}")
                    // 📍 Calculer la distance unifiée pour chaque livraison
                    val unifiedDistance = com.example.delivery.services.DistanceManager.calculateDeliveryDistance(
                        originAddress = delivery.originAddress,
                        originCity = delivery.originCity,
                        originPostalCode = delivery.originPostalCode,
                        deliveryAddress = delivery.deliveryAddress,
                        deliveryCity = delivery.deliveryCity,
                        deliveryZipCode = delivery.deliveryZipCode
                    )
                    if (unifiedDistance != null) {
                        totalUnifiedDistance += unifiedDistance
                        calculatedDistances++
                        println("📍 Distance unifiée livraison ${delivery.shipmentNo}: ${unifiedDistance.toInt()} km")
                    }
                    // Continuer avec le géocodage normal pour la map
                    val result = geocodingService.geocodeAddress(
                        address = delivery.deliveryAddress,
                        city = delivery.deliveryCity,
                        postalCode = delivery.deliveryZipCode,
                        country = delivery.deliveryCountry ?: "France"
                    )
                    if (result != null) {
                        geocodedDeliveries.add(Pair(result.latitude, result.longitude))
                        println("✅ Geocoded: ${delivery.deliveryAddress} -> ${result.latitude}, ${result.longitude}")
                    } else {
                        println("❌ Failed to geocode: ${delivery.deliveryAddress}, ${delivery.deliveryCity}")
                    }
                }
            }
            println("🗺️ DEBUG: Geocoded ${geocodedDeliveries.size} out of ${deliveriesForMap.size} deliveries")
            // 📊 Afficher les statistiques de distance unifiée
            if (calculatedDistances > 0) {
                val avgDistance = totalUnifiedDistance / calculatedDistances
                println("📊 STATISTIQUES DISTANCES UNIFIÉES:")
                println("   Total livraisons avec distance: $calculatedDistances/${deliveriesForMap.size}")
                println("   Distance totale: ${totalUnifiedDistance.toInt()} km")
                println("   Distance moyenne: ${avgDistance.toInt()} km")
                println("   Cache: ${com.example.delivery.services.DistanceManager.getCacheStats()}")
            }
            if (geocodedDeliveries.isEmpty()) {
                android.widget.Toast.makeText(
                    context,
                    "Aucune adresse n'a pu être géocodée",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                return@launch
            }
            // API Key TomTom
            val tomtomApiKey = "c92wOsiK2ds07Gzq9ZJXNRyyWeQhSYse"
            // TomTom route planner: départ dépôt + étapes de livraison + destination finale
            val firstDelivery = deliveriesForMap.firstOrNull()
            val originPoint = withContext(Dispatchers.IO) {
                if (firstDelivery?.originCity.isNullOrBlank()) {
                    null
                } else {
                    // Plus fiable: géocoder la ville (centre) plutôt qu'une adresse dépôt "factice"
                    geocodingService.geocodeAddress(
                        address = null,
                        city = firstDelivery?.originCity,
                        postalCode = firstDelivery?.originPostalCode ?: "69000",
                        country = "France"
                    )?.let { Pair(it.latitude, it.longitude) }
                }
            }
            val routePoints = mutableListOf<Pair<Double, Double>>()
            if (originPoint != null) routePoints.add(originPoint)
            routePoints.addAll(geocodedDeliveries)
            // Cas fréquent: une seule livraison -> construire une route simple (origine -> destination)
            if (geocodedDeliveries.size == 1) {
                val dest = geocodedDeliveries.first()
                val start = originPoint
                if (start == null) {
                    android.widget.Toast.makeText(
                        context,
                        "Origine introuvable pour tracer l'itinéraire",
                        android.widget.Toast.LENGTH_LONG
                    )
                    return@launch
                }
                val startLat = String.format(java.util.Locale.US, "%.6f", start.first)
                val startLon = String.format(java.util.Locale.US, "%.6f", start.second)
                val endLat = String.format(java.util.Locale.US, "%.6f", dest.first)
                val endLon = String.format(java.util.Locale.US, "%.6f", dest.second)
                val rSimple =
                    "(costModel:FASTEST,routingProvider:GLOBAL,sorted:(h~V${startLat}~J${startLon}~Vaddr~JE_Driver),travelMode:CAR,vehicleParameters:(axleWeight:-+,height:-+,length:-+,maxSpeed:-+,vehicleModelId:-+,weight:-+,width:-+))"
                val tomtomUrl =
                    "https://plan.tomtom.com/en/route/plan?key=$tomtomApiKey&p=$startLat,$startLon,11z&r=$rSimple&to=$endLat,$endLon"
                val tomtomUri = android.net.Uri.parse(tomtomUrl)
                println("🗺️ Opening TomTom (single delivery route)")
                println("🔗 URL: $tomtomUrl")
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, tomtomUri)
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "Aucune application disponible pour ouvrir la carte",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    println("❌ Erreur lors de l'ouverture de TomTom: ${e.message}")
                    android.widget.Toast.makeText(
                        context,
                        "Erreur lors de l'ouverture de TomTom",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }
            // Au minimum 2 points pour tracer un trajet
            if (routePoints.size < 2) {
                android.widget.Toast.makeText(
                    context,
                    "Impossible de tracer un trajet (points insuffisants)",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                return@launch
            }
            val startPoint = routePoints.first()
            val endPoint = routePoints.last()
            // Vue globale tournée (mobile TomTom): zoom faible pour voir les points/route
            val centerLat = routePoints.map { it.first }.average()
            val centerLon = routePoints.map { it.second }.average()
            val zoom = "5.62"
            println("🗺️ DEBUG: Total route points: ${routePoints.size}")
            println("🗺️ DEBUG: Start point: $startPoint")
            println("🗺️ DEBUG: End point: $endPoint")
            // TomTom requires: destination MUST appear in BOTH sorted: (last element) AND in to=
            // Using dropLast(1) was the root cause: the last stop was missing from sorted:
            println("🗺️ DEBUG: Points for sorted (ALL including destination): ${routePoints.size}")
            val lastStopIndex = routePoints.size - 1
            val sortedWaypoints = routePoints.mapIndexed { index, coord ->
                val waypointName = when (index) {
                    0 -> "E_Driver"
                    lastStopIndex -> "E_Destination"  // Final destination also included in sorted:
                    else -> "E_Stop_${index}"  // Sequential: E_Stop_1, E_Stop_2, ...
                }
                val lat = String.format(java.util.Locale.US, "%.6f", coord.first)
                val lon = String.format(java.util.Locale.US, "%.6f", coord.second)
                "h~V${lat}~J${lon}~Vaddr~J$waypointName"
            }.joinToString(",")
            val r = "(costModel:FASTEST,routingProvider:GLOBAL,sorted:($sortedWaypoints),travelMode:CAR,vehicleParameters:(axleWeight:-+,height:-+,length:-+,maxSpeed:-+,vehicleModelId:-+,weight:-+,width:-+))"
            val startLat = String.format(java.util.Locale.US, "%.6f", startPoint.first)
            val startLon = String.format(java.util.Locale.US, "%.6f", startPoint.second)
            val endLat = String.format(java.util.Locale.US, "%.6f", endPoint.first)
            val endLon = String.format(java.util.Locale.US, "%.6f", endPoint.second)
            // TomTom parse parfois mal le paramètre r quand il est encodé (%3A, %2C)
            // On garde r en format brut, comme l'URL de référence qui fonctionne.
            val centerLatStr = String.format(java.util.Locale.US, "%.6f", centerLat)
            val centerLonStr = String.format(java.util.Locale.US, "%.6f", centerLon)
            val tomtomUrl = "https://plan.tomtom.com/en/route/plan?key=$tomtomApiKey&p=$centerLatStr,$centerLonStr,${zoom}z&r=$r&to=$endLat,$endLon"
            val tomtomUri = android.net.Uri.parse(tomtomUrl)
            println("🗺️ Opening TomTom with ${routePoints.size} route points (origin + deliveries)")
            println("🔗 URL: $tomtomUrl")
            // Ouvrir dans le navigateur
            try {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    tomtomUri
                )
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "Aucune application disponible pour ouvrir la carte",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                println("❌ Erreur lors de l'ouverture de TomTom: ${e.message}")
                android.widget.Toast.makeText(
                    context,
                    "Erreur lors de l'ouverture de TomTom",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Calculate completion progress
    val completionProgress = remember(filteredDeliveries) {
        if (filteredDeliveries.isEmpty()) 0f
        else {
            val completed = filteredDeliveries.count { it.status == "LIVRE" || it.status == "DELIVERED" }
            completed.toFloat() / filteredDeliveries.size.toFloat()
        }
    }

    LaunchedEffect(driverId, selectedDate) {
        println("🔍 Screen: LaunchedEffect triggered - driverId=$driverId, selectedDate=$selectedDate")
        viewModel.loadAllTripsForDate(driverId, selectedDate)
        viewModel.loadShipmentDates(driverId)
    }

    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is OperationState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.clearOperationState()
                    // Refresh data for current selected date
                    viewModel.refresh(driverId)
                }
            }
            is OperationState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.clearOperationState()
                }
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = FigmaBg,
        topBar = {
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
                                    text = "Suivi de mes Tournées",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = onBackPressed,
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
                                    onClick = { viewModel.refresh(driverId) },
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Refresh",
                                        tint = PureWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        when (val state = multipleTripsState) {
                                            is MultipleTripsState.Success -> {
                                                // Naviguer vers la première livraison de la liste filtrée
                                                val firstDelivery = filteredDeliveries.firstOrNull()
                                                if (firstDelivery != null) {
                                                    openTomTomNavigationSimple(firstDelivery)
                                                } else {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Aucune livraison à naviguer",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            else -> {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Chargement des livraisons en cours...",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = "Naviguer",
                                        tint = PureWhite,
                                        modifier = Modifier.size(20.dp)
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
                LinearProgressIndicator(
                    progress = completionProgress,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = FigmaHeaderBlue,
                    trackColor = FigmaLightGrey
                )
            }
        },
        bottomBar = { BottomNavigationBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date Filter Row
            item {
                DateFilterRow(
                    selectedDate = selectedDate,
                    shipmentDates = shipmentDates,
                    onDateSelected = { newDate ->
                        viewModel.setSelectedDate(newDate)
                    },
                    onPreviousDay = { viewModel.goToPreviousDay(driverId) },
                    onNextDay = { viewModel.goToNextDay(driverId) },
                    onTodayClick = { viewModel.goToToday(driverId) }
                )
            }
            // Fast-access FilterChips row for instant filtering
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusOptions = listOf("Tout", "En expédition", "Livrée")
                    val statusMapping = mapOf(
                        "Tout" to emptySet(),
                        "En expédition" to setOf("EXPEDITION"),
                        "Livrée" to setOf("LIVRE", "DELIVERED")
                    )
                    statusOptions.forEach { status ->
                        val isSelected = when (status) {
                            "Tout" -> filterState.selectedStatuses.isEmpty()
                            else -> filterState.selectedStatuses == statusMapping[status]
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateStatusFilter(statusMapping[status] ?: emptySet())
                            },
                            label = { Text(status) },
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
            // Search Bar with responsive design
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher une livraison...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = if (isTablet) 24.dp else 16.dp,
                            vertical = 8.dp
                        ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            // Content based on state
            when (val state = multipleTripsState) {
                is MultipleTripsState.Loading -> {
                    item {
                        LoadingContent()
                    }
                }
                is MultipleTripsState.Error -> {
                    item {
                        ErrorContent(
                            message = state.message,
                            onRetry = { viewModel.refresh(driverId) }
                        )
                    }
                }
                is MultipleTripsState.Success -> {
                    if (state.data.trips.isEmpty()) {
                        item {
                            NoTripTodayContent(
                                onRefresh = { viewModel.refresh(driverId) }
                            )
                        }
                    } else {
                        // Stats card for all trips
                        val allDeliveries = state.data.trips.flatMap { it.deliveries }
                        item {
                            DeliveryStatsCard(
                                stats = DeliveryStats(
                                    total = allDeliveries.size,
                                    completed = allDeliveries.count { it.status == "LIVRE" || it.status == "DELIVERED" },
                                    inProgress = allDeliveries.count { it.status == "EXPEDITION" },
                                    notStarted = allDeliveries.count { it.status == "TO_PLAN" },
                                    completionPercentage = if (allDeliveries.isNotEmpty()) {
                                        (allDeliveries.count { it.status == "LIVRE" || it.status == "DELIVERED" } * 100 / allDeliveries.size)
                                    } else {
                                        0
                                    }
                                )
                            )
                        }
                        
                        // Display each trip with its deliveries
                        state.data.trips.forEach { tripWithDeliveries ->
                            // Trip header card
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .height(48.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF102A43)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal =12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Trajet: ${tripWithDeliveries.trip?.tripId ?: "N/A"}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "• ${getTripStatusText(tripWithDeliveries.trip?.status ?: "N/A")}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                        Text(
                                            text = "${tripWithDeliveries.deliveries.size}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            
                            // Deliveries for this trip (filtered)
                            val filteredDeliveriesForTrip = tripWithDeliveries.deliveries.filter { delivery ->
                                filteredDeliveries.any { it.shipmentId == delivery.shipmentId }
                            }
                            items(filteredDeliveriesForTrip, key = { it.shipmentId }) { delivery ->
                                DeliveryItemCard(
                                    delivery = delivery,
                                    onItemClick = onNavigateToDelivery,
                                    onCompleteClick = { viewModel.completeDelivery(delivery.shipmentId) },
                                    onNavigateClick = onNavigateToMap,
                                    onValidationClick = onValidationClick,
                                    onCallClick = onCallClick,
                                    onStatusChange = { deliveryItem, newStatus ->
                                        val tripShipmentLinkId = deliveryItem.tripShipmentLinkId ?: deliveryItem.shipmentId
                                        viewModel.updateTripShipmentStatus(tripShipmentLinkId, newStatus, driverId)
                                    },
                                    onReturnsClick = { deliveryItem ->
                                        navController.navigate("returns/${deliveryItem.shipmentId}")
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {
                    item {
                        ErrorContent(
                            message = "État inconnu",
                            onRetry = { viewModel.refresh(driverId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Chargement des livraisons...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Erreur",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Erreur de chargement",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Réessayer",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Réessayer")
            }
        }
    }
}

@Composable
private fun NoTripTodayContent(
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = "Aucune tournée",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Aucune tournée aujourd'hui",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Vous n'avez pas de livraisons prévues pour cette date.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            OutlinedButton(
                onClick = onRefresh
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualiser",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Actualiser")
            }
        }
    }
}

@Composable
fun DeliveryTrackingScreenWithDetails(
    onNavigateToDelivery: (DeliveryItem) -> Unit = {},
    onNavigateToMap: (DeliveryItem) -> Unit = {},
    onBackPressed: () -> Unit = {},
    onValidationClick: (DeliveryItem) -> Unit = {},
    onCallClick: (DeliveryItem) -> Unit = {},
    navController: NavController = rememberNavController(),
    viewModel: DeliveryTrackingViewModel = viewModel(),
    selectedDate: String? = null
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val userEmail = remember { authManager.getUserEmail() }
    val userRepository = remember { DirectUserRepository() }
    
    // State for driverId
    var driverId by remember { mutableIntStateOf(1) }
    var isLoadingDriver by remember { mutableStateOf(true) }
    
    // Fetch driverId from logged-in user
    LaunchedEffect(userEmail) {
        userEmail?.let { email ->
            isLoadingDriver = true
            withContext(Dispatchers.IO) {
                val userResult = userRepository.getUserByEmail(email)
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    val fetchedDriverId = user?.driverId?.toIntOrNull() ?: 1
                    driverId = fetchedDriverId
                    println("🔍 DeliveryTrackingScreenWithDetails: Fetched driverId=$driverId for user=$email")
                } else {
                    println("🔍 DeliveryTrackingScreenWithDetails: Failed to fetch user, using default driverId=1")
                }
                isLoadingDriver = false
            }
        }
    }
    
    // État pour gérer l'affichage des détails de livraison
    var showShipmentDetails by remember { mutableStateOf(false) }
    var selectedShipmentId by remember { mutableIntStateOf(0) }
    // État pour gérer l'affichage de la carte de navigation
    var showDriverMap by remember { mutableStateOf(false) }
    var selectedDeliveryForNavigation by remember { mutableStateOf<DeliveryItem?>(null) }
    // Si une date spécifique est fournie, la définir dans le ViewModel
    LaunchedEffect(selectedDate) {
        selectedDate?.let { dateString ->
            try {
                val localDate = LocalDate.parse(dateString)
                viewModel.setSelectedDate(localDate)
            } catch (e: Exception) {
                // En cas d'erreur de parsing, utiliser la date du jour
                println("Erreur de parsing de la date: $dateString")
            }
        }
    }
    // Fonction pour naviguer vers les détails
    val handleNavigateToDelivery: (DeliveryItem) -> Unit = { delivery ->
        showShipmentDetails = true
        selectedShipmentId = delivery.shipmentId
    }
    // Fonction pour naviguer vers la carte interne
    val handleNavigateToMap: (DeliveryItem) -> Unit = { delivery ->
        selectedDeliveryForNavigation = delivery
        showDriverMap = true
        showDriverMap = true
    }
    
    if (isLoadingDriver) {
        // Show loading state while fetching driverId
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box {
            DeliveryTrackingScreen(
                driverId = driverId,
                onNavigateToDelivery = handleNavigateToDelivery,
                onNavigateToMap = handleNavigateToMap,
                onBackPressed = onBackPressed,
                onValidationClick = onValidationClick,
                onCallClick = onCallClick,
                navController = navController,
                viewModel = viewModel
            )
            // Afficher l'écran de détails de livraison si nécessaire
            if (showShipmentDetails) {
                NewShipmentDetailScreen(
                    shipmentId = selectedShipmentId,
                    driverId = driverId,
                    onNavigateBack = {
                        showShipmentDetails = false
                        selectedShipmentId = 0
                    },
                    onNavigateToMap = { address ->
                        // Utiliser la navigation interne avec la carte
                        selectedDeliveryForNavigation?.let { delivery ->
                            showDriverMap = true
                        }
                    },
                    navController = navController
                )
            }
            // Afficher l'écran de navigation interne si nécessaire
            // DriverMapScreen disabled - using web navigation instead
            /*
            if (showDriverMap && selectedDeliveryForNavigation != null) {
                DriverMapScreen(
                    delivery = selectedDeliveryForNavigation!!,
                    onBackPressed = {
                        showDriverMap = false
                        selectedDeliveryForNavigation = null
                    },
                    navController = navController
                )
            }
            */
        }
    }
}

private fun getTripStatusText(status: String): String {
    return when (status) {
        "PLANNING" -> "Planifiée"
        "IN_PROGRESS" -> "En cours"
        "COMPLETED" -> "Terminée"
        else -> status
    }
}