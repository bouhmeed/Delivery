package com.example.delivery.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.delivery.network.ApiClient
import com.example.delivery.network.DeliveryValidationApiService
import com.example.delivery.network.DeliveryValidationRequest
import com.example.delivery.repository.DirectShipmentDetailRepository
import com.example.delivery.repository.DirectDeliveryValidationRepository
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryValidationScreen(
    navController: NavController = androidx.navigation.compose.rememberNavController(),
    shipmentId: Int? = null
) {
    // State for signature path - stores actual stroke data
    var signatureStrokes by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var isSigned by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var signerName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var isLoadingDetails by remember { mutableStateOf(false) }
    var existingSignatureUrl by remember { mutableStateOf<String?>(null) }
    
    // Photo variables
    var hasPhoto by remember { mutableStateOf(false) }
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }

    // State for Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    
    val shipmentDetailRepository = remember { DirectShipmentDetailRepository() }
    val directValidationRepo = remember { DirectDeliveryValidationRepository() }
    
    // Log pour vérifier le shipmentId
    println("🔍 DEBUG: shipmentId reçu = $shipmentId")
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Gestion des permissions caméra
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Launcher pour demander la permission caméra
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    // Launcher pour prendre une photo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.extras?.get("data")?.let { data ->
                photoBitmap = data as? Bitmap
                hasPhoto = true
            }
        }
    }

    // Fonction pour prendre une photo
    fun takePhoto() {
        when {
            hasCameraPermission -> {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(intent) 
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    // Load shipment details to get client name
    LaunchedEffect(shipmentId) {
        shipmentId?.let { id ->
            isLoadingDetails = true
            try {
                shipmentDetailRepository.getShipmentDetails(id).collect { result ->
                    if (result is com.example.delivery.repository.Result.Success) {
                        val shipment = result.data
                        val customerName = shipment.customer?.name
                        if (customerName != null) {
                            signerName = customerName
                            clientName = customerName
                            println("🔍 DEBUG: Client name loaded: $customerName")
                        }
                        
                        val existingSignature = shipment.deliveryProof?.signatureUrl
                        if (existingSignature != null && existingSignature.isNotEmpty()) {
                            existingSignatureUrl = existingSignature
                            isSigned = true
                            println("🔍 DEBUG: Existing signature loaded: ${existingSignature.take(50)}")
                        }

                        val existingImage = shipment.deliveryProof?.imageUrl
                        if (existingImage != null && existingImage.isNotBlank() && !existingImage.contains("/9j/4AAQSkZJRgABAQEAYABgAAD")) {
                            existingImageUrl = existingImage
                            photoBitmap = base64ToBitmap(existingImage)
                            hasPhoto = true
                            println("🔍 DEBUG: Existing photo loaded: ${existingImage.take(50)}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("🔍 DEBUG: Error loading shipment details: ${e.message}")
            } finally {
                isLoadingDetails = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Validation de Livraison") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Signature Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Signature du Client",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(
                                Color.White,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        if (existingSignatureUrl != null) {
                            val bitmap = remember(existingSignatureUrl) {
                                base64ToBitmap(existingSignatureUrl!!)
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Signature existante",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    text = "Signature existante (erreur de rendu)",
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        } else {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                currentStroke = listOf(offset)
                                                isSigned = true
                                            },
                                            onDrag = { change, _ ->
                                                currentStroke = currentStroke + change.position
                                            },
                                            onDragEnd = {
                                                signatureStrokes = signatureStrokes + listOf(currentStroke)
                                                currentStroke = emptyList()
                                            }
                                        )
                                    }
                            ) {
                                // Draw all completed strokes
                                signatureStrokes.forEach { stroke ->
                                    if (stroke.size > 1) {
                                        val path = Path().apply {
                                            moveTo(stroke[0].x, stroke[0].y)
                                            for (i in 1 until stroke.size) {
                                                lineTo(stroke[i].x, stroke[i].y)
                                            }
                                        }
                                        drawPath(
                                            path = path,
                                            color = Color.Black,
                                            style = Stroke(
                                                width = 3f,
                                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                                            )
                                        )
                                    }
                                }
                                
                                // Draw current stroke
                                if (currentStroke.size > 1) {
                                    val path = Path().apply {
                                        moveTo(currentStroke[0].x, currentStroke[0].y)
                                        for (i in 1 until currentStroke.size) {
                                            lineTo(currentStroke[i].x, currentStroke[i].y)
                                        }
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color.Black,
                                        style = Stroke(
                                            width = 3f,
                                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                            
                            // Placeholder text when empty
                            if (!isSigned) {
                                Text(
                                    text = "Signer ici",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
            
            // Clear Button
            OutlinedButton(
                onClick = {
                    signatureStrokes = emptyList()
                    currentStroke = emptyList()
                    isSigned = false
                    signerName = ""
                    notes = ""
                    existingSignatureUrl = null
                    photoBitmap = null
                    hasPhoto = false
                    existingImageUrl = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Effacer",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Effacer")
            }
            
            // Signer Name
            OutlinedTextField(
                value = signerName,
                onValueChange = { signerName = it },
                label = { Text("Nom du signataire") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optionnel)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Photo de Livraison Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Photo de Preuve",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (!hasPhoto) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { takePhoto() },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Prendre une photo de preuve",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        photoBitmap?.let { bitmap ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Photo de preuve",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedButton(
                                onClick = { 
                                    photoBitmap = null
                                    hasPhoto = false
                                    existingImageUrl = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Prendre une autre photo")
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Annuler")
                }
                
                // Returns Button
                OutlinedButton(
                    onClick = {
                        if (shipmentId != null) {
                            navController.navigate("returns/$shipmentId")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF9800)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFF9800))
                ) {
                    Text("Retours")
                }
                
                // Validate Button
                Button(
                    onClick = {
                        if (isSigned && shipmentId != null && signerName.isNotBlank()) {
                            scope.launch {
                                isLoading = true
                                try {
                                    println("🔍 DEBUG: Starting direct validation process...")
                                    
                                    val signatureBase64 = if (existingSignatureUrl != null) {
                                        existingSignatureUrl!!
                                    } else {
                                        val generated = captureSignatureAsBase64(signatureStrokes, currentStroke)
                                        if (generated == null) {
                                            snackbarHostState.showSnackbar("Erreur lors de la conversion de la signature")
                                            isLoading = false
                                            return@launch
                                        }
                                        generated
                                    }
                                    
                                    val photoBase64 = photoBitmap?.let { validationBitmapToBase64(it) }
                                    
                                    println("🔍 DEBUG: shipmentId = $shipmentId")
                                    println("🔍 DEBUG: Calling direct PostgreSQL Neon validation...")
                                    
                                    val result = directValidationRepo.validateDeliveryDirect(
                                        shipmentId = shipmentId,
                                        signatureBase64 = signatureBase64,
                                        imageData = photoBase64
                                    )
                                    
                                    when (result) {
                                        is com.example.delivery.repository.Result.Success -> {
                                            snackbarHostState.showSnackbar("Livraison validée avec succès!")
                                            // Success - navigate back after short delay
                                            kotlinx.coroutines.delay(1000)
                                            navController.navigateUp()
                                        }
                                        is com.example.delivery.repository.Result.Error -> {
                                            val errorMsg = result.message
                                            snackbarHostState.showSnackbar(errorMsg)
                                            println("Error saving validation: $errorMsg")
                                        }
                                        else -> {}
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Erreur: ${e.message}")
                                    println("Exception: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = isSigned && signerName.isNotBlank() && !isLoading && shipmentId != null,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Valider",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isLoading) "Enregistrement..." else "Valider")
                }
            }
            
            // Requirements Status
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSigned && signerName.isNotBlank()) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Statut de validation:",
                        fontWeight = FontWeight.Bold,
                        color = if (isSigned && signerName.isNotBlank())
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (signerName.isNotBlank()) Icons.Default.Check else Icons.Default.Clear,
                            contentDescription = null,
                            tint = if (signerName.isNotBlank()) Color.Green else Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Nom du signataire",
                            color = if (signerName.isNotBlank())
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isSigned) Icons.Default.Check else Icons.Default.Clear,
                            contentDescription = null,
                            tint = if (isSigned) Color.Green else Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Signature du client",
                            color = if (isSigned)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (!isSigned || signerName.isBlank()) {
                        Text(
                            text = "Veuillez compléter tous les éléments obligatoires pour valider",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // DEBUG: Show button state
                    Text(
                        text = "DEBUG: isSigned=$isSigned, nom=${signerName.isNotBlank()}, shipment=${shipmentId != null}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Instructions
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Instructions:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = "• Demandez au client de signer avec son doigt\n• Assurez-vous que la signature est lisible\n• Cliquez sur 'Valider' pour confirmer",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// Function to capture signature as Base64 using stored stroke data
fun captureSignatureAsBase64(strokes: List<List<Offset>>, currentStroke: List<Offset>): String? {
    return try {
        val bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // White background
        canvas.drawColor(android.graphics.Color.WHITE)
        
        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        
        // Draw all completed strokes
        strokes.forEach { stroke ->
            if (stroke.size > 1) {
                val path = AndroidPath().apply {
                    moveTo(stroke[0].x, stroke[0].y)
                    for (i in 1 until stroke.size) {
                        lineTo(stroke[i].x, stroke[i].y)
                    }
                }
                canvas.drawPath(path, paint)
            }
        }
        
        // Draw current stroke
        if (currentStroke.size > 1) {
            val path = AndroidPath().apply {
                moveTo(currentStroke[0].x, currentStroke[0].y)
                for (i in 1 until currentStroke.size) {
                    lineTo(currentStroke[i].x, currentStroke[i].y)
                }
            }
            canvas.drawPath(path, paint)
        }
        
        // Convert bitmap to Base64
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        stream.close()
        
        val base64String = "data:image/png;base64,${Base64.encodeToString(byteArray, Base64.NO_WRAP)}"
        println("🔍 DEBUG: Signature Base64 generated: length=${base64String.length}, startsWith=${base64String.startsWith("data:image/png;base64,")}")
        return base64String
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun base64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val cleanBase64 = if (base64Str.contains(",")) {
            base64Str.substring(base64Str.indexOf(",") + 1)
        } else {
            base64Str
        }
        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun validationBitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val byteArray = outputStream.toByteArray()
    return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
}
