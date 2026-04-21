package com.example.delivery.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.delivery.models.*
import com.example.delivery.ui.DesignSystem
import com.example.delivery.network.ApiClient
import com.example.delivery.network.DeliveryProofRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnsScreen(
    navController: NavController,
    shipmentId: Int,
    shipmentNo: String = "N/A",
    onBackPressed: () -> Unit = { navController.navigateUp() }
) {
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var packagesRecovered by remember { mutableStateOf(false) }
    var packagingRecovered by remember { mutableStateOf(false) }
    var palettesCount by remember { mutableStateOf("") }
    var caissesCount by remember { mutableStateOf("") }
    var bouteillesCount by remember { mutableStateOf("") }
    var futsCount by remember { mutableStateOf("") }
    var autreCount by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var defects by remember { mutableStateOf<List<ItemDefect>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Defect form state
    var selectedArticle by remember { mutableStateOf(sampleArticles.first()) }
    var defectQuantity by remember { mutableStateOf("") }
    var defectReason by remember { mutableStateOf("") }
    var expandedArticleDropdown by remember { mutableStateOf(false) }
    var availableArticles by remember { mutableStateOf(sampleArticles) }
    var isLoadingArticles by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Load articles from API
    LaunchedEffect(Unit) {
        isLoadingArticles = true
        try {
            val apiService = com.example.delivery.network.ApiClient.getRetrofit().create(com.example.delivery.network.ItemApiService::class.java)
            val response = apiService.getItems()
            if (response.isSuccessful && response.body()?.success == true) {
                val items = response.body()?.data ?: emptyList()
                availableArticles = items.map { item ->
                    Article(
                        id = item.id.toString(),
                        name = item.description,
                        description = item.itemNo
                    )
                }
                if (availableArticles.isNotEmpty()) {
                    selectedArticle = availableArticles.first()
                }
                println("🔍 DEBUG: Loaded ${availableArticles.size} articles from API")
            }
        } catch (e: Exception) {
            println("🔍 DEBUG: Error loading articles: ${e.message}")
            // Keep sample articles as fallback
        } finally {
            isLoadingArticles = false
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            capturedImageUri?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                capturedBitmap = bitmap
            }
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            capturedBitmap = bitmap
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Retours / Vides") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shipment Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DesignSystem.Colors.BACKGROUND_GRAY
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Sizes.ELEVATION_SMALL)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Expédition #$shipmentNo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: $shipmentId",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            
            // Photo Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DesignSystem.Colors.SURFACE_WHITE
                ),
                border = BorderStroke(DesignSystem.Sizes.BORDER_THIN, DesignSystem.Colors.OUTLINE_GRAY),
                elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Sizes.ELEVATION_SMALL)
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
                    
                    if (capturedBitmap != null) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Photo de preuve",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    DesignSystem.Colors.BACKGROUND_GRAY,
                                    RoundedCornerShape(DesignSystem.Sizes.CORNER_RADIUS_SMALL)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Caméra",
                                    modifier = Modifier.size(48.dp),
                                    tint = DesignSystem.Colors.TEXT_SECONDARY
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Aucune photo",
                                    color = DesignSystem.Colors.TEXT_SECONDARY
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
                                val uri = createReturnsCacheImageUri(context)
                                capturedImageUri = uri
                                context.grantUriPermission(
                                    context.packageName,
                                    uri,
                                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                )
                                cameraLauncher.launch(uri)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DesignSystem.Colors.PRIMARY_BLUE
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Caméra")
                        }
                        
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(DesignSystem.Sizes.BORDER_THIN, DesignSystem.Colors.PRIMARY_BLUE),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DesignSystem.Colors.PRIMARY_BLUE
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Galerie")
                        }
                    }
                }
            }
            
            // Returns Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DesignSystem.Colors.SURFACE_WHITE
                ),
                border = BorderStroke(DesignSystem.Sizes.BORDER_THIN, DesignSystem.Colors.OUTLINE_GRAY),
                elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Sizes.ELEVATION_SMALL)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Retours",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Colis récupérés?")
                        Switch(
                            checked = packagesRecovered,
                            onCheckedChange = { packagesRecovered = it }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Emballages récupérés?")
                        Switch(
                            checked = packagingRecovered,
                            onCheckedChange = { packagingRecovered = it }
                        )
                    }
                }
            }
            
            // Quantities Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DesignSystem.Colors.SURFACE_WHITE
                ),
                border = BorderStroke(DesignSystem.Sizes.BORDER_THIN, DesignSystem.Colors.OUTLINE_GRAY),
                elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Sizes.ELEVATION_SMALL)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quantités récupérées",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = palettesCount,
                        onValueChange = { palettesCount = it },
                        label = { Text("Palettes") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = caissesCount,
                        onValueChange = { caissesCount = it },
                        label = { Text("Caisses") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = bouteillesCount,
                        onValueChange = { bouteillesCount = it },
                        label = { Text("Bouteilles") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = futsCount,
                        onValueChange = { futsCount = it },
                        label = { Text("Fûts") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = autreCount,
                        onValueChange = { autreCount = it },
                        label = { Text("Autre") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            // Note Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DesignSystem.Colors.SURFACE_WHITE
                ),
                border = BorderStroke(DesignSystem.Sizes.BORDER_THIN, DesignSystem.Colors.OUTLINE_GRAY),
                elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Sizes.ELEVATION_SMALL)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Note",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Commentaire (optionnel)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 3
                    )
                }
            }
            
            // Defects Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DesignSystem.Colors.SURFACE_WHITE
                ),
                border = BorderStroke(DesignSystem.Sizes.BORDER_THIN, DesignSystem.Colors.OUTLINE_GRAY),
                elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Sizes.ELEVATION_SMALL)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Défauts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Article dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedArticleDropdown,
                        onExpandedChange = { expandedArticleDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = selectedArticle.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Article") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expandedArticleDropdown
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expandedArticleDropdown,
                            onDismissRequest = { expandedArticleDropdown = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            availableArticles.forEach { article ->
                                DropdownMenuItem(
                                    text = { Text(article.name) },
                                    onClick = {
                                        selectedArticle = article
                                        expandedArticleDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Defect quantity
                    OutlinedTextField(
                        value = defectQuantity,
                        onValueChange = { defectQuantity = it },
                        label = { Text("Quantité") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Defect reason
                    OutlinedTextField(
                        value = defectReason,
                        onValueChange = { defectReason = it },
                        label = { Text("Raison") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Add defect button
                    Button(
                        onClick = {
                            if (defectQuantity.isNotBlank() && defectReason.isNotBlank()) {
                                defects = defects + ItemDefect(
                                    article = selectedArticle.name,
                                    quantity = defectQuantity.toIntOrNull() ?: 0,
                                    reason = defectReason
                                )
                                defectQuantity = ""
                                defectReason = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ajouter défaut")
                    }
                    
                    // Defects list
                    if (defects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        defects.forEachIndexed { index, defect ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = DesignSystem.Colors.BACKGROUND_GRAY
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${defect.article} x${defect.quantity}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = defect.reason,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            defects = defects.filterIndexed { i, _ -> i != index }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Supprimer",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            
            // Submit Button
            Button(
                onClick = {
                    // Convert photo to base64 if available
                    val photoBase64 = capturedBitmap?.let { bitmapToBase64(it) } ?: ""
                    println("📸 Returns submit - shipmentId: $shipmentId, photo length: ${photoBase64.length}")
                    
                    // Call API to save delivery proof with photo
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val apiService = ApiClient.getRetrofit().create(com.example.delivery.network.DeliveryValidationApiService::class.java)
                            val request = DeliveryProofRequest(
                                shipmentId = shipmentId,
                                imageUrl = photoBase64,
                                signatureUrl = null
                            )
                            val response = apiService.saveDeliveryProof(request)
                            
                            println("📸 API response: ${response.isSuccessful}, body: ${response.body()}")
                            
                            if (response.isSuccessful && response.body()?.success == true) {
                                println("✅ Delivery proof saved successfully")
                                CoroutineScope(Dispatchers.Main).launch {
                                    navController.navigateUp()
                                }
                            } else {
                                println("❌ Error saving delivery proof: ${response.message()}")
                            }
                        } catch (e: Exception) {
                            println("❌ Exception saving delivery proof: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesignSystem.Sizes.BUTTON_HEIGHT_LARGE),
                enabled = !isLoading,
                shape = RoundedCornerShape(DesignSystem.Components.BUTTON_CORNER_RADIUS),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DesignSystem.Colors.PRIMARY_BLUE
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Soumettre", fontSize = 16.sp)
                }
            }
        }
    }
}

fun createReturnsCacheImageUri(context: Context): android.net.Uri {
    val timestamp = System.currentTimeMillis()
    val fileName = "returns_photo_$timestamp.jpg"
    val cacheDir = File(context.cacheDir, "returns_photos")
    cacheDir.mkdirs()
    val imageFile = File(cacheDir, fileName)
    
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

// Function to convert bitmap to base64
fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
}
