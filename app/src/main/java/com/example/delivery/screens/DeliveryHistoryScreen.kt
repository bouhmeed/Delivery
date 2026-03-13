package com.example.delivery.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DeliveryHistoryScreen(navController: NavController) {
    var selectedPeriod by remember { mutableStateOf("Tous") }
    var selectedStatus by remember { mutableStateOf("Tous") }
    var searchQuery by remember { mutableStateOf("") }
    var expandedPeriod by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    
    // Utiliser les données partagées
    val deliveryHistory = DeliveryData.deliveryHistory
    
    val periodOptions = listOf("Tous", "Aujourd'hui", "Cette semaine", "Ce mois", "30 derniers jours")
    val statusOptions = listOf("Tous", "Terminée", "En cours", "Annulée", "Retard")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historique des Livraisons") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Exporter */ }) {
                        Icon(Icons.Default.Download, contentDescription = "Exporter")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        // Convertir l'historique en deliveries pour la compatibilité
        val deliveries = remember {
            deliveryHistory.map { historyItem ->
                Delivery(
                    id = "LIV-${historyItem.date.toString().replace("-", "")}",
                    date = historyItem.date,
                    restaurant = historyItem.city,
                    status = historyItem.status,
                    time = "12:00", // Heure par défaut car pas dans HistoryItem
                    driverName = DeliveryData.currentDriver.name,
                    vehicle = DeliveryData.currentDriver.vehicle,
                    weight = historyItem.deliveriesCount * 1000f // Poids estimé
                )
            }
        }
        
        // Filtrer les livraisons
        val filteredDeliveries = remember(selectedPeriod, selectedStatus, searchQuery) {
            deliveries.filter { delivery ->
                val matchesStatus = selectedStatus == "Tous" || delivery.status == selectedStatus
                val matchesSearch = searchQuery.isEmpty() || 
                    delivery.id.contains(searchQuery, ignoreCase = true) ||
                    delivery.restaurant.contains(searchQuery, ignoreCase = true) ||
                    delivery.driverName.contains(searchQuery, ignoreCase = true)
                
                matchesStatus && matchesSearch
            }
        }
        
        // Grouper par date
        val groupedDeliveries = remember(filteredDeliveries) {
            filteredDeliveries.groupBy { it.date }
                .toSortedMap(Comparator.reverseOrder())
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Carte de statistiques
            item {
                HistoryStatsCard(deliveries = deliveries)
            }
            
            // Filtres et recherche
            item {
                FilterSearchCard(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedPeriod = selectedPeriod,
                    selectedStatus = selectedStatus,
                    periodOptions = periodOptions,
                    statusOptions = statusOptions,
                    expandedPeriod = expandedPeriod,
                    expandedStatus = expandedStatus,
                    onPeriodChange = { selectedPeriod = it; expandedPeriod = false },
                    onStatusChange = { selectedStatus = it; expandedStatus = false },
                    onExpandedPeriodChange = { expandedPeriod = it },
                    onExpandedStatusChange = { expandedStatus = it }
                )
            }
            
            // Résumé des résultats
            item {
                ResultsSummaryCard(
                    totalResults = filteredDeliveries.size,
                    selectedFilters = listOfNotNull(
                        selectedPeriod.takeIf { it != "Tous" },
                        selectedStatus.takeIf { it != "Tous" },
                        searchQuery.takeIf { it.isNotEmpty() }
                    )
                )
            }
            
            // Livraisons groupées par date
            groupedDeliveries.forEach { (date, dayDeliveries) ->
                item {
                    DateHeader(date = date, count = dayDeliveries.size)
                }
                
                items(dayDeliveries) { delivery ->
                    EnhancedDeliveryCard(delivery = delivery, navController = navController)
                }
                
                if (date != groupedDeliveries.keys.last()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// Nouveaux composants optimisés

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryStatsCard(deliveries: List<Delivery>) {
    val completed = deliveries.count { it.status == "Terminée" }
    val inProgress = deliveries.count { it.status == "En cours" }
    val cancelled = deliveries.count { it.status == "Annulée" }
    val delayed = deliveries.count { it.status == "Retard" }
    
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
            // En-tête
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statistiques globales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = "Statistiques",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Statistiques principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total",
                    value = deliveries.size.toString(),
                    icon = Icons.Default.Inventory,
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = "Terminées",
                    value = completed.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    label = "En cours",
                    value = inProgress.toString(),
                    icon = Icons.Default.Schedule,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Statistiques secondaires
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Annulées",
                    value = cancelled.toString(),
                    icon = Icons.Default.Cancel,
                    color = MaterialTheme.colorScheme.error
                )
                StatItem(
                    label = "Retard",
                    value = delayed.toString(),
                    icon = Icons.Default.Warning,
                    color = Color(0xFFFF9800)
                )
                StatItem(
                    label = "Taux succès",
                    value = "${(completed.toFloat() / deliveries.size * 100).toInt()}%",
                    icon = Icons.Default.TrendingUp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSearchCard(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedPeriod: String,
    selectedStatus: String,
    periodOptions: List<String>,
    statusOptions: List<String>,
    expandedPeriod: Boolean,
    expandedStatus: Boolean,
    onPeriodChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onExpandedPeriodChange: (Boolean) -> Unit,
    onExpandedStatusChange: (Boolean) -> Unit
) {
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
            
            // Barre de recherche
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                label = { Text("Rechercher...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Rechercher"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Effacer"
                            )
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Filtres
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Période
                ExposedDropdownMenuBox(
                    expanded = expandedPeriod,
                    onExpandedChange = onExpandedPeriodChange,
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedPeriod,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Période") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriod) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedPeriod,
                        onDismissRequest = { onExpandedPeriodChange(false) }
                    ) {
                        periodOptions.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period) },
                                onClick = { onPeriodChange(period) }
                            )
                        }
                    }
                }
                
                // Statut
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = onExpandedStatusChange,
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedStatus,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Statut") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { onExpandedStatusChange(false) }
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = { onStatusChange(status) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultsSummaryCard(
    totalResults: Int,
    selectedFilters: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$totalResults livraison${if (totalResults > 1) "s" else ""} trouvée${if (totalResults > 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            if (selectedFilters.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filtres actifs",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${selectedFilters.size} filtre${if (selectedFilters.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: LocalDate, count: Int) {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CalendarToday,
            contentDescription = "Date",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatter.format(date).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "($count livraison${if (count > 1) "s" else ""})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EnhancedDeliveryCard(delivery: Delivery, navController: NavController) {
    val statusColor = when (delivery.status) {
        "Terminée" -> MaterialTheme.colorScheme.tertiary
        "En cours" -> MaterialTheme.colorScheme.secondary
        "Annulée" -> MaterialTheme.colorScheme.error
        "Retard" -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.outline
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable {
                if (delivery.status == "Terminée") {
                    navController.navigate(Screen.POD.route)
                } else {
                    navController.navigate("order_details/${delivery.id}")
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // En-tête avec ID et statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = delivery.id,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Badge(
                    containerColor = statusColor
                ) {
                    Text(
                        text = delivery.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Informations principales
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Destination",
                    value = delivery.restaurant
                )
                InfoRow(
                    icon = Icons.Default.Person,
                    label = "Chauffeur",
                    value = delivery.driverName
                )
                InfoRow(
                    icon = Icons.Default.LocalShipping,
                    label = "Véhicule",
                    value = delivery.vehicle
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Pied avec heure et actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "Heure",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = delivery.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (delivery.status == "Terminée") {
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.POD.route) },
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text(
                                text = "POD",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { 
                            navController.navigate("order_details/${delivery.id}")
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Détails",
                            tint = statusColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StatItem(
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
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

data class Delivery(
    val id: String,
    val date: LocalDate,
    val restaurant: String,
    val status: String,
    val time: String,
    val driverName: String,
    val vehicle: String,
    val weight: Float
)
