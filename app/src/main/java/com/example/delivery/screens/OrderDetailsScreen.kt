package com.example.delivery.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OrderDetailsScreen(navController: NavController) {
    var orderStatus by remember { mutableStateOf("En attente") }
    
    // Données simulées pour la commande
    val order = remember {
        OrderDetail(
            id = "CMD002",
            clientName = "Logistique Corp International",
            phoneNumber = "+33 6 12 34 56 78",
            address = "Zone Industrielle Nord, 123 Rue des Usines, 69000 Lyon, France",
            items = listOf(
                OrderItem("Palette de Matériaux", "2 x 1200kg", "2400kg"),
                OrderItem("Conteneur Pièces", "1 x 800kg", "800kg"),
                OrderItem("Équipement Industriel", "1 x 500kg", "500kg")
            ),
            deliveryInstructions = "Livraison dock n°3. Présence obligatoire du réceptionnaire. Contrôle des documents et pesage obligatoire. Horaires d'accès: 8h-17h. Appeler 30min avant arrivée.",
            restaurant = "Entrepôt Central",
            estimatedTime = "13:15",
            totalAmount = "3.7 tonnes"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails Commande") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Partager les détails */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Partager")
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
            // Carte principale avec informations de la commande
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Commande #${order.id}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Entrepôt: ${order.restaurant}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Heure estimée: ${order.estimatedTime}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Poids total: ${order.totalAmount}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Statut de la commande
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Statut: ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Badge(
                                containerColor = when (orderStatus) {
                                    "En attente" -> MaterialTheme.colorScheme.surfaceVariant
                                    "En cours" -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.tertiary
                                }
                            ) {
                                Text(
                                    text = orderStatus,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Informations du client
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Informations Client",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        InfoRowWithIcon(
                            icon = Icons.Default.Person,
                            label = "Nom",
                            value = order.clientName
                        )
                        
                        InfoRowWithIcon(
                            icon = Icons.Default.Phone,
                            label = "Téléphone",
                            value = order.phoneNumber
                        )
                        
                        InfoRowWithIcon(
                            icon = Icons.Default.LocationOn,
                            label = "Adresse",
                            value = order.address
                        )
                    }
                }
            }

            // Articles de la commande
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Marchandises / Colis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        order.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = item.quantity,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = item.price,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (order.items.indexOf(item) < order.items.size - 1) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }

            // Instructions de livraison
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Instructions de Livraison",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = order.deliveryInstructions,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Boutons d'action
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Deux boutons principaux toujours visibles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Bouton Commencer le Transport
                        Button(
                            onClick = { 
                                if (orderStatus == "En attente") {
                                    orderStatus = "En cours"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = orderStatus == "En attente",
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (orderStatus == "En attente") 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (orderStatus == "En attente") 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Commencer le Transport",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        // Bouton Valider la Livraison
                        Button(
                            onClick = { 
                                if (orderStatus == "En cours") {
                                    navController.navigate(Screen.POD.route) 
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = orderStatus == "En cours",
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (orderStatus == "En cours") 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (orderStatus == "En cours") 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Valider la Livraison",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    // Boutons secondaires
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Appeler le client */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Appeler Client")
                        }
                        
                        OutlinedButton(
                            onClick = { /* Ouvrir la carte */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ouvrir Carte")
                        }
                    }
                    
                    // Bouton annuler (toujours visible sauf si terminée)
                    if (orderStatus != "Terminée") {
                        OutlinedButton(
                            onClick = { 
                                // Retour à la liste sans changement
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(
                                1.dp, 
                                MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.ArrowBack, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Retour à la liste", 
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRowWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .padding(end = 12.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

data class OrderDetail(
    val id: String,
    val clientName: String,
    val phoneNumber: String,
    val address: String,
    val items: List<OrderItem>,
    val deliveryInstructions: String,
    val restaurant: String,
    val estimatedTime: String,
    val totalAmount: String
)

data class OrderItem(
    val name: String,
    val quantity: String,
    val price: String
)
