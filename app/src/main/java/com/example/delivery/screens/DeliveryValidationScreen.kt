package com.example.delivery.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryValidationScreen(
    navController: NavController = androidx.navigation.compose.rememberNavController()
) {
    // State for signature path
    var signaturePath by remember { mutableStateOf(Path()) }
    var isSigned by remember { mutableStateOf(false) }
    var currentPath by remember { mutableStateOf(Path()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Validation de Livraison",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Text(
                text = "Veuillez faire signer le client",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Le client doit signer ci-dessous pour confirmer la réception de la livraison",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Signature Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    // Signature Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(
                                width = 2.dp,
                                color = if (isSigned) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPath = Path().apply {
                                            moveTo(offset.x, offset.y)
                                        }
                                        isSigned = true
                                    },
                                    onDrag = { change, _ ->
                                        currentPath.lineTo(change.position.x, change.position.y)
                                    },
                                    onDragEnd = {
                                        signaturePath = Path().apply {
                                            addPath(signaturePath)
                                            addPath(currentPath)
                                        }
                                        currentPath = Path()
                                    }
                                )
                            }
                    ) {
                        // Draw the main signature path
                        drawPath(
                            path = signaturePath,
                            color = Color.Black,
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            )
                        )
                        
                        // Draw the current path being drawn
                        drawPath(
                            path = currentPath,
                            color = Color.Black,
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            )
                        )
                    }
                    
                    // Placeholder text when empty
                    if (!isSigned) {
                        Text(
                            text = "Signer ici",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clear Button
                OutlinedButton(
                    onClick = {
                        signaturePath = Path()
                        currentPath = Path()
                        isSigned = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Effacer",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Effacer")
                }
                
                // Validate Button
                Button(
                    onClick = {
                        if (isSigned) {
                            // TODO: Handle signature validation
                            navController.navigateUp()
                        }
                    },
                    enabled = isSigned,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Valider",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Valider")
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
