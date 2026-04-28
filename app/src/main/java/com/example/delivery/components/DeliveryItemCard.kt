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







import com.example.delivery.models.DeliveryItem







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







    // Status options: Shipment.status values (TO_PLAN, EXPEDITION, DELIVERED)







    // UI shows French labels but sends DB values directly







    val statusOptions = listOf("TO_PLAN", "EXPEDITION", "DELIVERED")







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



    



    // Calculer la distance des routes entre l'origine (originId) et la destination



    LaunchedEffect(delivery.shipmentId, delivery.originAddress, delivery.originCity, delivery.originPostalCode, 



                   delivery.deliveryAddress, delivery.deliveryCity, delivery.deliveryZipCode,



                   delivery.fullAddress, delivery.locationCity, delivery.locationPostalCode) {



        // Utiliser deliveryAddress si disponible, sinon fullAddress



        val destAddress = delivery.deliveryAddress ?: delivery.fullAddress



        val destCity = delivery.deliveryCity ?: delivery.locationCity



        val destPostalCode = delivery.deliveryZipCode ?: delivery.locationPostalCode



        



        if (delivery.originAddress != null && destAddress != null && !isCalculatingDistance) {



            isCalculatingDistance = true



            val distance = geocodingService.calculateRouteDistanceBetweenAddresses(



                originAddress = delivery.originAddress,



                originCity = delivery.originCity,



                originPostalCode = delivery.originPostalCode,



                destAddress = destAddress,



                destCity = destCity,



                destPostalCode = destPostalCode



            )



            depotDistance = distance



            isCalculatingDistance = false



        }



    }







    







    // Animation pour le clic







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







            .scale(scale)







            .shadow(







                elevation = if (isPressed) 8.dp else 4.dp,







                shape = RoundedCornerShape(20.dp),







                spotColor = statusColor.copy(alpha = 0.3f)







            )







            .clickable {







                isPressed = true







                onItemClick(delivery)







                GlobalScope.launch {







                    delay(100)







                    isPressed = false







                }







            },







        shape = RoundedCornerShape(20.dp),







        colors = CardDefaults.cardColors(







            containerColor = cardBackgroundColor,







            contentColor = MaterialTheme.colorScheme.onSurface







        ),







        border = BorderStroke(







            width = if (isPressed) 2.dp else 1.dp,







            color = statusColor.copy(alpha = if (isPressed) 0.8f else 0.3f)







        )







    ) {







        Column(







            modifier = Modifier







                .fillMaxWidth()







                .padding(20.dp)







        ) {







            // Header with sequence number and status







            Row(







                modifier = Modifier.fillMaxWidth(),







                horizontalArrangement = Arrangement.SpaceBetween,







                verticalAlignment = Alignment.CenterVertically







            ) {







                // Sequence number with enhanced design







                Box(







                    modifier = Modifier







                        .size(40.dp)







                        .shadow(







                            elevation = 4.dp,







                            shape = RoundedCornerShape(12.dp),







                            spotColor = statusColor.copy(alpha = 0.4f)







                        )







                        .background(







                            statusColor,







                            RoundedCornerShape(12.dp)







                        ),







                    contentAlignment = Alignment.Center







                ) {







                    Text(







                        text = delivery.sequence.toString(),







                        style = MaterialTheme.typography.titleMedium,







                        fontWeight = FontWeight.Bold,







                        color = Color.White







                    )







                }







                







                // Enhanced status badge







                Surface(







                    modifier = Modifier







                        .shadow(







                            elevation = 2.dp,







                            shape = RoundedCornerShape(20.dp),







                            spotColor = statusColor.copy(alpha = 0.3f)







                        ),







                    shape = RoundedCornerShape(20.dp),







                    color = statusColor.copy(alpha = 0.1f),







                    border = BorderStroke(







                        width = 1.dp,







                        color = statusColor.copy(alpha = 0.5f)







                    )







                ) {







                    Column(







                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),







                        horizontalAlignment = Alignment.CenterHorizontally







                    ) {







                        Row(







                            verticalAlignment = Alignment.CenterVertically,







                            horizontalArrangement = Arrangement.spacedBy(4.dp)







                        ) {







                            Box(







                                modifier = Modifier







                                    .size(8.dp)







                                    .background(







                                        statusColor,







                                        RoundedCornerShape(4.dp)







                                    )







                            )







                            // Status display with French translation from Shipment.status







                            Text(







                                text = when (delivery.status) {







                                    "DELIVERED" -> "Livrée"







                                    "EXPEDITION" -> "En expédition"







                                    "TO_PLAN" -> "À planifier"







                                    else -> delivery.status







                                },







                                style = MaterialTheme.typography.labelMedium,







                                fontWeight = FontWeight.SemiBold,







                                color = statusColor







                            )







                        }







                        // Raw TripShipmentLink.status (DB value)







                        Text(







                            text = delivery.status,







                            style = MaterialTheme.typography.labelSmall,







                            fontWeight = FontWeight.Medium,







                            color = statusColor.copy(alpha = 0.8f)







                        )







                    }







                }







            }







            







            Spacer(modifier = Modifier.height(16.dp))







            







            // Client name with enhanced typography







            Column {







                Text(







                    text = delivery.clientName ?: "Client non spécifié",







                    style = MaterialTheme.typography.titleLarge,







                    fontWeight = FontWeight.Bold,







                    color = Color(0xFF1A1A1A)







                )







                







                Spacer(modifier = Modifier.height(4.dp))







                







                // Shipment number and type







                Row(







                    horizontalArrangement = Arrangement.spacedBy(8.dp),







                    verticalAlignment = Alignment.CenterVertically







                ) {







                    Text(







                        text = "N°${delivery.shipmentNo}",







                        style = MaterialTheme.typography.bodyMedium,







                        color = Color(0xFF666666),







                        fontWeight = FontWeight.Medium







                    )







                    // Type badge







                    delivery.type?.let { type ->







                        val (typeLabel, typeColor) = when (type) {







                            "OUTBOUND" -> "Sortante" to Color(0xFF4CAF50)







                            "INBOUND" -> "Entrante" to Color(0xFF2196F3)







                            "TRANSFER" -> "Transfert" to Color(0xFFFF9800)







                            else -> type to Color(0xFF9E9E9E)







                        }







                        Surface(







                            shape = RoundedCornerShape(6.dp),







                            color = typeColor.copy(alpha = 0.15f),







                            border = BorderStroke(







                                width = 1.dp,







                                color = typeColor.copy(alpha = 0.5f)







                            )







                        ) {







                            Text(







                                text = typeLabel,







                                style = MaterialTheme.typography.labelSmall,







                                color = typeColor,







                                fontWeight = FontWeight.SemiBold,







                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)







                            )







                        }







                    }







                }







            }







            







            Spacer(modifier = Modifier.height(12.dp))







            







            // Address with enhanced design







            Surface(







                modifier = Modifier.fillMaxWidth(),







                shape = RoundedCornerShape(12.dp),







                color = Color(0xFFF8F9FA),







                border = BorderStroke(







                    width = 1.dp,







                    color = Color(0xFFE9ECEF)







                )







            ) {







                Row(







                    modifier = Modifier







                        .fillMaxWidth()







                        .padding(12.dp),







                    verticalAlignment = Alignment.CenterVertically,







                    horizontalArrangement = Arrangement.spacedBy(8.dp)







                ) {







                    Icon(







                        imageVector = Icons.Default.LocationOn,







                        contentDescription = "Adresse",







                        tint = statusColor,







                        modifier = Modifier.size(20.dp)







                    )







                    Column(







                        modifier = Modifier.weight(1f)







                    ) {







                        Text(







                            text = delivery.fullAddress ?: "Adresse non spécifiée",







                            style = MaterialTheme.typography.bodyMedium,







                            color = Color(0xFF333333),







                            fontWeight = FontWeight.Medium,







                            maxLines = 2,







                            overflow = TextOverflow.Ellipsis







                        )







                        Text(







                            text = "${delivery.locationCity ?: "Ville inconnue"} ${delivery.locationPostalCode ?: "00000"}",







                            style = MaterialTheme.typography.bodySmall,







                            color = Color(0xFF666666)







                        )







                    }







                }







            }







            Spacer(modifier = Modifier.height(12.dp))







            







            // Enhanced delivery info







            Row(







                modifier = Modifier.fillMaxWidth(),







                horizontalArrangement = Arrangement.spacedBy(16.dp)







            ) {







                // Distance and duration







                Row(







                    verticalAlignment = Alignment.CenterVertically,







                    horizontalArrangement = Arrangement.spacedBy(8.dp)







                ) {







                    // Afficher la distance de l'origine si disponible, sinon la distance existante



                    val distanceToShow = depotDistance ?: delivery.distanceKm



                    



                    distanceToShow?.let { distance ->



                        val distanceLabel = if (depotDistance != null) "Origine: ${distance.toInt()} km" else "${distance.toInt()} km"



                        



                        Surface(







                            shape = RoundedCornerShape(8.dp),







                            color = if (depotDistance != null) Color(0xFFE3F2FD) else Color(0xFFF0F0F0),







                            border = BorderStroke(







                                width = 1.dp,







                                color = if (depotDistance != null) Color(0xFF2196F3) else Color(0xFFE0E0E0)







                            )







                        ) {







                            Row(







                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),







                                verticalAlignment = Alignment.CenterVertically,







                                horizontalArrangement = Arrangement.spacedBy(4.dp)







                            ) {







                                Icon(







                                    imageVector = if (depotDistance != null) Icons.Default.LocationOn else Icons.Default.DirectionsCar,







                                    contentDescription = "Distance",







                                    tint = if (depotDistance != null) Color(0xFF2196F3) else Color(0xFF666666),







                                    modifier = Modifier.size(16.dp)







                                )







                                if (isCalculatingDistance) {



                                    CircularProgressIndicator(



                                        modifier = Modifier.size(16.dp),



                                        strokeWidth = 2.dp,



                                        color = Color(0xFF2196F3)



                                    )



                                } else {



                                    Text(







                                        text = distanceLabel,







                                        style = MaterialTheme.typography.bodySmall,







                                        color = if (depotDistance != null) Color(0xFF1976D2) else Color(0xFF666666),







                                        fontWeight = FontWeight.Medium







                                    )



                                }



                            }







                        }



                    }







                    







                    delivery.estimatedDuration?.let { duration ->







                        Surface(







                            shape = RoundedCornerShape(8.dp),







                            color = Color(0xFFF0F0F0),







                            border = BorderStroke(







                                width = 1.dp,







                                color = Color(0xFFE0E0E0)







                            )







                        ) {







                            Row(







                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),







                                verticalAlignment = Alignment.CenterVertically,







                                horizontalArrangement = Arrangement.spacedBy(4.dp)







                            ) {







                                Icon(







                                    imageVector = Icons.Default.Schedule,







                                    contentDescription = "Durée",







                                    tint = Color(0xFF666666),







                                    modifier = Modifier.size(16.dp)







                                )







                                Text(







                                    text = "${duration} min",







                                    style = MaterialTheme.typography.bodySmall,







                                    color = Color(0xFF666666),







                                    fontWeight = FontWeight.Medium







                                )







                            }







                        }







                    }







                }







                







                // Quantity and type







                Column(







                    horizontalAlignment = Alignment.End







                ) {







                    Text(







                        text = "${delivery.quantity} ${delivery.uom}",







                        style = MaterialTheme.typography.bodySmall,







                        color = Color(0xFF666666),







                        fontWeight = FontWeight.Medium







                    )







                }







            }







            







            Spacer(modifier = Modifier.height(12.dp))







            







            // Liste déroulante de statuts







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







                                    







                                    // Appeler le callback pour mettre à jour le statut







                                    onStatusChange(delivery, status)







                                    







                                    // Simuler la fin de la mise à jour (sera géré par le ViewModel)







                                    scope.launch {







                                        delay(2000) // Timeout de sécurité







                                        isUpdatingStatus = false







                                    }







                                }







                            },







                            enabled = !isUpdatingStatus







                        )







                    }







                }







            }







            







            Spacer(modifier = Modifier.height(12.dp))







            







            // Action buttons with enhanced design and consistent sizing







            Column(







                modifier = Modifier.fillMaxWidth(),







                verticalArrangement = Arrangement.spacedBy(DesignSystem.Sizes.SPACING_MEDIUM)







            ) {







                // Première rangée: Itinéraire, Validation, Terminer







                Row(







                    modifier = Modifier.fillMaxWidth(),







                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Sizes.SPACING_SMALL)







                ) {







                    // Navigate button - Web navigation with TomTom







                    Button(







                        onClick = {







                            println("🗺️ Bouton Itinéraire cliqué - Navigation web TomTom!")







                            openTomTomWebNavigation(context, delivery)







                        },







                        modifier = Modifier







                            .weight(0.9f)







                            .height(DesignSystem.Sizes.BUTTON_HEIGHT_MEDIUM),







                        shape = RoundedCornerShape(DesignSystem.Components.BUTTON_CORNER_RADIUS),







                        colors = ButtonDefaults.buttonColors(







                            containerColor = DesignSystem.Colors.NAVIGATION_GREEN,







                            contentColor = DesignSystem.Colors.SURFACE_WHITE







                        ),







                        elevation = ButtonDefaults.buttonElevation(







                            defaultElevation = 6.dp,







                            pressedElevation = 8.dp







                        ),







                        border = BorderStroke(







                            width = 1.dp,







                            color = DesignSystem.Colors.SURFACE_WHITE.copy(alpha = 0.3f)







                        )







                    ) {







                        Row(







                            modifier = Modifier.padding(horizontal = DesignSystem.Sizes.SPACING_MINI),







                            horizontalArrangement = Arrangement.Center,







                            verticalAlignment = Alignment.CenterVertically







                        ) {







                            Icon(







                                imageVector = Icons.Default.Navigation,







                                contentDescription = "Navigation",







                                modifier = Modifier.size(DesignSystem.Sizes.ICON_SIZE_MEDIUM)







                            )







                            Spacer(modifier = Modifier.width(DesignSystem.Sizes.SPACING_MINI))







                            Text(







                                text = "Itinéraire",







                                style = MaterialTheme.typography.labelMedium,







                                fontWeight = FontWeight.Bold







                            )







                        }







                    }







                    







                    // Validation button - Primary blue (uniquement si "DELIVERED" est sélectionné)







                    println("🔍 DEBUG: selectedStatus = '$selectedStatus', podDone = ${delivery.podDone}")







                    if (selectedStatus == "DELIVERED") {







                        Button(







                            onClick = { 







                                println("✅ Bouton Validation cliqué!")







                                onValidationClick(delivery)







                            },







                            modifier = Modifier







                                .weight(1.2f)  // Plus d'espace pour le texte plus long







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







                                contentDescription = "Validation",







                                modifier = Modifier.size(DesignSystem.Sizes.ICON_SIZE_MEDIUM)







                            )







                            Spacer(modifier = Modifier.width(DesignSystem.Sizes.SPACING_MINI))







                            Text(







                                text = "Validation",







                                style = MaterialTheme.typography.labelMedium,







                                fontWeight = FontWeight.SemiBold







                            )







                        }







                    }







                    







                    // Complete button - Success green with disabled state







                    Button(







                        onClick = { onCompleteClick(delivery) },







                        modifier = Modifier







                            .weight(0.9f)  // Moins d'espace, texte plus court







                            .height(DesignSystem.Sizes.BUTTON_HEIGHT_MEDIUM),







                        shape = RoundedCornerShape(DesignSystem.Components.BUTTON_CORNER_RADIUS),







                        colors = ButtonDefaults.buttonColors(







                            containerColor = if (delivery.podDone) DesignSystem.Colors.DISABLED_GRAY else DesignSystem.Colors.SUCCESS_GREEN,







                            contentColor = DesignSystem.Colors.SURFACE_WHITE







                        ),







                        enabled = !delivery.podDone,







                        elevation = ButtonDefaults.buttonElevation(







                            defaultElevation = if (delivery.podDone) DesignSystem.Sizes.ELEVATION_NONE else DesignSystem.Components.BUTTON_ELEVATION,







                            pressedElevation = if (delivery.podDone) DesignSystem.Sizes.ELEVATION_NONE else DesignSystem.Components.BUTTON_ELEVATION_PRESSED







                        )







                    ) {







                        Icon(







                            imageVector = if (delivery.podDone) Icons.Default.CheckCircle else Icons.Default.Check,







                            contentDescription = "Compléter",







                            modifier = Modifier.size(DesignSystem.Sizes.ICON_SIZE_MEDIUM)







                        )







                        Spacer(modifier = Modifier.width(DesignSystem.Sizes.SPACING_MINI))







                        Text(







                            text = if (delivery.podDone) "Terminé" else "Terminer",







                            style = MaterialTheme.typography.labelMedium,







                            fontWeight = FontWeight.SemiBold







                        )







                    }







                }







                







                // Deuxième rangée: Appeler (si numéro disponible) - Full width with accent color







                val phoneNumber = delivery.clientPhone







                if (phoneNumber != null) {







                    Button(







                        onClick = { 







                            println("Bouton Appeler cliqué!")







                            onCallClick(delivery)







                        },







                        modifier = Modifier







                            .fillMaxWidth()







                            .height(DesignSystem.Sizes.BUTTON_HEIGHT_MEDIUM),







                        shape = RoundedCornerShape(DesignSystem.Components.BUTTON_CORNER_RADIUS),







                        colors = ButtonDefaults.buttonColors(







                            containerColor = DesignSystem.Colors.CALL_GREEN,







                            contentColor = DesignSystem.Colors.SURFACE_WHITE







                        ),







                        elevation = ButtonDefaults.buttonElevation(







                            defaultElevation = DesignSystem.Components.BUTTON_ELEVATION,







                            pressedElevation = DesignSystem.Components.BUTTON_ELEVATION_PRESSED







                        )







                    ) {







                        Row(







                            modifier = Modifier.padding(vertical = DesignSystem.Sizes.SPACING_SMALL),







                            horizontalArrangement = Arrangement.Center,







                            verticalAlignment = Alignment.CenterVertically







                        ) {







                            Icon(







                                imageVector = Icons.Default.Call,







                                contentDescription = "Appeler",







                                modifier = Modifier.size(DesignSystem.Sizes.ICON_SIZE_LARGE)







                            )







                            Spacer(modifier = Modifier.width(DesignSystem.Sizes.SPACING_SMALL))







                            Text(







                                text = "Appeler",







                                style = MaterialTheme.typography.labelMedium,







                                fontWeight = FontWeight.SemiBold







                            )







                        }







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















// Web navigation functions



private fun openTomTomWebNavigation(context: android.content.Context, delivery: DeliveryItem) {
    val geocodingService = TomTomGeocodingService()
    
    println("🔍 DEBUG: deliveryAddress = ${delivery.deliveryAddress}, deliveryCity = ${delivery.deliveryCity}")
    
    // TOUJOURS utiliser le geocoding (pas de latitude/longitude dans la base de données)
    println("⚠️ Using geocoding for: ${delivery.deliveryAddress}, ${delivery.deliveryCity}")
    
    // Utiliser le geocoding de manière synchrone avec GlobalScope
    GlobalScope.launch {
        val geocodingResult = geocodingService.geocodeAddress(
            address = delivery.deliveryAddress,
            city = delivery.deliveryCity,
            postalCode = delivery.deliveryZipCode,
            country = delivery.deliveryCountry ?: "France"
        )
        
        if (geocodingResult != null) {
            println("✅ Geocoding successful: ${geocodingResult.latitude}, ${geocodingResult.longitude}")
            openTomTomNavigationWithLocation(
                context,
                48.8566, 2.3522, // Default start location (Paris)
                geocodingResult.latitude,
                geocodingResult.longitude
            )
        } else {
            println("❌ Geocoding failed for: ${delivery.deliveryAddress}, ${delivery.deliveryCity}")
            android.widget.Toast.makeText(
                context,
                "Impossible de géocoder l'adresse: ${delivery.deliveryAddress}",
                android.widget.Toast.LENGTH_LONG
            ).show()
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







        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))







        val packageManager = context.packageManager







        val activities = packageManager.queryIntentActivities(intent, 0)















        if (activities.isNotEmpty()) {







            context.startActivity(intent)







            println("✅ TomTom route planner opened successfully")







        } else {







            println("⚠️ No browser found, trying Google Maps fallback")







            openGoogleMapsFallbackWithRoute(context, startLat, startLon, destLat, destLon)







        }







    } catch (e: Exception) {







        println("❌ Failed to open TomTom route planner: ${e.message}")







        openGoogleMapsFallbackWithRoute(context, startLat, startLon, destLat, destLon)







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







