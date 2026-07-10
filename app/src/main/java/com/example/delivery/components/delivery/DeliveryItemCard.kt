package com.example.delivery.components



import androidx.compose.animation.core.*

import androidx.compose.animation.core.animateFloatAsState

import androidx.compose.animation.core.spring

import androidx.compose.animation.core.Spring

import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy

import androidx.compose.foundation.background

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.horizontalScroll

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip

import androidx.compose.ui.draw.scale

import androidx.compose.ui.draw.shadow

import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import androidx.core.content.ContextCompat

import android.content.Intent

import android.net.Uri

import android.location.Location

import android.Manifest

import android.content.pm.PackageManager

import androidx.core.app.ActivityCompat

import com.google.android.gms.location.FusedLocationProviderClient

import com.google.android.gms.location.LocationServices

import com.google.android.gms.location.LocationRequest

import com.google.android.gms.location.LocationCallback

import com.google.android.gms.location.LocationResult

import com.google.android.gms.location.Priority

import com.example.delivery.models.delivery.DeliveryItem

import com.example.delivery.ui.DesignSystem

import com.example.delivery.services.TomTomGeocodingService

import kotlinx.coroutines.delay

import kotlinx.coroutines.GlobalScope

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)



@Composable



fun DeliveryItemCard(



    delivery: DeliveryItem,



    modifier: Modifier = Modifier,



    onItemClick: (DeliveryItem) -> Unit = {},



    onCompleteClick: (DeliveryItem) -> Unit = {},



    onNavigateClick: (DeliveryItem) -> Unit = {},



    onValidationClick: (DeliveryItem) -> Unit = {},



    onCallClick: (DeliveryItem) -> Unit = {},



    onStatusChange: (DeliveryItem, String) -> Unit = { _, _ -> },



    onReturnsClick: (DeliveryItem) -> Unit = {}



) {



    val context = LocalContext.current



    val scope = rememberCoroutineScope()



    var isPressed by remember { mutableStateOf(false) }



    // États pour la liste déroulante de statuts - Shipment.status values



    var expandedStatus by remember { mutableStateOf(false) }



    // Status options: Shipment.status values (EXPEDITION, DELIVERED) - TO_PLAN hidden from UI

    // UI shows French labels but sends DB values directly



    val statusOptions = listOf("EXPEDITION", "DELIVERED")



    var selectedStatus by remember { mutableStateOf(



        when (delivery.status) {



            "DELIVERED" -> "DELIVERED"



            "EXPEDITION" -> "EXPEDITION"



            "TO_PLAN" -> "TO_PLAN"



            else -> "TO_PLAN" // Default to TO_PLAN for any unknown status



        }



    ) }



    // Log pour déboguer



    println("🔍 DEBUG: delivery.podDone = ${delivery.podDone}, delivery.status = '${delivery.status}', selectedStatus = '$selectedStatus'")



    // État de chargement pour la mise à jour du statut



    var isUpdatingStatus by remember { mutableStateOf(false) }



    // Service de géocodage



    val geocodingService = remember { TomTomGeocodingService() }



    // État pour la distance calculée - unique pour chaque carte via shipmentId



    var depotDistance by remember(delivery.shipmentId) { mutableStateOf<Double?>(null) }



    var isCalculatingDistance by remember(delivery.shipmentId) { mutableStateOf(false) }



    // 🎯 CALCUL DE DISTANCE UNIFIÉE - Utilisé PARTOUT pour cohérence



    LaunchedEffect(delivery.shipmentId, delivery.originAddress, delivery.originCity, delivery.originPostalCode, 



                   delivery.deliveryAddress, delivery.deliveryCity, delivery.deliveryZipCode,



                   delivery.fullAddress, delivery.locationCity, delivery.locationPostalCode) {



        // Utiliser deliveryAddress si disponible, sinon fullAddress



        val destAddress = delivery.deliveryAddress ?: delivery.fullAddress



        val destCity = delivery.deliveryCity ?: delivery.locationCity



        val destPostalCode = delivery.deliveryZipCode ?: delivery.locationPostalCode



        if (delivery.originAddress != null && destAddress != null && !isCalculatingDistance) {



            isCalculatingDistance = true



            scope.launch {



                try {



                    // 🎯 UTILISER LE DISTANCE MANAGER UNIFIÉ



                    val distance = com.example.delivery.services.DistanceManager.calculateDeliveryDistance(



                        originAddress = delivery.originAddress,



                        originCity = delivery.originCity,



                        originPostalCode = delivery.originPostalCode,



                        deliveryAddress = destAddress,



                        deliveryCity = destCity,



                        deliveryZipCode = destPostalCode



                    )



                    // 🎯 DISTANCE UNIFIÉE - Stockée et utilisée PARTOUT



                    depotDistance = distance



                    // Log pour debug et cohérence



                    println("📍 DISTANCE UNIFIÉE: ${distance?.toInt()} km pour livraison ${delivery.shipmentNo}")



                    println("🔍 ${delivery.originAddress} ${delivery.originCity} → ${destAddress} ${destCity}")



                    println("📊 Cache: ${com.example.delivery.services.DistanceManager.getCacheStats()}")



                } catch (e: Exception) {



                    println("❌ Erreur calcul distance unifiée: ${e.message}")



                } finally {



                    isCalculatingDistance = false



                }



            }



        }



    }



    val scale by animateFloatAsState(



        targetValue = if (isPressed) 0.98f else 1f,



        animationSpec = spring(



   dampingRatio = Spring.DampingRatioMediumBouncy,



            stiffness = Spring.StiffnessLow



        ), label = ""



    )



    // Couleurs basées sur le statut avec palette cohérente



    val statusColor = DesignSystem.Utils.getStatusColor(delivery.podDone, delivery.status)



    // Fond de carte toujours blanc



    val cardBackgroundColor = DesignSystem.Colors.SURFACE_WHITE



    Card(



        modifier = modifier



            .fillMaxWidth()



            .padding(horizontal = 16.dp)



            .scale(scale)



            .shadow(



                elevation = if (isPressed) 8.dp else 4.dp,



                shape = RoundedCornerShape(24.dp),



                spotColor = Color(0xFF102A43).copy(alpha = 0.1f)



            )



            .clickable {



                isPressed = true



                onItemClick(delivery)



                GlobalScope.launch {



                    delay(100)



                    isPressed = false



                }



            },



        shape = RoundedCornerShape(24.dp),



        colors = CardDefaults.cardColors(



            containerColor = cardBackgroundColor,



            contentColor = MaterialTheme.colorScheme.onSurface



        ),



        border = BorderStroke(



            width = 1.dp,



            color = Color(0xFFE2E8F0)



        )



    ) {



        Column(



            modifier = Modifier



                .fillMaxWidth()



                .padding(24.dp)



        ) {



            // Header with shipment number and premium status badge



            Row(



                modifier = Modifier.fillMaxWidth(),



                horizontalArrangement = Arrangement.SpaceBetween,



                verticalAlignment = Alignment.CenterVertically



            ) {



                // Shipment number in deep navy bold



                Column {



                    Text(



                        text = "N°${delivery.shipmentNo}",



                        style = MaterialTheme.typography.titleLarge,



                        fontWeight = FontWeight.Bold,



                        color = Color(0xFF102A43)



                    )



                    // Origin address subdued in medium-gray



                    if (delivery.originAddress != null) {



                        Text(



                            text = "Origine: ${delivery.originCity ?: "Ville inconnue"}",



                            style = MaterialTheme.typography.bodySmall,



                            color = Color(0xFF64748B),



                            fontWeight = FontWeight.Medium



                        )



                    }



                }



                



                // Premium pill badge with pastel background



                val (badgeBg, badgeText) = when (delivery.status) {



                    "TO_PLAN" -> Color(0xFFFFF7E0) to Color(0xFFB06000)



                    "EXPEDITION" -> Color(0xFFE8F0FE) to Color(0xFF1A73E8)



                    "DELIVERED" -> Color(0xFFE6F4EA) to Color(0xFF137333)



                    else -> Color(0xFFF1F3F4) to Color(0xFF5F6368)



                }



                Surface(



                    shape = CircleShape,

                    color = badgeBg,

                    border = BorderStroke(1.dp, badgeText.copy(alpha = 0.3f))

                ) {



                    Text(



                        text = when (delivery.status) {



                            "DELIVERED" -> "Livrée"



                            "EXPEDITION" -> "En cours"



                            "TO_PLAN" -> "À planifier"



                            else -> delivery.status



                        },



                        style = MaterialTheme.typography.labelMedium,



                        fontWeight = FontWeight.Bold,



                        color = badgeText,



                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)



                    )



                }



            }



            Spacer(modifier = Modifier.height(16.dp))



            // Destination address in prominent deep navy bold



            Column {



                Text(



                    text = delivery.fullAddress ?: delivery.deliveryAddress ?: "Adresse non spécifiée",



                    style = MaterialTheme.typography.titleMedium,



                    fontWeight = FontWeight.Bold,



                    color = Color(0xFF102A43),



                    maxLines = 2,



                    overflow = TextOverflow.Ellipsis



                )



                Spacer(modifier = Modifier.height(4.dp))



                Text(



                    text = "${delivery.deliveryCity ?: "Ville inconnue"} ${delivery.deliveryZipCode ?: "00000"}",



                    style = MaterialTheme.typography.bodyMedium,



                    color = Color(0xFF64748B),



                    fontWeight = FontWeight.Medium



                )



            }



            Spacer(modifier = Modifier.height(12.dp))



            // Client name and type info



            Row(



                modifier = Modifier.fillMaxWidth(),



                horizontalArrangement = Arrangement.SpaceBetween,



                verticalAlignment = Alignment.CenterVertically



            ) {



                Text(



                    text = delivery.clientName ?: "Client non spécifié",



                    style = MaterialTheme.typography.bodyLarge,



                    fontWeight = FontWeight.Bold,



                    color = Color(0xFF102A43)



                )



                // Type badge



                delivery.type?.let { type ->



                    val (typeLabel, typeColor) = when (type) {



                        "OUTBOUND" -> "Sortant" to Color(0xFF4CAF50)



                        "INBOUND" -> "Entrante" to Color(0xFF2196F3)



                        "TRANSFER" -> "Transfert" to Color(0xFFFF9800)



                        else -> type to Color(0xFF9E9E9E)



                    }



                    Surface(



                        shape = RoundedCornerShape(12.dp),



                        color = typeColor.copy(alpha = 0.15f),



                        border = BorderStroke(



                            width = 1.dp,



                            color = typeColor.copy(alpha = 0.5f)



                        )



                    ) {



                        Text(



                            text = typeLabel,



                            style = MaterialTheme.typography.labelMedium,



                            color = typeColor,



                            fontWeight = FontWeight.Bold,



                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)



                        )



                    }



                }



            }



            Spacer(modifier = Modifier.height(12.dp))



            // Distance and duration info



            Row(



                modifier = Modifier.fillMaxWidth(),



                horizontalArrangement = Arrangement.spacedBy(12.dp)



            ) {



                val distanceToShow = depotDistance ?: delivery.distanceKm



                distanceToShow?.let { distance ->



                    Surface(



                        shape = RoundedCornerShape(12.dp),



                        color = Color(0xFFF1F5F9),



                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))



                    ) {



                        Row(



                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),



                            verticalAlignment = Alignment.CenterVertically,



                            horizontalArrangement = Arrangement.spacedBy(6.dp)



                        ) {



                            Icon(



                                imageVector = Icons.Default.DirectionsCar,



                                contentDescription = "Distance",



                                tint = Color(0xFF102A43),



                                modifier = Modifier.size(16.dp)



                            )



                            if (isCalculatingDistance) {



                                CircularProgressIndicator(



                                    modifier = Modifier.size(16.dp),



                                    strokeWidth = 2.dp,



                                    color = Color(0xFF1976D2)



                                )



                            } else {



                                Text(



                                    text = "${distance.toInt()} km",



                                    style = MaterialTheme.typography.bodyMedium,



                                    color = Color(0xFF102A43),



                                    fontWeight = FontWeight.Bold



                                )



                            }



                        }



                    }



                }



                delivery.estimatedDuration?.let { duration ->



                    Surface(



                        shape = RoundedCornerShape(12.dp),



                        color = Color(0xFFF1F5F9),



                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))



                    ) {



                        Row(



                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),



                            verticalAlignment = Alignment.CenterVertically,



                            horizontalArrangement = Arrangement.spacedBy(6.dp)



                        ) {



                            Icon(



                                imageVector = Icons.Default.Schedule,



                                contentDescription = "Durée",



                                tint = Color(0xFF102A43),



                                modifier = Modifier.size(16.dp)



                            )



                            Text(



                                text = "${duration} min",



                                style = MaterialTheme.typography.bodyMedium,



                                color = Color(0xFF102A43),



                                fontWeight = FontWeight.Bold



                            )



                        }



                    }



                }



            }



            Spacer(modifier = Modifier.height(16.dp))



            // Status dropdown with premium design



            ExposedDropdownMenuBox(



                expanded = expandedStatus,



                onExpandedChange = { expandedStatus = it },



                modifier = Modifier.fillMaxWidth()



            ) {



                OutlinedTextField(



                    value = when (selectedStatus) {



                        "TO_PLAN" -> "À planifier"



                        "EXPEDITION" -> "En expédition"



                        "DELIVERED" -> "Livrée"



                        else -> selectedStatus



                    },



                    onValueChange = { },



                    readOnly = true,



                    label = { Text("Statut") },



                    trailingIcon = { 



                        Row(



                            verticalAlignment = Alignment.CenterVertically,



                            horizontalArrangement = Arrangement.spacedBy(4.dp)



                        ) {



                            if (isUpdatingStatus) {



                                CircularProgressIndicator(



                                    modifier = Modifier.size(16.dp),



                                    strokeWidth = 2.dp,



                                    color = MaterialTheme.colorScheme.primary



                                )



                            } else {



                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)



                            }



                        }



                    },



                    enabled = !isUpdatingStatus,



                    modifier = Modifier



                        .fillMaxWidth()



                        .menuAnchor(



                            MenuAnchorType.PrimaryNotEditable,



                            enabled = !isUpdatingStatus



                        ),



                    shape = RoundedCornerShape(12.dp),



                    colors = OutlinedTextFieldDefaults.colors(



                        focusedBorderColor = Color(0xFF1976D2),



                        unfocusedBorderColor = Color(0xFFE2E8F0)



                    )



                )



                DropdownMenu(



                    expanded = expandedStatus,



                    onDismissRequest = { expandedStatus = false },



                    modifier = Modifier.exposedDropdownSize(),



                    containerColor = Color.White



                ) {



                    statusOptions.forEach { status ->



                        DropdownMenuItem(



                            text = { 



                                Row(



                                    verticalAlignment = Alignment.CenterVertically,



                                    horizontalArrangement = Arrangement.spacedBy(8.dp)



                                ) {



                                    if (isUpdatingStatus && selectedStatus == status) {



                                        CircularProgressIndicator(



                                            modifier = Modifier.size(16.dp),



                                            strokeWidth = 2.dp,



                                            color = MaterialTheme.colorScheme.primary



                                        )



                                    }



                                    Text(



                                        when (status) {



                                            "TO_PLAN" -> "À planifier"



                                            "EXPEDITION" -> "En expédition"



                                            "DELIVERED" -> "Livrée"



                                            else -> status



                                        }



                                    )



                                }



                            },



                            onClick = {



                                if (!isUpdatingStatus && status != selectedStatus) {



                                    isUpdatingStatus = true



                                    selectedStatus = status



                                    expandedStatus = false



                                    println("🔄 Changement de statut: ${delivery.shipmentNo} -> $status")



                                    onStatusChange(delivery, status)



                                    scope.launch {



                                        delay(2000)



                                        isUpdatingStatus = false



                                    }



                                }



                            },



                            enabled = !isUpdatingStatus



                        )



                    }



                }



            }



            Spacer(modifier = Modifier.height(16.dp))



            // Action Dock - Text buttons with icons for better readability

            // Disposition en 2 lignes pour éviter que le texte soit coupé



            Column(

                modifier = Modifier.fillMaxWidth(),

                verticalArrangement = Arrangement.spacedBy(8.dp)

            ) {

                // Première ligne : Appeler, Naviguer et Retours

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.spacedBy(8.dp)

                ) {

                    // Call - Soft green text button

                    delivery.clientPhone?.let {

                        TextButton(

                            onClick = {

                                println("📞 Appel client!")

                                onCallClick(delivery)

                            },

                            modifier = Modifier.weight(1f),

                            colors = ButtonDefaults.textButtonColors(

                                contentColor = Color(0xFF4CAF50)

                            )

                        ) {

                            Icon(

                                imageVector = Icons.Default.Call,

                                contentDescription = "Appeler",

                                tint = Color(0xFF4CAF50),

                                modifier = Modifier.size(18.dp)

                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(

                                text = "Appeler",

                                style = MaterialTheme.typography.labelLarge,

                                fontWeight = FontWeight.Bold

                            )

                        }

                    }



                    // Navigation - Primary brand blue text button

                    TextButton(

                        onClick = {

                            println("🧭 Navigation TomTom!")

                            openTomTomWebNavigation(context, delivery)

                        },

                        modifier = Modifier.weight(1f),

                        colors = ButtonDefaults.textButtonColors(

                            contentColor = Color(0xFF1976D2)

                        )

                    ) {

                        Icon(

                            imageVector = Icons.Default.Navigation,

                            contentDescription = "Naviguer",

                            tint = Color(0xFF1976D2),

                            modifier = Modifier.size(18.dp)

                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(

                            text = "Naviguer",

                            style = MaterialTheme.typography.labelLarge,

                            fontWeight = FontWeight.Bold

                        )

                    }



                    // Returns - Icon button for secondary action

                    IconButton(

                        onClick = {

                            println("📦 Retours!")

                            onReturnsClick(delivery)

                        },

                        modifier = Modifier.size(48.dp)

                    ) {

                        Icon(

                            imageVector = Icons.Default.Inventory2,

                            contentDescription = "Retours",

                            tint = Color(0xFFFF9800),

                            modifier = Modifier.size(24.dp)

                        )

                    }

                }



                // Deuxième ligne : Valider (seul, prend toute la largeur)

                if (selectedStatus == "DELIVERED") {

                    Button(

                        onClick = {

                            println("✅ Validation!")

                            onValidationClick(delivery)

                        },

                        modifier = Modifier.fillMaxWidth(),

                        colors = ButtonDefaults.buttonColors(

                            containerColor = Color(0xFF10B981),

                            contentColor = Color.White

                        ),

                        shape = RoundedCornerShape(12.dp)

                    ) {

                        Icon(

                            imageVector = Icons.Default.Verified,

                            contentDescription = "Valider",

                            tint = Color.White,

                            modifier = Modifier.size(18.dp)

                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(

                            text = "Valider",

                            style = MaterialTheme.typography.labelLarge,

                            fontWeight = FontWeight.Bold

                        )

                    }

                }

            }



        }



    }



}



