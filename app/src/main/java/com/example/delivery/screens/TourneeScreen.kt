package com.example.delivery.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.navigation.Screen
import com.example.delivery.data.DeliveryData
import java.time.LocalDate
import java.time.YearMonth

// Classes de données pour la compatibilité (remplacées par DeliveryData)
typealias TourInfo = com.example.delivery.data.TourInfo
typealias ExpeditionInfo = com.example.delivery.data.ExpeditionInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourneeScreen(navController: NavController) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(LocalDate.now())) }
    var showStats by remember { mutableStateOf(false) }
    var includeReturns by remember { mutableStateOf(false) }
    
    // Utiliser les données partagées
    val todayTour = DeliveryData.todayTour
    val allTours = remember { listOf(todayTour) }
    
    // Données simulées des tournees par jour
    val toursByDate = remember {
        mapOf(
            LocalDate.now() to todayTour.expeditionCount,
            LocalDate.now().minusDays(1) to 2,
            LocalDate.now().plusDays(1) to 4,
            LocalDate.now().plusDays(2) to 3,
        )
    }
    
    val toursByLocation = remember {
        mapOf(
            todayTour.city to todayTour.expeditionCount
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Ma Tournee", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { showStats = !showStats }) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "Statistiques",
                            tint = if (showStats) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
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
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // En-tête avec calendrier et filtres
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Calendrier des tournées
                    TourneeCalendarCard(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        toursByDate = toursByDate,
                        onDateSelected = { date -> selectedDate = date },
                        onMonthChanged = { month -> currentMonth = month }
                    )
                    
                    // Options de filtrage
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
                                text = "Options",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = includeReturns,
                                    onCheckedChange = { includeReturns = it }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Inclure les retours",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            
            // Résumé du jour sélectionné
            item {
                DaySummaryCard(
                    selectedDate = selectedDate,
                    allTours = allTours,
                    toursByDate = toursByDate
                )
            }
            
            // Tournées par ville
            item {
                Text(
                    text = "Tournées du ${selectedDate.dayOfMonth} ${selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Afficher toutes les tournées
            items(allTours) { tour ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Carte d'informations de la tournée
                    TourInfoCard(currentTour = tour)
                    
                    // Section des expéditions pour cette ville
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Expéditions (${tour.expeditionCount})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Bouton d'action rapide
                            OutlinedButton(
                                onClick = { 
                                    // Action pour voir tous les détails
                                },
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text(
                                    text = "Voir tout",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        // Liste des expéditions pour cette ville
                        tour.expeditions.forEach { expedition ->
                            ExpeditionCard(expedition = expedition)
                        }
                    }
                    
                    // Séparateur entre les villes
                    if (tour.city != allTours.last().city) {
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// Nouveaux composants pour la tournée
@Composable
fun TourInfoCard(currentTour: TourInfo) {
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
            // Ville en haut
            Text(
                text = currentTour.city,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Nombre d'expéditions
            Text(
                text = "${currentTour.expeditionCount} expéditions dans cette ville",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Informations chauffeur et véhicule
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Nom du chauffeur
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Chauffeur",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = currentTour.driverName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Véhicule
                Column(
                    modifier = Modifier.weight(1.5f)
                ) {
                    Text(
                        text = "Véhicule",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = currentTour.vehicle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ExpeditionCard(expedition: ExpeditionInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { /* Navigation vers détails de l'expédition */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // En-tête avec ID et destination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expedition.id,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = expedition.destination,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Détails de l'expédition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Volume et poids
                Column {
                    Text(
                        text = expedition.volume,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = expedition.weight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Temps estimé
                Text(
                    text = expedition.estimatedTime,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

// Composants pour le calendrier des tournées
@Composable
fun TourneeCalendarCard(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    toursByDate: Map<LocalDate, Int>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // En-tête du calendrier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMonthChanged(currentMonth.minusMonths(1)) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft, 
                        contentDescription = "Mois précédent",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(
                    onClick = { onMonthChanged(currentMonth.plusMonths(1)) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowRight, 
                        contentDescription = "Mois suivant",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Jours de la semaine
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Grille du calendrier
            TourneeCalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                toursByDate = toursByDate,
                onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
fun TourneeCalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    toursByDate: Map<LocalDate, Int>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()
    
    // Corriger le calcul du premier jour de la semaine
    // En Java/Kotlin: Monday=1, Tuesday=2, ..., Sunday=7
    // On veut que Monday=0, Tuesday=1, ..., Sunday=6 pour commencer par lundi
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value - 1) % 7
    
    // Créer les jours du calendrier
    val calendarDays = mutableListOf<LocalDate?>()
    
    // Ajouter les jours vides du début
    repeat(firstDayOfWeek) {
        calendarDays.add(null)
    }
    
    // Ajouter tous les jours du mois
    for (day in 1..lastDayOfMonth.dayOfMonth) {
        calendarDays.add(currentMonth.atDay(day))
    }
    
    // Afficher en grille de 7 colonnes
    Column {
        var weekDays = mutableListOf<LocalDate?>()
        
        calendarDays.forEachIndexed { index, date ->
            weekDays.add(date)
            
            // Créer une ligne chaque semaine (7 jours)
            if ((index + 1) % 7 == 0 || index == calendarDays.lastIndex) {
                // Compléter la semaine si nécessaire
                while (weekDays.size < 7) {
                    weekDays.add(null)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weekDays.forEach { day ->
                        TourneeCalendarDay(
                            date = day,
                            isSelected = day == selectedDate,
                            hasTour = day != null && toursByDate.containsKey(day!!),
                            tourCount = day?.let { toursByDate[it] } ?: 0,
                            onClick = { 
                                if (day != null) {
                                    onDateSelected(day)
                                }
                            }
                        )
                    }
                }
                
                weekDays.clear()
                if (index < calendarDays.lastIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun TourneeCalendarDay(
    date: LocalDate?,
    isSelected: Boolean,
    hasTour: Boolean,
    tourCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(40.dp)
            .clickable(enabled = date != null, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                hasTour -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (date != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    if (hasTour && tourCount > 0) {
                        Text(
                            text = tourCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DaySummaryCard(
    selectedDate: LocalDate,
    allTours: List<TourInfo>,
    toursByDate: Map<LocalDate, Int>
) {
    val totalExpeditions = allTours.sumOf { it.expeditionCount }
    val totalTours = allTours.size
    val dayTourCount = toursByDate[selectedDate] ?: 0
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                    text = "Résumé du jour",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                
                Icon(
                    Icons.Default.Summarize,
                    contentDescription = "Résumé",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Statistiques
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Tournées",
                    value = totalTours.toString(),
                    icon = Icons.Default.Route,
                    color = MaterialTheme.colorScheme.tertiary
                )
                SummaryItem(
                    label = "Expéditions",
                    value = totalExpeditions.toString(),
                    icon = Icons.Default.LocalShipping,
                    color = MaterialTheme.colorScheme.secondary
                )
                SummaryItem(
                    label = "Villes",
                    value = totalTours.toString(),
                    icon = Icons.Default.LocationCity,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
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
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
        )
    }
}
