package com.example.delivery.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PODScreen(navController: NavController) {
    var hasSignature by remember { mutableStateOf(false) }
    var hasPhoto by remember { mutableStateOf(false) }
    var isConfirming by remember { mutableStateOf(false) }
    var deliveryCompleted by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showProblemDialog by remember { mutableStateOf(false) }
    var selectedProblem by remember { mutableStateOf("") }
    
    // État pour la signature
    var signaturePath by remember { mutableStateOf(Path()) }
    var isDrawing by remember { mutableStateOf(false) }
    var signatureName by remember { mutableStateOf("") }
    var signatureNotes by remember { mutableStateOf("") }
    
    // État pour les informations optionnelles
    var deliveryTime by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var specialInstructions by remember { mutableStateOf("") }
    var packageCondition by remember { mutableStateOf("") }

    // Données simulées pour la livraison
    val deliveryInfo = remember {
        DeliveryInfo(
            id = "CMD002",
            clientName = "Logistique Corp International",
            address = "Zone Industrielle Nord, 123 Rue des Usines, 69000 Lyon",
            weight = "3.7 tonnes",
            notes = "Livraison dock n°3 - Contrôle documents requis"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preuve de Livraison") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Informations de la livraison
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Livraison #${deliveryInfo.id}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Client: ${deliveryInfo.clientName}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Adresse: ${deliveryInfo.address}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Poids: ${deliveryInfo.weight}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Notes: ${deliveryInfo.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // État de la livraison
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (deliveryCompleted) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (deliveryCompleted) Icons.Default.CheckCircle else Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = if (deliveryCompleted) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (deliveryCompleted) "Livraison Terminée" else "Livraison en Cours",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (deliveryCompleted) 
                                    "Validée à ${java.time.LocalTime.now().toString().substring(0, 5)}" 
                                else 
                                    "En attente de validation",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Section Signature
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Signature Client",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Champ pour le nom du signataire
                        OutlinedTextField(
                            value = signatureName,
                            onValueChange = { signatureName = it },
                            label = { Text("Nom du signataire") },
                            placeholder = { Text("Entrez le nom du signataire") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            enabled = !deliveryCompleted,
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        
                        // Zone de signature dessinable
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            isDrawing = true
                                            signaturePath.moveTo(offset.x, offset.y)
                                            hasSignature = true
                                        },
                                        onDrag = { _, dragAmount ->
                                            if (isDrawing) {
                                                signaturePath.lineTo(dragAmount.x, dragAmount.y)
                                            }
                                        },
                                        onDragEnd = {
                                            isDrawing = false
                                        }
                                    )
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (hasSignature) 
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
                            else 
                                null
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                if (!hasSignature) {
                                    // Texte d'instruction quand pas de signature
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Signez ici",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                } else {
                                    // Canvas pour dessiner la signature
                                    Canvas(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        drawPath(
                                            path = signaturePath,
                                            color = Color.Black,
                                            style = Stroke(width = 3.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Bouton pour effacer la signature
                        if (hasSignature) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        signaturePath = Path()
                                        hasSignature = false
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Effacer")
                                }
                            }
                        }
                        
                        // Champ pour les notes optionnelles
                        OutlinedTextField(
                            value = signatureNotes,
                            onValueChange = { signatureNotes = it },
                            label = { Text("Notes (optionnel)") },
                            placeholder = { Text("Ajoutez des commentaires ou remarques...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            enabled = !deliveryCompleted,
                            maxLines = 3,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Note,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }
            }

            // Section Informations Optionnelles
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Informations Optionnelles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Heure de livraison
                        OutlinedTextField(
                            value = deliveryTime,
                            onValueChange = { deliveryTime = it },
                            label = { Text("Heure de livraison") },
                            placeholder = { Text("HH:MM") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            enabled = !deliveryCompleted,
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        
                        // Nom du destinataire
                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            label = { Text("Nom du destinataire") },
                            placeholder = { Text("Personne ayant reçu la livraison") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            enabled = !deliveryCompleted,
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        
                        // Instructions spéciales
                        OutlinedTextField(
                            value = specialInstructions,
                            onValueChange = { specialInstructions = it },
                            label = { Text("Instructions spéciales") },
                            placeholder = { Text("Instructions particulières pour la livraison...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            enabled = !deliveryCompleted,
                            maxLines = 2,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        
                        // État des colis
                        OutlinedTextField(
                            value = packageCondition,
                            onValueChange = { packageCondition = it },
                            label = { Text("État des colis") },
                            placeholder = { Text("Remarques sur l'état des marchandises...") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !deliveryCompleted,
                            maxLines = 2,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }
            }

            // Section Photo
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Photo de Livraison",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        if (!hasPhoto) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Icône et titre
                                    Icon(
                                        Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Photo de Livraison",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Capturez les marchandises livrées",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // Bouton photo principal
                                    Button(
                                        onClick = { 
                                            hasPhoto = true
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "📸 Prendre Photo",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    
                                    // Instructions supplémentaires
                                    Text(
                                        text = "Assurez-vous que les marchandises sont bien visibles",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Icône de succès
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "📸 Photo Capturée!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Marchandises livrées avec succès",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // Bouton pour reprendre
                                    OutlinedButton(
                                        onClick = { 
                                            hasPhoto = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        border = BorderStroke(
                                            1.dp, 
                                            MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "🔄 Reprendre Photo",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Boutons d'action
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bouton de confirmation
                    Button(
                        onClick = {
                            if (hasSignature && hasPhoto && signatureName.isNotBlank() && !deliveryCompleted) {
                                isConfirming = true
                                // Simuler la confirmation
                                deliveryCompleted = true
                                isConfirming = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = hasSignature && hasPhoto && signatureName.isNotBlank() && !deliveryCompleted && !isConfirming,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isConfirming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (deliveryCompleted) "Livraison Confirmée ✓" else "Confirmer la Livraison",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    // Boutons secondaires
                    if (!deliveryCompleted) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Bouton Annuler Commande
                            Button(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Annuler")
                            }
                            
                            // Bouton Signaler Problème
                            OutlinedButton(
                                onClick = { showProblemDialog = true },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(
                                    1.dp, 
                                    MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Problème", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Bouton Support (optionnel)
                        OutlinedButton(
                            onClick = { /* Contacter support */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Contacter le Support")
                        }
                    } else {
                        // Bouton pour retourner à la liste
                        Button(
                            onClick = { 
                                navController.navigate(Screen.History.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.List, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Voir l'Historique")
                        }
                    }
                }
            }

            // Instructions
            if (!deliveryCompleted) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Instructions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            InstructionStep(1, "Arriver chez le client", true)
                            InstructionStep(2, "Entrez le nom du signataire", signatureName.isNotBlank())
                            InstructionStep(3, "Obtenir la signature", hasSignature)
                            InstructionStep(4, "Prendre photo des marchandises", hasPhoto)
                            InstructionStep(5, "Confirmer la livraison", hasSignature && hasPhoto && signatureName.isNotBlank())
                        }
                    }
                }
            }
        }
    }
    
    // Dialog d'annulation de commande
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Annuler la Commande") },
            text = { 
                Text("Êtes-vous sûr de vouloir annuler cette livraison ?\n\nCette action ne peut pas être annulée.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        // Marquer comme annulée et retourner à l'historique
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Oui, Annuler")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Non")
                }
            }
        )
    }
    
    // Dialog de signalement de problème
    if (showProblemDialog) {
        AlertDialog(
            onDismissRequest = { 
                showProblemDialog = false
                selectedProblem = ""
            },
            title = { Text("Signaler un Problème") },
            text = {
                Column {
                    Text("Quel type de problème rencontrez-vous ?")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val problems = listOf(
                        "Client absent",
                        "Adresse incorrecte", 
                        "Marchandises endommagées",
                        "Problème d'accès",
                        "Autre"
                    )
                    
                    problems.forEach { problem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    selectedProblem = problem 
                                    showProblemDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedProblem == problem,
                                onClick = { 
                                    selectedProblem = problem 
                                    showProblemDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(problem)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showProblemDialog = false
                        selectedProblem = ""
                    },
                    enabled = selectedProblem.isNotEmpty()
                ) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showProblemDialog = false
                    selectedProblem = ""
                }) {
                    Text("Annuler")
                }
            }
        )
    }
    
    // Dialog de confirmation de problème
    if (selectedProblem.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { 
                selectedProblem = ""
            },
            title = { Text("Problème Signalé") },
            text = { 
                Text("Problème signalé : $selectedProblem\n\nUn support technique vous contactera rapidement.") 
            },
            confirmButton = {
                Button(
                    onClick = { 
                        selectedProblem = ""
                        // Retourner à la liste des commandes
                        navController.popBackStack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun InstructionStep(
    step: Int,
    description: String,
    completed: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$step. $description",
            style = MaterialTheme.typography.bodyMedium,
            color = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

data class DeliveryInfo(
    val id: String,
    val clientName: String,
    val address: String,
    val weight: String,
    val notes: String
)