@Composable



private fun StatusBadge(



    status: String,



    isCompleted: Boolean



) {



    val backgroundColor = DesignSystem.Utils.getStatusColor(isCompleted, status)



    Box(



        modifier = Modifier



            .background(



                backgroundColor,



                RoundedCornerShape(DesignSystem.Components.CHIP_CORNER_RADIUS)



            )



            .padding(



                horizontal = DesignSystem.Components.CHIP_HORIZONTAL_PADDING,



                vertical = DesignSystem.Components.CHIP_VERTICAL_PADDING



            )



    ) {



        Text(



            text = when {



                status == "DELIVERED" -> "Livrée"



                status == "EXPEDITION" -> "En expédition"



                status == "TO_PLAN" -> "À planifier"



                else -> status



            },



            style = MaterialTheme.typography.bodySmall,



            fontWeight = FontWeight.SemiBold,



            color = DesignSystem.Colors.SURFACE_WHITE



        )



    }



}



@Composable



private fun InfoItem(



    icon: ImageVector,



    text: String,



    modifier: Modifier = Modifier



) {



    Row(



        modifier = modifier,



        verticalAlignment = Alignment.CenterVertically,



        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Sizes.SPACING_MINI)



    ) {



        Icon(



            imageVector = icon,



            contentDescription = text,



            tint = DesignSystem.Colors.TEXT_SECONDARY,



            modifier = Modifier.size(DesignSystem.Sizes.ICON_SIZE_SMALL)



        )



        Text(



            text = text,



            style = MaterialTheme.typography.bodySmall,



            color = DesignSystem.Colors.TEXT_SECONDARY



        )



    }



}



