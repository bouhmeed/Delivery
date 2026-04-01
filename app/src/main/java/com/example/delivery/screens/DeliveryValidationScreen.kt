package com.example.delivery.screens

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.delivery.network.ApiClient
import com.example.delivery.network.DeliveryValidationApiService
import com.example.delivery.network.DeliveryValidationRequest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.provider.MediaStore
import android.os.Environment

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
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var signerName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    // State for Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Log pour vérifier le shipmentId
    println("🔍 DEBUG: shipmentId reçu = $shipmentId")
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            println("📸 DEBUG: Photo prise avec succès")
        } else {
            println("📸 DEBUG: Échec de la prise de photo")
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            capturedImageUri = it
            println("📸 DEBUG: Image sélectionnée depuis la galerie: $it")
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
            // Photo Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Photo de Preuve",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (capturedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(capturedImageUri),
                            contentDescription = "Photo de preuve",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    Color.White,
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aucune photo",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val uri = createImageUri(context)
                                capturedImageUri = uri
                                cameraLauncher.launch(uri)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Appareil Photo")
                        }
                        
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Galerie")
                        }
                    }
                }
            }
            
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
            
            // Clear Button
            OutlinedButton(
                onClick = {
                    signatureStrokes = emptyList()
                    currentStroke = emptyList()
                    isSigned = false
                    signerName = ""
                    notes = ""
                    capturedImageUri = null
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
                
                // Validate Button
                Button(
                    onClick = {
                        if (isSigned && capturedImageUri != null && shipmentId != null && signerName.isNotBlank()) {
                            scope.launch {
                                isLoading = true
                                try {
                                    println("🔍 DEBUG: Starting conversion process...")
                                    println("🔍 DEBUG: isSigned=$isSigned, capturedImageUri=$capturedImageUri, shipmentId=$shipmentId, signerName='$signerName'")
                                    
                                    val signatureBase64 = captureSignatureAsBase64(signatureStrokes, currentStroke)
                                    println("🔍 DEBUG: signatureBase64 result: ${if (signatureBase64 != null) "SUCCESS (${signatureBase64.length} chars)" else "NULL"}")
                                    
                                    val imageBase64 = capturedImageUri?.let { uri ->
                                        uriToBase64(context, uri)
                                    }
                                    println("🔍 DEBUG: imageBase64 result: ${if (imageBase64 != null) "SUCCESS (${imageBase64.length} chars)" else "NULL"}")
                                    
                                    if (signatureBase64 == null || imageBase64 == null) {
                                        println("🔍 DEBUG: Conversion failed - signatureBase64=$signatureBase64, imageBase64=$imageBase64")
                                        snackbarHostState.showSnackbar("Erreur lors de la conversion des images")
                                        isLoading = false
                                        return@launch
                                    }
                                    
                                    val apiService = ApiClient.getRetrofit().create(DeliveryValidationApiService::class.java)
                                    
                                    println("🔍 DEBUG: API URL = ${ApiClient.getCurrentUrl()}")
                                    println("🔍 DEBUG: shipmentId = $shipmentId")
                                    println("🔍 DEBUG: signerName = '$signerName'")
                                    println("🔍 DEBUG: signatureBase64 length = ${signatureBase64.length}")
                                    println("🔍 DEBUG: imageBase64 length = ${imageBase64.length}")
                                    
                                    val request = DeliveryValidationRequest(
                                        shipmentId = shipmentId,
                                        signerName = signerName,
                                        signatureData = signatureBase64,
                                        imageData = imageBase64,
                                        notes = notes.takeIf { it.isNotBlank() }
                                    )
                                    
                                    println("🔍 DEBUG: Request created, calling API...")
                                    val response = apiService.validateDelivery(request)
                                    
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        snackbarHostState.showSnackbar("Livraison validée avec succès!")
                                        // Success - navigate back after short delay
                                        kotlinx.coroutines.delay(1000)
                                        navController.navigateUp()
                                    } else {
                                        val errorMsg = response.body()?.message ?: "Erreur lors de la validation"
                                        snackbarHostState.showSnackbar(errorMsg)
                                        println("Error saving validation: $errorMsg")
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
                    enabled = isSigned && capturedImageUri != null && signerName.isNotBlank() && !isLoading && shipmentId != null,
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
                    containerColor = if (isSigned && capturedImageUri != null && signerName.isNotBlank()) 
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
                        color = if (isSigned && capturedImageUri != null && signerName.isNotBlank())
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
                            imageVector = if (capturedImageUri != null) Icons.Default.Check else Icons.Default.Clear,
                            contentDescription = null,
                            tint = if (capturedImageUri != null) Color.Green else Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Photo de preuve",
                            color = if (capturedImageUri != null)
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
                    
                    if (!isSigned || capturedImageUri == null || signerName.isBlank()) {
                        Text(
                            text = "Veuillez compléter tous les éléments obligatoires pour valider",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // DEBUG: Show button state
                    Text(
                        text = "DEBUG: isSigned=$isSigned, photo=${capturedImageUri != null}, nom=${signerName.isNotBlank()}, shipment=${shipmentId != null}",
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

// Function to create image URI for camera
fun createImageUri(context: Context): Uri {
    val timestamp = System.currentTimeMillis()
    val filename = "delivery_proof_$timestamp.jpg"
    
    // Use MediaStore to create a proper image URI
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Delivery")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    
    val imageUri = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )
    
    return imageUri ?: throw IllegalStateException("Failed to create image URI")
}

// Function to convert URI to Base64
fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        println("🔍 DEBUG: Converting URI to Base64: $uri")
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        if (bitmap == null) {
            println("🔍 DEBUG: Failed to decode bitmap from URI")
            return null
        }
        
        println("🔍 DEBUG: Bitmap decoded: width=${bitmap.width}, height=${bitmap.height}")
        
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        stream.close()
        
        val base64String = "data:image/jpeg;base64,${Base64.encodeToString(byteArray, Base64.NO_WRAP)}"
        println("🔍 DEBUG: Image Base64 generated: length=${base64String.length}, startsWith=${base64String.startsWith("data:image/jpeg;base64,")}")
        return base64String
    } catch (e: Exception) {
        println("🔍 DEBUG: Exception in uriToBase64: ${e.message}")
        e.printStackTrace()
        null
    }
}
