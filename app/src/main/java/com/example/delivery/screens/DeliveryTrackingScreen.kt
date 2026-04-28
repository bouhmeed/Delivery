package com.example.delivery.screens







import androidx.compose.animation.core.*



import androidx.compose.foundation.background



import androidx.compose.foundation.layout.*



import androidx.compose.foundation.lazy.LazyColumn



import androidx.compose.foundation.lazy.items



import androidx.compose.foundation.shape.RoundedCornerShape



import androidx.compose.material.icons.Icons



import androidx.compose.material.icons.filled.*



import androidx.compose.material3.*



import androidx.compose.material3.ExperimentalMaterial3Api



import androidx.compose.runtime.*



import androidx.compose.runtime.mutableIntStateOf



import androidx.compose.ui.Alignment



import androidx.compose.ui.Modifier



import androidx.compose.ui.draw.clip



import androidx.compose.ui.graphics.Color



import androidx.compose.ui.text.font.FontWeight



import androidx.compose.ui.text.style.TextAlign



import androidx.compose.ui.unit.dp



import androidx.compose.ui.unit.sp



import androidx.compose.ui.platform.LocalContext



import androidx.lifecycle.compose.collectAsStateWithLifecycle



import androidx.lifecycle.viewmodel.compose.viewModel



import androidx.compose.runtime.rememberCoroutineScope



import androidx.navigation.NavController



import androidx.navigation.compose.rememberNavController



import com.example.delivery.components.*



import com.example.delivery.components.DeliveryItemCard



import com.example.delivery.components.DeliveryStatsCard



import com.example.delivery.components.DateFilterRow



import com.example.delivery.models.DeliveryItem



import com.example.delivery.screens.NewShipmentDetailScreen



import com.example.delivery.viewmodel.DeliveryTrackingViewModel



import com.example.delivery.viewmodel.DeliveryStats



import com.example.delivery.viewmodel.OperationState



import com.example.delivery.viewmodel.TripWithDeliveriesState



import kotlinx.coroutines.launch



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



    var searchQuery by remember { mutableStateOf("") }

    

    val context = LocalContext.current



    // Fonction pour ouvrir TomTom avec tous les points de livraison du jour
    fun openTomTomMapsWithAllDeliveries(deliveries: List<DeliveryItem>) {
        val geocodingService = com.example.delivery.services.TomTomGeocodingService()
        
        println("🗺️ Geocoding ${deliveries.size} deliveries for map view")
        
        // Utiliser le geocoding pour toutes les livraisons
        kotlinx.coroutines.GlobalScope.launch {
            val geocodedDeliveries = mutableListOf<Pair<Double, Double>>()
            
            for (delivery in deliveries) {
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
            
            // Calculer le centre de la carte (moyenne des coordonnées)
            val avgLat = geocodedDeliveries.map { it.first }.average()
            val avgLon = geocodedDeliveries.map { it.second }.average()
            val zoom = 10 // Zoom par défaut
            
            // Créer les paramètres
            val coords = geocodedDeliveries.map { "${it.first},${it.second}" }
            val stops = coords.joinToString(";")
            
            // Créer le paramètre r avec le format simplifié
            val waypoints = geocodedDeliveries.map { "h~V${it.first}~J${it.second}~Vaddr~E_Delivery" }.joinToString(",")
            val r = "(costModel:FASTEST,routingProvider:GLOBAL,sorted:($waypoints),travelMode:CAR)"
            
            // URL complète avec format correct
            val tomtomUrl = "https://plan.tomtom.com/en/route/plan?key=$tomtomApiKey&mode=car&p=$avgLat,$avgLon,${zoom}z&r=$r"
            
            println("🗺️ Opening TomTom with ${geocodedDeliveries.size} geocoded points")
            println("🔗 URL: $tomtomUrl")
            
            // Ouvrir dans le navigateur
            try {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(tomtomUrl)
                )
                context.startActivity(intent)
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



    val scope = rememberCoroutineScope()



    



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



                            imageVector = Icons.Default.ArrowBack,



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



        // Filter deliveries based on search query

        val filteredDeliveries = when (val state = tripState) {

            is TripWithDeliveriesState.Success -> {

                if (state.data.trip == null) {

                    emptyList()

                } else {

                    if (searchQuery.isBlank()) {

                        state.data.deliveries

                    } else {

                        val query = searchQuery.lowercase()

                        state.data.deliveries.filter { delivery ->

                            delivery.shipmentNo?.lowercase()?.contains(query) == true ||

                            delivery.clientName?.lowercase()?.contains(query) == true ||

                            delivery.deliveryAddress?.lowercase()?.contains(query) == true

                        }

                    }

                }

            }

            else -> emptyList()

        }



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



            // Search Bar

            item {

                OutlinedTextField(

                    value = searchQuery,

                    onValueChange = { searchQuery = it },

                    placeholder = { Text("Rechercher une livraison...") },

                    leadingIcon = {

                        Icon(Icons.Default.Search, contentDescription = "Recherche")

                    },

                    modifier = Modifier

                        .fillMaxWidth()

                        .padding(horizontal = 16.dp, vertical = 8.dp),

                    singleLine = true,

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



