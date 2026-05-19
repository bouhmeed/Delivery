package com.example.delivery.screens.delivery

import com.example.delivery.repository.Result







import androidx.compose.animation.core.*



import androidx.compose.foundation.background



import androidx.compose.foundation.layout.*



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



import com.example.delivery.viewmodel.delivery.FilterState



import com.example.delivery.viewmodel.delivery.SortOption



import com.example.delivery.viewmodel.delivery.SortOrder



import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



import java.time.LocalDate







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



    val tripState by viewModel.tripWithDeliveriesState.collectAsState()



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
    fun openTomTomMapsWithAllDeliveries(deliveries: List<DeliveryItem>) {
        val geocodingService = com.example.delivery.services.TomTomGeocodingService()
        
        println("🗺️ Geocoding ${deliveries.size} deliveries for map view AVEC DISTANCE UNIFIÉE")
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
                        println("❌ Failed to geocode: ${delivery.deliveryAddress}")
                    }
                }
            }
            
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
                    ).show()
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

            // TomTom: garder la destination finale uniquement dans `to=...` (pas dans sorted)
            val pointsForSorted = routePoints.dropLast(1)
            val sortedWaypoints = pointsForSorted.mapIndexed { index, coord ->
                val waypointName = when (index) {
                    0 -> "E_Driver"
                    else -> "E_Stop_${index + 1}"
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



    



    // Load data on first composition and when date changes



    LaunchedEffect(driverId, selectedDate) {



        viewModel.loadTripForDate(driverId, selectedDate)



        viewModel.loadShipmentDates(driverId)



    }



    



    // Handle operation state changes



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



        topBar = {



            TopAppBar(



                title = { Text("Suivi de mes Tournées") },



                navigationIcon = {



                    IconButton(onClick = onBackPressed) {



                        Icon(



                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,



                            contentDescription = "Retour"



                        )



                    }



                },



                actions = {



                    IconButton(



                        onClick = {



                            when (val state = tripState) {



                                is TripWithDeliveriesState.Success -> {



                                    openTomTomMapsWithAllDeliveries(state.data.deliveries)



                                }



                                else -> {



                                    android.widget.Toast.makeText(



                                        context,



                                        "Chargement des livraisons en cours...",



                                        android.widget.Toast.LENGTH_SHORT



                                    ).show()



                                }



                            }



                        }



                    ) {



                        Icon(



                            imageVector = Icons.Default.Map,



                            contentDescription = "Voir carte TomTom"



                        )



                    }



                    IconButton(



                        onClick = {



                            scope.launch {



                                viewModel.refresh(driverId)



                            }



                        },



                        enabled = !isRefreshing



                    ) {



                        Icon(



                            imageVector = Icons.Default.Refresh,



                            contentDescription = "Actualiser",



                            modifier = if (isRefreshing) Modifier.size(20.dp) else Modifier.size(24.dp)



                        )



                    }



                }



            )



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

            // Filter Section Card with responsive design
            item {
                
                if (filteredDeliveries.isEmpty() && filterState.selectedStatuses.isEmpty() && 
                    filterState.selectedTypes.isEmpty() && filterState.customerQuery.isBlank()) {
                    // Show empty filter state
                    com.example.delivery.components.EmptyFilterState(
                        onClearFilters = { viewModel.clearFilters() }
                    )
                } else {
                    // Show filter card
                    FilterSectionCard(
                        isExpanded = filtersExpanded,
                        onExpandedChange = { filtersExpanded = it },
                        selectedStatuses = filterState.selectedStatuses,
                        onStatusFilterChange = { viewModel.updateStatusFilter(it) },
                        selectedTypes = filterState.selectedTypes,
                        onTypeFilterChange = { viewModel.updateTypeFilter(it) },
                        customerQuery = filterState.customerQuery,
                        onCustomerQueryChange = { viewModel.updateCustomerQuery(it) },
                        sortBy = filterState.sortBy,
                        onSortByChange = { viewModel.updateSortOption(it) },
                        sortOrder = filterState.sortOrder,
                        onSortOrderChange = { viewModel.updateSortOrder(it) },
                        activeFiltersCount = calculateActiveFiltersCount(filterState),
                        onClearFilters = { viewModel.clearFilters() }
                    )
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

            when (val state = tripState) {

                is TripWithDeliveriesState.Loading -> {

                    item {

                        LoadingContent()

                    }

                }

                is TripWithDeliveriesState.Error -> {

                    item {

                        ErrorContent(

                            message = state.message,

                            onRetry = { viewModel.refresh(driverId) }

                        )

                    }

                }

                is TripWithDeliveriesState.Success -> {

                    if (state.data.trip == null) {

                        item {

                            NoTripTodayContent(

                                onRefresh = { viewModel.refresh(driverId) }

                            )

                        }

                    } else {

                        // Stats card

                        item {

                            DeliveryStatsCard(

                                stats = DeliveryStats(

                                    total = filteredDeliveries.size,

                                    completed = filteredDeliveries.count { it.status == "DELIVERED" },

                                    inProgress = filteredDeliveries.count { it.status == "EXPEDITION" },

                                    notStarted = filteredDeliveries.count { it.status == "TO_PLAN" },

                                    completionPercentage = if (filteredDeliveries.isNotEmpty()) {

                                        (filteredDeliveries.count { it.status == "DELIVERED" } * 100 / filteredDeliveries.size)

                                    } else {

                                        0

                                    }

                                )

                            )

                        }



                        // Delivery items with origin information from backend

                        items(filteredDeliveries) { delivery ->

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

                                }

                            )

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



    driverId: Int,



    onNavigateToDelivery: (DeliveryItem) -> Unit = {},



    onNavigateToMap: (DeliveryItem) -> Unit = {},



    onBackPressed: () -> Unit = {},



    onValidationClick: (DeliveryItem) -> Unit = {},



    onCallClick: (DeliveryItem) -> Unit = {},



    navController: NavController = rememberNavController(),



    viewModel: DeliveryTrackingViewModel = viewModel(),



    selectedDate: String? = null



) {



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



    }



    



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
private fun getTripStatusText(status: String): String {
    return when (status) {
        "PLANNING" -> "Planifiée"
        "IN_PROGRESS" -> "En cours"
        "COMPLETED" -> "Terminée"
        else -> status

    }
}