private fun getStatusColor(delivery: DeliveryItem): Color {



    return DesignSystem.Utils.getStatusColor(delivery.podDone, delivery.status)



}



private fun formatAddress(address: String?, city: String?, postalCode: String?): String {



    val parts = listOfNotNull(



        address,



        postalCode,



        city



    )



    return parts.joinToString(", ")



}



// Ouvre directement l'application TomTom avec l'itinéraire vers la destination du shipment
private fun openTomTomWebNavigation(context: android.content.Context, delivery: DeliveryItem) {
    val geocodingService = TomTomGeocodingService()

    // Adresse de destination
    val destAddress = delivery.deliveryAddress ?: delivery.fullAddress
    val destCity    = delivery.deliveryCity    ?: delivery.locationCity
    val destZipCode = delivery.deliveryZipCode ?: delivery.locationPostalCode

    if (destAddress.isNullOrBlank() || destCity.isNullOrBlank()) {
        android.widget.Toast.makeText(context, "Adresse de destination manquante", android.widget.Toast.LENGTH_SHORT).show()
        return
    }

    // Trouver l'app TomTom installée
    val pm = context.packageManager
    val tomtomPackages = listOf(
        "com.tomtom.speedcams.android.map",
        "com.tomtom.navigation.android",
        "com.tomtom.go",
        "com.tomtom.speedcams"
    )
    val tomtomPkg = tomtomPackages.firstOrNull { pkg ->
        try { pm.getPackageInfo(pkg, 0); true } catch (e: Exception) { false }
    } ?: pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
            .firstOrNull { it.packageName.lowercase().contains("tomtom") }?.packageName

    if (tomtomPkg == null) {
        android.widget.Toast.makeText(context, "❌ Application TomTom non installée", android.widget.Toast.LENGTH_LONG).show()
        return
    }

    println("✅ TomTom trouvé: $tomtomPkg")
    android.widget.Toast.makeText(context, "🧭 Ouverture de TomTom...", android.widget.Toast.LENGTH_SHORT).show()

    // Géocoder la destination puis ouvrir TomTom
    kotlinx.coroutines.GlobalScope.launch {
        try {
            val result = geocodingService.geocodeAddress(
                address    = destAddress,
                city       = destCity,
                postalCode = destZipCode,
                country    = delivery.deliveryCountry ?: "France"
            )

            android.os.Handler(android.os.Looper.getMainLooper()).post {
                if (result != null) {
                    val lat = String.format(java.util.Locale.US, "%.6f", result.latitude)
                    val lon = String.format(java.util.Locale.US, "%.6f", result.longitude)

                    // geo URI → TomTom l'intercepte et ouvre l'itinéraire depuis la position actuelle
                    val geoUri = android.net.Uri.parse("geo:$lat,$lon?q=$lat,$lon($destCity)")
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, geoUri)
                    intent.setPackage(tomtomPkg)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK

                    println("🗺️ Ouverture TomTom: $tomtomPkg → geo:$lat,$lon")
                    try {
                        context.startActivity(intent)
                        android.widget.Toast.makeText(context, "🧭 TomTom → $destCity", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // Si geo URI échoue, lancer TomTom directement
                        println("⚠️ geo URI échoué, lancement direct TomTom: ${e.message}")
                        val launchIntent = pm.getLaunchIntentForPackage(tomtomPkg)
                        if (launchIntent != null) {
                            launchIntent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(launchIntent)
                            android.widget.Toast.makeText(context,
                                "TomTom ouvert — destination: $destCity",
                                android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    // Géocodage échoué : ouvrir TomTom directement sans coordonnées
                    println("⚠️ Géocodage échoué, lancement direct TomTom")
                    val launchIntent = pm.getLaunchIntentForPackage(tomtomPkg)
                    if (launchIntent != null) {
                        launchIntent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(launchIntent)
                        android.widget.Toast.makeText(context,
                            "TomTom ouvert — saisissez: $destCity",
                            android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        android.widget.Toast.makeText(context,
                            "Impossible d'ouvrir TomTom",
                            android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Erreur: ${e.message}")
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(context, "Erreur: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}



private fun openTomTomNavigationWithLocation(context: android.content.Context, startLat: Double, startLon: Double, destLat: Double, destLon: Double) {



    val apiKey = "c92wOsiK2ds07Gzq9ZJXNRyyWeQhSYse"



    // TomTom route URL format with destination in sorted parameter



    // Format: key, p (position), r (route with sorted waypoints), to (destination)



    val url = "https://plan.tomtom.com/en/route/plan?key=$apiKey&p=$startLat,$startLon,12z&r=(costModel:FASTEST,routingProvider:GLOBAL,sorted:(h~V${startLat}~J${startLon}~Vaddr~E_Driver,h~V${destLat}~J${destLon}~Vaddr~E_Client),travelMode:CAR,vehicleParameters:(axleWeight:-+,height:-+,length:-+,maxSpeed:-+,vehicleModelId:-+,weight:-+,width:-+))&to=$destLat,$destLon"



    println("🌐 Opening TomTom route planner:")



    println("📍 From (Driver): $startLat, $startLon")



    println("📍 To (Client): $destLat, $destLon")



    println("🔗 URL: $url")



    try {
        // Check if TomTom app is installed first
        val tomtomPackage = "com.tomtom.speedcams.android.map"
        println("🔍 DELIVERY CARD: Checking for TomTom package: $tomtomPackage")
        
        val tomtomInstalled = try {
            val packageInfo = context.packageManager.getPackageInfo(tomtomPackage, 0)
            println("✅ DELIVERY CARD: TomTom package found: ${packageInfo.packageName}")
            println("✅ DELIVERY CARD: TomTom version: ${packageInfo.versionName}")
            true
        } catch (e: Exception) {
            println("❌ DELIVERY CARD: TomTom package NOT found: ${e.message}")
            false
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        if (tomtomInstalled) {
            println("✅ DELIVERY CARD: TomTom app installed, opening directly")
            intent.setPackage(tomtomPackage)
            println("🎯 DELIVERY CARD: Intent package set to: $tomtomPackage")
            context.startActivity(intent)
            println("✅ DELIVERY CARD: TomTom route planner opened successfully")
        } else {
            println("❌ DELIVERY CARD: TomTom app not installed")
            // Show Toast on main thread
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(
                    context,
                    "Application TomTom non installée",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

    } catch (e: Exception) {
        println("❌ Failed to open TomTom route planner: ${e.message}")
    }

}



private fun openGoogleMapsFallbackWithRoute(context: android.content.Context, startLat: Double, startLon: Double, destLat: Double, destLon: Double) {



    val url = "https://www.google.com/maps/dir/?api=1&origin=$startLat,$startLon&destination=$destLat,$destLon&travelmode=driving"



    println("🔄 Opening Google Maps fallback with route: $url")



    try {



        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))



        context.startActivity(intent)



        println("✅ Google Maps opened successfully")



    } catch (e: Exception) {



        println("❌ Failed to open Google Maps: ${e.message}")



    }



}



private fun openGoogleMapsFallback(context: android.content.Context, lat: Double, lon: Double) {



    val url = "https://maps.google.com/maps?q=$lat,$lon"



    println("🔄 Opening Google Maps fallback: $url")



    try {



        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))



        context.startActivity(intent)



        println("✅ Google Maps opened successfully")



    } catch (e: Exception) {



        println("❌ Failed to open Google Maps: ${e.message}")



    }



}

