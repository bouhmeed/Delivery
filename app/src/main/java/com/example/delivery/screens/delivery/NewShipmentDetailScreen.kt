package com.example.delivery.screens.delivery



import androidx.compose.foundation.background

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.horizontalScroll

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.shadow

import androidx.compose.ui.graphics.Brush
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

import androidx.lifecycle.compose.LocalLifecycleOwner

import androidx.lifecycle.LifecycleEventObserver

import androidx.lifecycle.Lifecycle

import androidx.navigation.NavController

import coil.compose.AsyncImage

import coil.compose.rememberAsyncImagePainter

import coil.request.ImageRequest

import java.util.Base64

import com.example.delivery.models.delivery.ShipmentDetailFull

import com.example.delivery.models.delivery.ShipmentDisplayStatus

import com.example.delivery.models.delivery.getDisplayInfo

import com.example.delivery.ui.DesignSystem

import com.example.delivery.repository.delivery.DirectShipmentDetailRepository

import com.example.delivery.viewmodel.delivery.ShipmentDetailViewModel

import com.example.delivery.viewmodel.delivery.ShipmentDetailState

import com.example.delivery.viewmodel.delivery.ShipmentOperationState

import kotlinx.coroutines.launch

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

        

        try {

            // Utiliser l'API TomTom pour la navigation

            val fullAddress = "$address, $city $zipCode"

            println("🌐 URL TomTom: $fullAddress")

            

            // Check if TomTom app is installed first
            val tomtomPackage = "com.tomtom.speedcams.android.map"
            println("🔍 Checking for TomTom package: $tomtomPackage")
            
            val tomtomInstalled = try {
                val packageInfo = context.packageManager.getPackageInfo(tomtomPackage, 0)
                println("✅ TomTom package found: ${packageInfo.packageName}")
                println("✅ TomTom version: ${packageInfo.versionName}")
                true
            } catch (e: Exception) {
                println("❌ TomTom package NOT found: ${e.message}")
                false
            }

            if (tomtomInstalled) {
                println("✅ TomTom app installed, opening directly")
                // Use TomTom app specific URI scheme
                val tomtomUri = "tomtomgo://navigate?to=$fullAddress"
                val tomtomIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(tomtomUri))
                tomtomIntent.setPackage(tomtomPackage)
                context.startActivity(tomtomIntent)
            } else {
                println("❌ TomTom app not installed")
                android.widget.Toast.makeText(context, "Application TomTom non installée", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            println("❌ Erreur: ${e.message}")
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Erreur lors de l'ouverture de TomTom", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    // Observe screen active/resume lifecycle to automatically reload shipment details when returning from validation or returns pages

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {

        val observer = LifecycleEventObserver { _, event ->

            if (event == Lifecycle.Event.ON_RESUME) {

                println("🔄 NewShipmentDetailScreen ON_RESUME: Reloading shipment details...")

                viewModel.loadShipmentDetails(shipmentId)

            }

        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {

            lifecycleOwner.lifecycle.removeObserver(observer)

        }

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
                                    text = "Détails de la livraison",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = onNavigateBack,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
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
            }
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

    val repository = DirectShipmentDetailRepository()

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

            context.startActivity(intent)

        } catch (e: Exception) {

            // Fallback: ouvrir l'application téléphone

            val intentDial = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {

                data = android.net.Uri.parse("tel:$phoneNumber")

            }

            try {

                context.startActivity(intentDial)

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

                Column(

                    modifier = Modifier.fillMaxWidth()

                ) {

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = Arrangement.SpaceBetween,

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        Column(modifier = Modifier.weight(1f)) {

                            Text(

                                text = shipment.shipmentNo ?: "N/A",

                                fontSize = 20.sp,

                                fontWeight = FontWeight.Bold,

                                color = Color(0xFF1976D2),

                                maxLines = 2,

                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis

                            )

                            Text(

                                text = "ID: ${shipment.id}",

                                fontSize = 14.sp,

                                color = Color.Gray

                            )

                        }

                        

                        // Badge de statut

                        Surface(

                            modifier = Modifier.padding(start = 8.dp),

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

                

                if (!shipment.priority.isNullOrBlank()) {

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

        

        // Photos de preuve de livraison

        if (shipment.deliveryImages.isNotEmpty()) {

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

                        text = "Photos de preuve de livraison (${shipment.deliveryImages.size})",

                        fontSize = 16.sp,

                        fontWeight = FontWeight.Bold,

                        color = Color(0xFF1976D2),

                        modifier = Modifier.padding(bottom = 12.dp)

                    )

                    

                    // Display all delivery images

                    shipment.deliveryImages.forEachIndexed { index, deliveryImage ->

                        println("🔍 Delivery Image $index URL length: ${deliveryImage.url.length}")

                        println("🔍 Delivery Image $index type: ${deliveryImage.documentType}")

                        

                        // Extract base64 data from data URL

                        val imageBase64Data = if (deliveryImage.url.startsWith("data:image")) {

                            deliveryImage.url.substringAfter("base64,")

                        } else {

                            deliveryImage.url

                        }

                        

                        // Decode base64 to bitmap

                        val proofImageBitmap = remember(imageBase64Data) {

                            try {

                                val bytes = android.util.Base64.decode(imageBase64Data, android.util.Base64.DEFAULT)

                                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                                println("📏 Proof image $index dimensions: ${bitmap?.width}x${bitmap?.height}")

                                bitmap?.asImageBitmap()

                            } catch (e: Exception) {

                                println("❌ Error decoding proof image $index: ${e.message}")

                                null

                            }

                        }

                        

                        if (proofImageBitmap != null) {

                            androidx.compose.foundation.Image(

                                bitmap = proofImageBitmap,

                                contentDescription = "Photo de preuve de livraison ${index + 1}",

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

                                    text = "Impossible de charger la photo ${index + 1}",

                                    color = Color.Red,

                                    fontSize = 12.sp

                                )

                            }

                        }

                        

                        if (index < shipment.deliveryImages.size - 1) {

                            Spacer(modifier = Modifier.height(8.dp))

                            HorizontalDivider()

                            Spacer(modifier = Modifier.height(8.dp))

                        }

                    }

                }

            }

        } else if (shipment.deliveryProof != null && !shipment.deliveryProof.imageUrl.isNullOrEmpty()) {

            // Fallback to legacy imageUrl from deliveryProof

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

                    println("🔍 Legacy Image URL length: ${imageUrl.length}")

                    

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

                            println("📏 Legacy proof image dimensions: ${bitmap?.width}x${bitmap?.height}")

                            bitmap?.asImageBitmap()

                        } catch (e: Exception) {

                            println("❌ Error decoding legacy proof image: ${e.message}")

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

            // Bouton Marquer livré (si non complété)

            if (displayStatus != ShipmentDisplayStatus.COMPLETED) {

                Button(

                    onClick = onMarkAsDelivered,

                    modifier = Modifier

                        .fillMaxWidth()

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

