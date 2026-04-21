package com.example.delivery.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.util.Base64
import com.example.delivery.models.ShipmentDetailFull
import com.example.delivery.models.ShipmentDisplayStatus
import com.example.delivery.models.getDisplayInfo
import com.example.delivery.ui.DesignSystem
import com.example.delivery.repository.ShipmentDetailRepository
import com.example.delivery.viewmodel.ShipmentDetailViewModel
import com.example.delivery.viewmodel.ShipmentDetailState
import com.example.delivery.viewmodel.ShipmentOperationState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewShipmentDetailScreen(
    shipmentId: Int,
    driverId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToMap: (String) -> Unit,
    navController: NavController,
    viewModel: ShipmentDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val shipmentDetailState by viewModel.shipmentDetailState.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Fonction pour ouvrir TomTom Maps avec l'adresse
    fun openTomTomMaps(address: String, city: String, zipCode: String) {
        println("🗺️ BOUTON NAVIGUER CLIQUÉ!")
        println("🗺️ Adresse: $address, $city $zipCode")
        
        // Test simple avec Toast
        android.widget.Toast.makeText(context, "Bouton Naviguer cliqué!\n$address, $city $zipCode", android.widget.Toast.LENGTH_LONG).show()
        
        try {
            // Utiliser l'API TomTom pour la navigation
            val fullAddress = "$address, $city $zipCode"
            println("🌐 URL TomTom: $fullAddress")
            
            // Essayer d'abord avec l'application TomTom
            val tomtomUri = "tomtomgo://navigate?to=$fullAddress"
            val tomtomIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(tomtomUri))
            
            if (tomtomIntent.resolveActivity(context.packageManager) != null) {
                println("✅ TomTom Go trouvé, lancement de la navigation")
                ContextCompat.startActivity(context, tomtomIntent, null)
            } else {
                println("⚠️ TomTom Go non trouvé, essai avec TomTom web")
                
                // Fallback 1: TomTom Web
                val webUri = "https://www.tomtom.com/livetraffic/?apikey=YOUR_API_KEY&center=$fullAddress"
                val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(webUri))
                
                if (webIntent.resolveActivity(context.packageManager) != null) {
                    println("✅ Navigateur web trouvé pour TomTom")
                    ContextCompat.startActivity(context, webIntent, null)
                } else {
                    println("❌ Aucune application trouvée")
                    // Fallback 2: Copier l'adresse dans le presse-papiers
                    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Address", fullAddress)
                    clipboardManager.setPrimaryClip(clip)
                    println("📋 Adresse copiée: $fullAddress")
                }
            }
        } catch (e: Exception) {
            println("❌ Erreur: ${e.message}")
            e.printStackTrace()
        }
    }
    
    LaunchedEffect(shipmentId) {
        viewModel.loadShipmentDetails(shipmentId)
    }
    
    LaunchedEffect(operationState) {
        operationState?.let { state ->
            scope.launch {
                when (state) {
                    is ShipmentOperationState.Success -> {
                        snackbarHostState.showSnackbar(state.message)
                        viewModel.clearOperationState()
                        viewModel.loadShipmentDetails(shipmentId)
                    }
                    is ShipmentOperationState.Error -> {
                        snackbarHostState.showSnackbar(state.message)
                        viewModel.clearOperationState()
                    }
                    else -> {}
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails de la livraison") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            when (val state = shipmentDetailState) {
                is ShipmentDetailState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is ShipmentDetailState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Erreur",
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Erreur de chargement",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = { viewModel.loadShipmentDetails(shipmentId) }) {
                                Text("Réessayer")
                            }
                        }
                    }
                }
                
                is ShipmentDetailState.Success -> {
                    ShipmentDetailContent(
                        shipment = state.data,
                        driverId = driverId,
                        onMarkAsDelivered = { viewModel.completeShipment(shipmentId) },
                        onNavigateToMap = { 
                            println("🗺️ ON_NAVIGATE_TO_MAP APPELÉ!")
                            val destination = state.data.destination
                            destination?.address?.let { address ->
                                destination?.city?.let { city ->
                                    destination?.postalCode?.let { zipCode ->
                                        println("🗺️ APPEL DE openTomTomMaps avec: $address, $city, $zipCode")
                                        openTomTomMaps(address, city, zipCode)
                                    } ?: run {
                                        println("⚠️ ZIP CODE NULL")
                                    }
                                } ?: run {
                                    println("⚠️ CITY NULL")
                                }
                            } ?: run {
                                println("⚠️ ADDRESS NULL")
                            }
                        },
                        onValidation = {
                            println("✅ VALIDATION CLIQUÉE!")
                            android.widget.Toast.makeText(context, "Validation de la livraison ${shipmentId}", android.widget.Toast.LENGTH_SHORT).show()
                            // TODO: Implémenter la logique de validation
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShipmentDetailContent(
    shipment: ShipmentDetailFull,
    driverId: Int,
    onMarkAsDelivered: () -> Unit,
    onNavigateToMap: () -> Unit,
    onValidation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = ShipmentDetailRepository()
    val displayStatus = repository.getDisplayStatus(shipment)
    val statusInfo: Pair<String, String> = displayStatus.getDisplayInfo()
    val statusText = statusInfo.first
    val statusColor = statusInfo.second
    val belongsToCurrentTrip = repository.belongsToCurrentTrip(shipment, driverId)
    val tripSequence = repository.getTripSequence(shipment)
    
    // Fonction pour appeler le client
    fun makePhoneCall(phoneNumber: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_CALL).apply {
            data = android.net.Uri.parse("tel:$phoneNumber")
        }
        try {
            ContextCompat.startActivity(context, intent, null)
        } catch (e: Exception) {
            // Fallback: ouvrir l'application téléphone
            val intentDial = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                data = android.net.Uri.parse("tel:$phoneNumber")
            }
            try {
                ContextCompat.startActivity(context, intentDial, null)
            } catch (e2: Exception) {
                // En dernier recours, copier le numéro
                val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Phone Number", phoneNumber)
                clipboardManager.setPrimaryClip(clip)
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Carte principale du colis
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header avec statut
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = shipment.shipmentNo ?: "N/A",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            text = "ID: ${shipment.id}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    
                    // Badge de statut
                    Surface(
                        modifier = Modifier,
                        shape = RoundedCornerShape(16.dp),
                        color = Color(android.graphics.Color.parseColor(statusColor))
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Informations principales
                NewShipmentInfoRow(
                    icon = Icons.Default.Description,
                    label = "Description",
                    value = shipment.description ?: "N/A"
                )
                
                NewShipmentInfoRow(
                    icon = Icons.Default.Inventory,
                    label = "Quantité",
                    value = "${shipment.quantity ?: 0} ${shipment.uom ?: ""}"
                )
                
                NewShipmentInfoRow(
                    icon = Icons.Default.LocalShipping,
                    label = "Type",
                    value = shipment.type ?: "N/A"
                )
                
                if (shipment.priority != null) {
                    NewShipmentInfoRow(
                        icon = Icons.Default.PriorityHigh,
                        label = "Priorité",
                        value = shipment.priority
                    )
                }
                
                // Informations de tournée si applicable
                if (belongsToCurrentTrip && tripSequence != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    NewShipmentInfoRow(
                        icon = Icons.Default.Numbers,
                        label = "Position dans la tournée",
                        value = "Arrêt n°$tripSequence"
                    )
                }
            }
        }
        
        // Carte d'adresse de livraison
        val destination = shipment.destination
        if (destination != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Adresse de livraison",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    NewShipmentInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Adresse",
                        value = destination.address ?: "N/A"
                    )
                    
                    NewShipmentInfoRow(
                        icon = Icons.Default.LocationCity,
                        label = "Ville",
                        value = "${destination.city ?: ""} ${destination.postalCode ?: ""}"
                    )
                    
                    // Afficher les informations du client si disponibles
                    if (shipment.customer != null) {
                        if (!shipment.customer.name.isNullOrEmpty()) {
                            NewShipmentInfoRow(
                                icon = Icons.Default.Person,
                                label = "Client",
                                value = shipment.customer.name
                            )
                        }
                        
                        if (!shipment.customer.phone.isNullOrEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Téléphone",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = shipment.customer.phone,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                }
                                
                                // Bouton pour appeler
                                OutlinedButton(
                                    onClick = { makePhoneCall(shipment.customer.phone!!) },
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Appeler",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Appeler", fontSize = 12.sp)
                                }
                            }
                        }
                        
                        if (!shipment.customer.email.isNullOrEmpty()) {
                            NewShipmentInfoRow(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = shipment.customer.email
                            )
                        }
                    }
                }
            }
        }
        
        // Carte d'origine
        if (shipment.origin != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "🏢 Point d'origine",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    NewShipmentInfoRow(
                        icon = Icons.Default.Business,
                        label = "Nom",
                        value = shipment.origin?.name ?: "N/A"
                    )
                    
                    NewShipmentInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Adresse",
                        value = shipment.origin?.address ?: "N/A"
                    )
                    
                    NewShipmentInfoRow(
                        icon = Icons.Default.LocationCity,
                        label = "Ville",
                        value = "${shipment.origin?.city ?: ""} ${shipment.origin?.postalCode ?: ""}"
                    )
                }
            }
        }
        
        // Informations de tournée
        if (shipment.trip != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "🚛 Informations de tournée",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    NewShipmentInfoRow(
                        icon = Icons.Default.Description,
                        label = "Tournée",
                        value = shipment.trip?.tripId ?: "N/A"
                    )
                    
                    NewShipmentInfoRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Date",
                        value = shipment.trip?.tripDate ?: "N/A"
                    )
                    
                    NewShipmentInfoRow(
                        icon = Icons.Default.Info,
                        label = "Statut",
                        value = shipment.trip?.status ?: "N/A"
                    )
                    
                    if (shipment.trip?.podDone == true) {
                        NewShipmentInfoRow(
                            icon = Icons.Default.CheckCircle,
                            label = "POD",
                            value = "Effectué"
                        )
                    }
                }
            }
        }
        
        // Signature de livraison
        println("🔍 DEBUG: deliveryProof = ${shipment.deliveryProof}")
        println("🔍 DEBUG: signatureUrl = ${shipment.deliveryProof?.signatureUrl}")
        if (shipment.deliveryProof != null && !shipment.deliveryProof.signatureUrl.isNullOrEmpty()) {
            println("✅ DEBUG: Showing signature card")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Signature de livraison",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Display signature image by decoding base64
                    val signatureUrl = shipment.deliveryProof.signatureUrl
                    println("🔍 Signature URL length: ${signatureUrl.length}")
                    
                    // Extract base64 data from data URL
                    val base64Data = if (signatureUrl.startsWith("data:image")) {
                        signatureUrl.substringAfter("base64,")
                    } else {
                        signatureUrl
                    }
                    
                    // Decode base64 to bitmap
                    val imageBitmap = remember(base64Data) {
                        try {
                            val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            println("📏 Bitmap dimensions: ${bitmap?.width}x${bitmap?.height}")
                            bitmap?.asImageBitmap()
                        } catch (e: Exception) {
                            println("❌ Error decoding signature: ${e.message}")
                            null
                        }
                    }
                    
                    if (imageBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = imageBitmap,
                            contentDescription = "Signature du client",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 1500.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Impossible de charger la signature",
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    if (shipment.deliveryProof.createdAt != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Signé le: ${shipment.deliveryProof.createdAt}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        // Photo de preuve de livraison
        if (shipment.deliveryProof != null && !shipment.deliveryProof.imageUrl.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Photo de preuve de livraison",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Display proof photo by decoding base64
                    val imageUrl = shipment.deliveryProof.imageUrl
                    println("🔍 Image URL length: ${imageUrl.length}")
                    
                    // Extract base64 data from data URL
                    val imageBase64Data = if (imageUrl.startsWith("data:image")) {
                        imageUrl.substringAfter("base64,")
                    } else {
                        imageUrl
                    }
                    
                    // Decode base64 to bitmap
                    val proofImageBitmap = remember(imageBase64Data) {
                        try {
                            val bytes = android.util.Base64.decode(imageBase64Data, android.util.Base64.DEFAULT)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            println("📏 Proof image dimensions: ${bitmap?.width}x${bitmap?.height}")
                            bitmap?.asImageBitmap()
                        } catch (e: Exception) {
                            println("❌ Error decoding proof image: ${e.message}")
                            null
                        }
                    }
                    
                    if (proofImageBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = proofImageBitmap,
                            contentDescription = "Photo de preuve de livraison",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 1500.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Impossible de charger la photo de preuve",
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        } else {
            println("❌ DEBUG: Signature card not shown - deliveryProof is null or signatureUrl is empty")
        }
        
        // Boutons d'action
        if (belongsToCurrentTrip) {
            // Première rangée: Marquer livré (si non complété) et Validation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Sizes.SPACING_MEDIUM)
            ) {
                if (displayStatus != ShipmentDisplayStatus.COMPLETED) {
                    Button(
                        onClick = onMarkAsDelivered,
                        modifier = Modifier
                            .weight(1f)
                            .height(DesignSystem.Sizes.BUTTON_HEIGHT_MEDIUM),
                        shape = RoundedCornerShape(DesignSystem.Components.BUTTON_CORNER_RADIUS),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DesignSystem.Colors.SUCCESS_GREEN,
                            contentColor = DesignSystem.Colors.SURFACE_WHITE
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = DesignSystem.Components.BUTTON_ELEVATION,
                            pressedElevation = DesignSystem.Components.BUTTON_ELEVATION_PRESSED
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(DesignSystem.Sizes.ICON_SIZE_MEDIUM)
                        )
                        Spacer(modifier = Modifier.width(DesignSystem.Sizes.SPACING_SMALL))
                        Text("Marquer livré")
                    }
                }
                
                // Bouton Validation (toujours visible)
                Button(
                    onClick = onValidation,
                    modifier = Modifier
                        .weight(1f)
                        .height(DesignSystem.Sizes.BUTTON_HEIGHT_MEDIUM),
                    shape = RoundedCornerShape(DesignSystem.Components.BUTTON_CORNER_RADIUS),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DesignSystem.Colors.VALIDATION_BLUE,
                        contentColor = DesignSystem.Colors.SURFACE_WHITE
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = DesignSystem.Components.BUTTON_ELEVATION,
                        pressedElevation = DesignSystem.Components.BUTTON_ELEVATION_PRESSED
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(DesignSystem.Sizes.ICON_SIZE_MEDIUM)
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Sizes.SPACING_SMALL))
                    Text("Validation")
                }
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Sizes.SPACING_MEDIUM))
            
            // Deuxième rangée: Navigation
            OutlinedButton(
                onClick = { 
                    println("🗺️ BOUTON CLIQUÉ DIRECTEMENT!")
                    android.widget.Toast.makeText(context, "Bouton Naviguer cliqué directement!", android.widget.Toast.LENGTH_SHORT).show()
                    onNavigateToMap()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesignSystem.Sizes.BUTTON_HEIGHT_MEDIUM),
                shape = RoundedCornerShape(DesignSystem.Components.BUTTON_CORNER_RADIUS),
                border = BorderStroke(
                    width = DesignSystem.Sizes.BORDER_MEDIUM,
                    color = DesignSystem.Colors.NAVIGATION_GREEN
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DesignSystem.Colors.NAVIGATION_GREEN
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = DesignSystem.Components.BUTTON_ELEVATION,
                    pressedElevation = DesignSystem.Components.BUTTON_ELEVATION_PRESSED
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(DesignSystem.Sizes.ICON_SIZE_MEDIUM)
                )
                Spacer(modifier = Modifier.width(DesignSystem.Sizes.SPACING_SMALL))
                Text("Navigation")
            }
        }
    }
}

@Composable
fun NewShipmentInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DesignSystem.Sizes.SPACING_MINI),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DesignSystem.Colors.PRIMARY_BLUE,
            modifier = Modifier.size(DesignSystem.Sizes.ICON_SIZE_MEDIUM)
        )
        Spacer(modifier = Modifier.width(DesignSystem.Sizes.SPACING_SMALL))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = DesignSystem.Colors.TEXT_SECONDARY
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = DesignSystem.Colors.TEXT_PRIMARY
            )
        }
    }
}
