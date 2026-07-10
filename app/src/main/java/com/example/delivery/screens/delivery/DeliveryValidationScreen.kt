package com.example.delivery.screens.delivery

import com.example.delivery.repository.Result
import androidx.compose.ui.layout.onSizeChanged

import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.delivery.repository.delivery.DirectShipmentDetailRepository
import com.example.delivery.repository.delivery.DirectDeliveryValidationRepository
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath

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
fun DeliveryValidationScreen(
    navController: NavController = androidx.navigation.compose.rememberNavController(),
    shipmentId: Int? = null
) {
    // State for signature path - stores actual stroke data
    var signatureStrokes by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasWidth by remember { mutableStateOf(0) }
    var canvasHeight by remember { mutableStateOf(0) }
    var isSigned by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var signerName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var isLoadingDetails by remember { mutableStateOf(false) }
    var existingSignatureUrl by remember { mutableStateOf<String?>(null) }
    

    // State for Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    
    val shipmentDetailRepository = remember { DirectShipmentDetailRepository() }
    val directValidationRepo = remember { DirectDeliveryValidationRepository() }
    
    // Log pour vérifier le shipmentId
    println("🔍 DEBUG: shipmentId reçu = $shipmentId")
    
    val scope = rememberCoroutineScope()

    
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
                                    text = "Validation de Livraison",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { navController.navigateUp() },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Retour",
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
                            Text(
                                text = "Signature déjà enregistrée",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .onSizeChanged { size ->
                                        canvasWidth = size.width
                                        canvasHeight = size.height
                                    }
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
                                        val generated = captureSignatureAsBase64(
                                            strokes = signatureStrokes,
                                            currentStroke = currentStroke,
                                            width = canvasWidth,
                                            height = canvasHeight
                                        )
                                        if (generated == null) {
                                            snackbarHostState.showSnackbar("Erreur lors de la conversion de la signature")
                                            isLoading = false
                                            return@launch
                                        }
                                        generated
                                    }
                                    
                                    
                                    println("🔍 DEBUG: shipmentId = $shipmentId")
                                    println("🔍 DEBUG: Calling direct PostgreSQL Neon validation...")
                                    
                                    val result = directValidationRepo.validateDeliveryDirect(
                                        shipmentId = shipmentId,
                                        signatureBase64 = signatureBase64,
                                        imageData = null
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
        }
    }
}

// Function to capture signature as Base64 using stored stroke data
fun captureSignatureAsBase64(
    strokes: List<List<Offset>>,
    currentStroke: List<Offset>,
    width: Int,
    height: Int
): String? {
    return try {
        val bmpWidth = if (width > 0) width else 400
        val bmpHeight = if (height > 0) height else 200
        
        val bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
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

