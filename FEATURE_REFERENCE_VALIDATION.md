# Delivery Validation Screen Feature Reference

## Overview
Delivery validation screen with signature capture, return button, and integration with returns page.

## File Location
`app/src/main/java/com/example/delivery/presentation/screens/DeliveryValidationScreen.kt`

## Component Details

### Signature Capture
- **Component**: `DeliveryValidationScreen` (lines 859-1199)

### Implementation
```kotlin
var signatureStrokes by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }
var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
var isSigned by remember { mutableStateOf(false) }

// Canvas with drag gesture detection
Canvas(
    modifier = Modifier.pointerInput(Unit) {
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
    // Draw signature strokes
    signatureStrokes.forEach { stroke ->
        drawPath(
            path = Path().apply {
                stroke.forEachIndexed { index, offset ->
                    if (index == 0) {
                        moveTo(offset.x, offset.y)
                    } else {
                        lineTo(offset.x, offset.y)
                    }
                }
            },
            color = Color.Black,
            style = Stroke(
                width = 3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
    
    // Draw current stroke
    if (currentStroke.isNotEmpty()) {
        drawPath(
            path = Path().apply {
                currentStroke.forEachIndexed { index, offset ->
                    if (index == 0) {
                        moveTo(offset.x, offset.y)
                    } else {
                        lineTo(offset.x, offset.y)
                    }
                }
            },
            color = Color.Black,
            style = Stroke(
                width = 3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
```

### Signature Conversion
- **Function**: `captureSignatureAsBase64(signatureStrokes, currentStroke)`
- **Process**: Creates bitmap, draws strokes, converts to Base64
- **API Upload**: Sent as `signatureData` in `DeliveryValidationRequest`

```kotlin
fun captureSignatureAsBase64(
    signatureStrokes: List<List<Offset>>,
    currentStroke: List<Offset>
): String {
    val allStrokes = if (currentStroke.isNotEmpty()) {
        signatureStrokes + listOf(currentStroke)
    } else {
        signatureStrokes
    }
    
    val bitmap = Bitmap.createBitmap(
        CANVAS_WIDTH,
        CANVAS_HEIGHT,
        Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    
    val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    
    allStrokes.forEach { stroke ->
        val path = Path()
        stroke.forEachIndexed { index, offset ->
            if (index == 0) {
                path.moveTo(offset.x, offset.y)
            } else {
                path.lineTo(offset.x, offset.y)
            }
        }
        canvas.drawPath(path, paint)
    }
    
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}
```

### Validation Form
- Signature canvas (150.dp height, white background, 8.dp rounded corners)
- Signer name input (OutlinedTextField)
- Notes input (OutlinedTextField, 100.dp height, max 3 lines)
- Clear button to reset signature and fields
- Validate button (enabled only when signed and name filled)
- Loading state with CircularProgressIndicator

```kotlin
@Composable
fun ValidationForm(
    signatureStrokes: List<List<Offset>>,
    currentStroke: List<Offset>,
    signerName: String,
    notes: String,
    onSignatureChange: (List<List<Offset>>, List<Offset>) -> Unit,
    onSignerNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onClear: () -> Unit,
    onValidate: () -> Unit,
    isLoading: Boolean
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Signature Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
        ) {
            // Signature canvas implementation
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Signer Name
        OutlinedTextField(
            value = signerName,
            onValueChange = onSignerNameChange,
            label = { Text("Nom du signataire") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 3
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f)
            ) {
                Text("Effacer")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = onValidate,
                enabled = signatureStrokes.isNotEmpty() && signerName.isNotBlank() && !isLoading,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF237FDA)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Valider")
                }
            }
        }
    }
}
```

### Return Button Behavior
- **Location**: Bottom of validation screen
- **Color**: Blue `Color(0xFF237FDA)`
- **Action**: Shows ReturnsScreen as overlay
- **State**: `showReturnsScreen` boolean flag

```kotlin
var showReturnsScreen by remember { mutableStateOf(false) }

Button(
    onClick = { showReturnsScreen = true },
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF237FDA),
        contentColor = Color.White
    )
) {
    Icon(Icons.Default.ArrowBack, contentDescription = "Retours")
    Spacer(modifier = Modifier.width(8.dp))
    Text("Retours")
}

// Overlay
if (showReturnsScreen && shipmentId != null) {
    ReturnsScreen(
        shipmentId = shipmentId.toString(),
        shipmentNo = shipmentNo ?: "",
        onBackPressed = { showReturnsScreen = false },
        onSubmitReturns = { returnsRequest ->
            // Submit to API
        }
    )
}
```

## Implementation Steps

### Step 1: Add Signature Capture Canvas
```kotlin
@Composable
fun SignatureCanvas(
    signatureStrokes: List<List<Offset>>,
    currentStroke: List<Offset>>,
    onSignatureChange: (List<List<Offset>>, List<Offset>) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onSignatureChange(signatureStrokes, listOf(offset))
                    },
                    onDrag = { change, _ ->
                        onSignatureChange(signatureStrokes, currentStroke + change.position)
                    },
                    onDragEnd = {
                        onSignatureChange(signatureStrokes + listOf(currentStroke), emptyList())
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw strokes
        }
    }
}
```

### Step 2: Add Signature to Base64 Conversion
```kotlin
fun captureSignatureAsBase64(
    signatureStrokes: List<List<Offset>>,
    currentStroke: List<Offset>
): String {
    // Implementation as shown above
}
```

### Step 3: Add Validation Form
```kotlin
@Composable
fun ValidationForm(
    // Parameters as shown above
) {
    // Implementation as shown above
}
```

### Step 4: Add Return Button
```kotlin
@Composable
fun ReturnButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF237FDA),
            contentColor = Color.White
        )
    ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Retours")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Retours")
    }
}
```

### Step 5: Add Returns Screen Overlay
```kotlin
@Composable
fun DeliveryValidationScreen(
    shipmentId: String,
    shipmentNo: String
) {
    var showReturnsScreen by remember { mutableStateOf(false) }
    
    // Main content
    
    if (showReturnsScreen) {
        ReturnsScreen(
            shipmentId = shipmentId,
            shipmentNo = shipmentNo,
            onBackPressed = { showReturnsScreen = false },
            onSubmitReturns = { request ->
                // Handle submission
            }
        )
    }
}
```

## API Endpoint Required
- `POST /api/delivery-validation/validate` - Submit validation with signature

## Notes
- Signature canvas uses drag gesture detection
- Signature converted to Base64 for API upload
- Validation requires signature and signer name
- Return button is blue (0xFF237FDA)
- ReturnsScreen shown as overlay
