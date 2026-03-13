package com.example.delivery.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.navigation.Screen
import com.example.delivery.data.DeliveryData
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OrdersListScreen(navController: NavController) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedStatus by remember { mutableStateOf("Tous") }
    var expanded by remember { mutableStateOf(false) }
    
    // Utiliser les données partagées
    val todayTour = DeliveryData.todayTour
    val todayStats = DeliveryData.todayStats
    
    val statusOptions = listOf("Tous", "En attente", "En cours", "Terminé")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suivi mes Tournées") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Rafraîchir */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        // Convertir les expéditions en orders pour la compatibilité
        val orders = remember {
            todayTour.expeditions.map { expedition ->
                Order(
                    id = expedition.id,
                    status = when (expedition.status) {
                        "En attente" -> "À planifier"
                        "En cours" -> "En expédition"
                        "Terminé" -> "Livrée"
                        else -> "À planifier"
                    },
                    restaurant = expedition.destination,
                    time = expedition.estimatedTime.replace("~", "")
                )
            }
        }
        
        // Calculer la progression
        val totalDeliveries = orders.size
        val signedDeliveries = orders.count { it.status == "Livrée" }
        val progressPercentage = if (totalDeliveries > 0) {
            (signedDeliveries.toFloat() / totalDeliveries.toFloat()) * 100
        } else 0f
        
        val filteredOrders = remember(selectedStatus) {
            if (selectedStatus == "Tous") {
                orders
            } else {
                orders.filter { it.status == selectedStatus }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // En-tête avec filtres et progression
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Carte de progression du jour
                    DailyProgressCard(
                        signedDeliveries = signedDeliveries,
                        totalDeliveries = totalDeliveries,
                        progressPercentage = progressPercentage,
                        selectedDate = selectedDate
                    )
                    
                    // Carte de filtres
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Filtres et recherche",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sélecteur de date
                                OutlinedTextField(
                                    value = "${selectedDate.dayOfMonth} ${selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${selectedDate.year}",
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Date") },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = "Calendrier"
                                        )
                                    },
                                    trailingIcon = {
                                        Row {
                                            IconButton(
                                                onClick = { 
                                                    selectedDate = selectedDate.minusDays(1)
                                                }
                                            ) {
                                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Jour précédent")
                                            }
                                            IconButton(
                                                onClick = { 
                                                    selectedDate = selectedDate.plusDays(1)
                                                }
                                            ) {
                                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Jour suivant")
                                            }
                                        }
                                    }
                                )
                                
                                // Dropdown de statut
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = selectedStatus,
                                        onValueChange = { },
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        label = { Text("Statut") },
                                        modifier = Modifier.menuAnchor()
                                    )
                                    
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        statusOptions.forEach { status ->
                                            DropdownMenuItem(
                                                text = { Text(status) },
                                                onClick = {
                                                    selectedStatus = status
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Barre de recherche
                            OutlinedTextField(
                                value = "", // TODO: Ajouter état pour la recherche
                                onValueChange = { /* TODO: Implémenter la recherche */ },
                                label = { Text("Rechercher une commande...") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Rechercher"
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { /* TODO: Action de recherche avancée */ }
                                    ) {
                                        Icon(
                                            Icons.Default.FilterList,
                                            contentDescription = "Filtres avancés"
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Statistiques rapides
            item {
                QuickStatsCard(
                    totalOrders = totalDeliveries,
                    signedOrders = signedDeliveries,
                    pendingOrders = totalDeliveries - signedDeliveries,
                    selectedStatus = selectedStatus
                )
            }
            
            // En-tête des résultats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredOrders.size} commande${if (filteredOrders.size > 1) "s" else ""} trouvée${if (filteredOrders.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Bouton de tri
                    OutlinedButton(
                        onClick = { /* TODO: Implémenter le tri */ },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = "Trier",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Trier",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            items(filteredOrders) { order ->
                OrderCard(order = order, navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCard(order: Order, navController: NavController) {
    var selectedStatus by remember { mutableStateOf(order.status) }
    var showStatusMenu by remember { mutableStateOf(false) }
    
    val statusOptions = listOf("À planifier", "En expédition", "Livrée")
    
    // Définir les couleurs en fonction du statut
    val statusColor = when (selectedStatus) {
        "À planifier" -> MaterialTheme.colorScheme.secondary
        "En expédition" -> MaterialTheme.colorScheme.primary
        "Livrée" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    
    val statusContainerColor = when (selectedStatus) {
        "À planifier" -> MaterialTheme.colorScheme.secondaryContainer
        "En expédition" -> MaterialTheme.colorScheme.primaryContainer
        "Livrée" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = { navController.navigate("order_details/${order.id}") }),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusContainerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // En-tête de la commande
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Informations principales
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = order.id,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Badge de statut
                        Badge(
                            containerColor = statusColor
                        ) {
                            Text(
                                text = selectedStatus,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = order.restaurant,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = order.time,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Icône de navigation
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Voir détails",
                    tint = statusColor,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Section de changement de statut et bouton POD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Changer le statut:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bouton POD - seulement si le statut est "Livrée"
                    OutlinedButton(
                        onClick = { 
                            if (selectedStatus == "Livrée") {
                                navController.navigate(Screen.POD.route)
                            }
                        },
                        modifier = Modifier.height(36.dp),
                        enabled = selectedStatus == "Livrée", // Actif seulement si livré
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedStatus == "Livrée") 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surface,
                            contentColor = if (selectedStatus == "Livrée") 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(
                            1.dp, 
                            if (selectedStatus == "Livrée") 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = "Signé",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedStatus == "Livrée") 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    // Bouton Map
                    OutlinedButton(
                        onClick = { 
                            // Ouvrir la carte - navigation vers une page de carte
                            navController.navigate("map_screen/${order.id}")
                        },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = "Voir sur la carte",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Map",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Bouton Call
                    OutlinedButton(
                        onClick = { 
                            // Action pour appeler le client
                            // Simule un appel téléphonique
                        },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.tertiary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Appeler le client",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Call",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Box {
                        OutlinedButton(
                            onClick = { showStatusMenu = true },
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = statusContainerColor,
                                contentColor = statusColor
                            ),
                            border = BorderStroke(1.dp, statusColor)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedStatus,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Ouvrir le menu",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false },
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(8.dp)
                        )
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Indicateur de couleur pour le statut
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    when (status) {
                                                        "À planifier" -> MaterialTheme.colorScheme.secondary
                                                        "En expédition" -> MaterialTheme.colorScheme.primary
                                                        "Livrée" -> MaterialTheme.colorScheme.tertiary
                                                        else -> MaterialTheme.colorScheme.outline
                                                    },
                                                    RoundedCornerShape(6.dp)
                                                )
                                        )
                                        
                                        Text(
                                            text = status,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        Spacer(modifier = Modifier.weight(1f))
                                        
                                        if (status == selectedStatus) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Sélectionné",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedStatus = status
                                    showStatusMenu = false
                                    // API call pour mettre à jour le statut
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class Order(
    val id: String,
    val status: String,
    val restaurant: String,
    val time: String
)

@Composable
fun DailyProgressCard(
    signedDeliveries: Int,
    totalDeliveries: Int,
    progressPercentage: Float,
    selectedDate: LocalDate
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // En-tête avec la date et le statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progression du ${selectedDate.dayOfMonth} ${selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                if (progressPercentage >= 100f) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Terminé",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Texte de progression
            Text(
                text = "$signedDeliveries sur $totalDeliveries livraisons signées",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Barre de progression
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${progressPercentage.toInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = when {
                            progressPercentage >= 100f -> "✅ Terminé"
                            progressPercentage >= 75f -> "🎯 Presque terminé"
                            progressPercentage >= 50f -> "📈 En bonne voie"
                            progressPercentage >= 25f -> "🚀 Démarré"
                            else -> "⏳ En cours"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Barre de progression visuelle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressPercentage / 100f)
                            .fillMaxHeight()
                            .background(
                                when {
                                    progressPercentage >= 100f -> MaterialTheme.colorScheme.tertiary
                                    progressPercentage >= 75f -> MaterialTheme.colorScheme.primary
                                    progressPercentage >= 50f -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                },
                                RoundedCornerShape(6.dp)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Détail par statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressStat(
                    label = "À planifier",
                    count = totalDeliveries - signedDeliveries - (totalDeliveries * 0.25f).toInt(),
                    color = MaterialTheme.colorScheme.secondary
                )
                ProgressStat(
                    label = "En expédition",
                    count = (totalDeliveries * 0.25f).toInt(),
                    color = MaterialTheme.colorScheme.primary
                )
                ProgressStat(
                    label = "Livrée",
                    count = signedDeliveries,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun ProgressStat(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun QuickStatsCard(
    totalOrders: Int,
    signedOrders: Int,
    pendingOrders: Int,
    selectedStatus: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // En-tête
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statistiques rapides",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "Statistiques",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Statistiques
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStatItem(
                    label = "Total",
                    value = totalOrders.toString(),
                    icon = Icons.Default.Inventory,
                    color = MaterialTheme.colorScheme.secondary
                )
                QuickStatItem(
                    label = "Signées",
                    value = signedOrders.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.tertiary
                )
                QuickStatItem(
                    label = "En attente",
                    value = pendingOrders.toString(),
                    icon = Icons.Default.Schedule,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Filtre actuel
            if (selectedStatus != "Tous") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filtre actif",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filtre actif: $selectedStatus",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun QuickStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        )
    }
}
