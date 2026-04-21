# Returns Page Feature Reference - Photo Proof

## Overview
Returns page with photo capture, display, form sections, and submission functionality.

## File Location
`app/src/main/java/com/example/delivery/presentation/screens/ReturnsScreen.kt`

## Component Details

### Return Page Structure
- **Component**: `ReturnsScreen` (lines 7000-7326)
- **DesignSystem**: Uses centralized design tokens

### Sections
1. **Shipment Header Card**
   - Shipment ID display
   - Shipment number display
   - Gray background `BACKGROUND_GRAY`
   - 2.dp elevation

2. **Photo Section**
   - Title: "Photo de Preuve"
   - Photo display (200.dp height, rounded corners)
   - Placeholder when no photo (icon + text)
   - Camera button (PRIMARY_BLUE)
   - Gallery button (outlined with PRIMARY_BLUE border)

3. **Returns Section**
   - Question: "Colis récupérés?" (Yes/No)
   - Question: "Emballages récupérés?" (Yes/No)
   - Toggle switches for boolean values

4. **Quantities Section**
   - Palettes count
   - Caisses count
   - Bouteilles count
   - Fûts count
   - Autre count
   - Numeric keyboard input

5. **Note Section**
   - Comment text area
   - Multi-line input

6. **Defects Section**
   - Article dropdown (loaded from API)
   - Defect quantity input
   - Defect reason input
   - Add defect button
   - Defects list with remove button
   - ExposedDropdownMenu for article selection

### Submit Button
- Location: Bottom bar
- Color: PRIMARY_BLUE
- Height: 56.dp
- Rounded corners: BUTTON_CORNER_RADIUS
- Loading state with spinner
- Disabled during submission

## Image Capture

### Camera Launcher
```kotlin
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
```

### Gallery Launcher
```kotlin
val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri?.let {
        val inputStream = context.contentResolver.openInputStream(it)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        capturedBitmap = bitmap
    }
}
```

### URI Creation
```kotlin
fun createReturnsCacheImageUri(context: Context): Uri {
    val timestamp = System.currentTimeMillis()
    val fileName = "returns_photo_$timestamp.jpg"
    val cacheDir = File(context.cacheDir, "returns_photos")
    cacheDir.mkdirs()
    val imageFile = File(cacheDir, fileName)
    
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
```

### Launch Camera
```kotlin
val uri = createReturnsCacheImageUri(context)
capturedImageUri = uri
context.grantUriPermission(context.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
cameraLauncher.launch(uri)
```

## Image Display

### Component
```kotlin
@Composable
fun PhotoDisplay(
    bitmap: Bitmap?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
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
                .background(BACKGROUND_GRAY, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CameraAlt, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Aucune photo")
            }
        }
    }
}
```

### Photo Buttons
```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceEvenly
) {
    Button(
        onClick = onCameraClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = DesignSystem.Colors.PRIMARY_BLUE
        )
    ) {
        Icon(Icons.Default.CameraAlt)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Caméra")
    }
    
    OutlinedButton(
        onClick = onGalleryClick,
        border = BorderStroke(1.dp, DesignSystem.Colors.PRIMARY_BLUE),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = DesignSystem.Colors.PRIMARY_BLUE
        )
    ) {
        Icon(Icons.Default.PhotoLibrary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Galerie")
    }
}
```

## Image Processing

### Bitmap Loading
```kotlin
fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        null
    }
}
```

### Base64 Conversion
```kotlin
fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}
```

### Upload
```kotlin
val photoBase64 = capturedBitmap?.let { bitmapToBase64(it) }

val returnsRequest = ReturnsRequest(
    shipmentId = shipmentId,
    shipmentNo = shipmentNo,
    packagesRecovered = packagesRecovered,
    packagingRecovered = packagingRecovered,
    quantities = RecoveredQuantities(
        palettes = palettesCount,
        caisses = caissesCount,
        bouteilles = bouteillesCount,
        futs = futsCount,
        autre = autreCount
    ),
    comment = comment,
    defects = defects,
    photoUri = capturedImageUri?.toString(),
    photoBase64 = photoBase64
)
```

## Implementation Steps

### Step 1: Add File Provider Configuration
```xml
<!-- AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### Step 2: Add file_paths.xml
```xml
<!-- res/xml/file_paths.xml -->
<paths>
    <cache-path name="returns_photos" path="returns_photos/" />
    <external-path name="external_files" path="." />
</paths>
```

### Step 3: Add Camera Permissions
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### Step 4: Create ReturnsScreen Component
```kotlin
@Composable
fun ReturnsScreen(
    shipmentId: String,
    shipmentNo: String,
    onBackPressed: () -> Unit,
    onSubmitReturns: (ReturnsRequest) -> Unit
) {
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
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
    
    // Camera and gallery launchers
    val cameraLauncher = rememberLauncherForActivityResult(...)
    val galleryLauncher = rememberLauncherForActivityResult(...)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Retours") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shipment Header
            item {
                ShipmentHeaderCard(shipmentId, shipmentNo)
            }
            
            // Photo Section
            item {
                PhotoSection(
                    bitmap = capturedBitmap,
                    onCameraClick = { /* launch camera */ },
                    onGalleryClick = { /* launch gallery */ }
                )
            }
            
            // Returns Section
            item {
                ReturnsSection(
                    packagesRecovered = packagesRecovered,
                    packagingRecovered = packagingRecovered,
                    onPackagesRecoveredChange = { packagesRecovered = it },
                    onPackagingRecoveredChange = { packagingRecovered = it }
                )
            }
            
            // Quantities Section
            item {
                QuantitiesSection(
                    palettesCount = palettesCount,
                    caissesCount = caissesCount,
                    bouteillesCount = bouteillesCount,
                    futsCount = futsCount,
                    autreCount = autreCount,
                    onPalettesChange = { palettesCount = it },
                    onCaissesChange = { caissesCount = it },
                    onBouteillesChange = { bouteillesCount = it },
                    onFutsChange = { futsCount = it },
                    onAutreChange = { autreCount = it }
                )
            }
            
            // Note Section
            item {
                NoteSection(
                    comment = comment,
                    onCommentChange = { comment = it }
                )
            }
            
            // Defects Section
            item {
                DefectsSection(
                    defects = defects,
                    onAddDefect = { defects = defects + it },
                    onRemoveDefect = { defects = defects - it }
                )
            }
            
            // Submit Button
            item {
                SubmitButton(
                    isLoading = isLoading,
                    onClick = {
                        isLoading = true
                        val request = ReturnsRequest(...)
                        onSubmitReturns(request)
                        isLoading = false
                    }
                )
            }
        }
    }
}
```

### Step 5: Add API Endpoint
```kotlin
interface ReturnsApiService {
    @POST("api/returns/submit")
    suspend fun submitReturns(@Body request: ReturnsRequest): Response<ReturnsResponse>
}
```

## API Endpoint Required
- `POST /api/returns/submit` - Submit returns with photo proof

## Notes
- Photo captured and displayed in ReturnsScreen
- Base64 conversion for API upload
- Camera and gallery both supported
- Photo stored in app cache directory
- FileProvider required for URI sharing
