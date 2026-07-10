// HomeScreen.kt – UI layer only, all data handled by HomeViewModel
package com.example.delivery.screens.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.delivery.components.TodayTourCard
import com.example.delivery.components.VehicleMaintenanceSection
import com.example.delivery.components.CommonTopAppBar
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.auth.AuthManager
import com.example.delivery.viewmodel.home.HomeViewModel
import com.example.delivery.viewmodel.home.HomeUiState
import com.example.delivery.viewmodel.delivery.TodayTourViewModel
import com.example.delivery.viewmodel.delivery.TodayTourState
import com.airbnb.lottie.compose.*

// ─────────────────────────────────────────────
// Palette "Logistics UI Kit" — cartes blanches, accent bleu unique, badges pill
// ─────────────────────────────────────────────
private val BgSoft            = Color(0xFFEAF2F8) // fond bleu ciel doux (comme Ma Tournée)
private val PureWhite         = Color(0xFFFFFFFF)
private val TextDark          = Color(0xFF1C2333)
private val TextMuted         = Color(0xFF8A93A6)
private val BorderSubtle      = Color(0xFFEAEEF5)

// Header (inchangé)
private val FigmaShadowColor  = Color(0xFF0C6BCE).copy(alpha = 0.10f)

// Accent unique façon "Logistic & Shipping UI Kit"
private val AccentBlue        = Color(0xFF2F6FED)
private val AccentBlueDark    = Color(0xFF1E4FBF)
private val AccentTeal        = Color(0xFF16C79A)
private val AccentAmber       = Color(0xFFFF9F0A)
private val AccentRed         = Color(0xFFFF4D4D)

private val GradBlue          = Brush.linearGradient(listOf(AccentBlue, AccentBlueDark))
private val ShadowSoft        = Color(0xFF1C2333).copy(alpha = 0.06f)
private val ShadowBlue        = AccentBlue.copy(alpha = 0.20f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val userEmail = authManager.getUserEmail()

    // ViewModels
    val homeViewModel: HomeViewModel = viewModel()
    val unreadCount by homeViewModel.unreadCount.collectAsStateWithLifecycle()
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val todayTourViewModel: TodayTourViewModel = viewModel()

    // Liaison du driver ID
    LaunchedEffect((uiState as? HomeUiState.Success)?.driverInfo?.id) {
        val driverIdString = (uiState as? HomeUiState.Success)?.driverInfo?.id
        driverIdString?.toIntOrNull()?.let { id ->
            todayTourViewModel.setDriverId(id)
        }
    }

    val todayTourState by todayTourViewModel.todayTourState.collectAsStateWithLifecycle()

    // Chargement initial
    LaunchedEffect(userEmail) {
        userEmail?.let { email ->
            homeViewModel.loadData(email)
            homeViewModel.loadUnreadCount()
        }
    }

    Scaffold(
        containerColor = BgSoft,
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgSoft)
                .padding(bottom = paddingValues.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ───────────────────────────────────────────────
            // ── Header with Lottie animation (INCHANGÉ) ──
            item {
                val successState = uiState as? HomeUiState.Success
                val firstName = successState?.userInfo?.firstName ?: ""
                val lastName = successState?.userInfo?.lastName ?: ""
                val driverName = if (firstName.isNotEmpty() || lastName.isNotEmpty())
                    "$firstName $lastName".trim() else "Chauffeur"

                val todayDateString = remember {
                    SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
                        .format(Date()).replaceFirstChar { it.uppercase() }
                }

                // Lottie composition loading from assets
                val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Fast Delivery.json"))
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    isPlaying = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .shadow(
                            elevation = 8.dp,
                            spotColor = FigmaShadowColor,
                            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                        )
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .statusBarsPadding()
                ) {
                    // Lottie animation as background
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.matchParentSize()
                    )

                    // Gradient overlay for readability
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF05204A).copy(alpha = 0.85f),
                                        Color(0xFF084A9E).copy(alpha = 0.85f)
                                    )
                                )
                            )
                    ) {
                        // Content overlay
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Text content
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Greeting
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Bonjour,",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PureWhite.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = driverName,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                }

                                // Date
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = PureWhite.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = todayDateString,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PureWhite
                                    )
                                }
                            }

                            // Right: Company logo
                            val configuration = LocalConfiguration.current
                            val isTablet = configuration.screenWidthDp >= 600
                            AsyncImage(
                                model = "file:///android_asset/almatrack.png",
                                contentDescription = "Almatrack Logo",
                                modifier = Modifier.size(if (isTablet) 400.dp else 120.dp)
                            )
                        }
                    }
                }
            }

            // ───────────────────────────────────────────────
            // ÉTATS : CHARGEMENT / ERREUR / SUCCÈS — nouveau style "moderne coloré"
            // ───────────────────────────────────────────────
            when (uiState) {
                is HomeUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(200.dp)
                                .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = ShadowSoft)
                                .clip(RoundedCornerShape(20.dp))
                                .background(PureWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AccentBlue, strokeWidth = 3.dp)
                        }
                    }
                }

                is HomeUiState.Error -> {
                    val message = (uiState as HomeUiState.Error).message
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = ShadowSoft)
                                .clip(RoundedCornerShape(20.dp))
                                .background(PureWhite)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(AccentRed.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = AccentRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        "Erreur de chargement",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        message,
                                        fontSize = 13.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                is HomeUiState.Success -> {
                    val data = uiState as HomeUiState.Success

                    // ── Titre "Tournée en cours" — badge dégradé ──
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(AccentBlue.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = AccentBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "Tournée en cours",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                }
                                Text(
                                    text = "Aujourd'hui",
                                    fontSize = 14.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 40.dp)
                                )
                            }
                        }
                    }

                    // ── Carte Tournée ──
                    item {
                        var isPressed by remember { mutableStateOf(false) }
                        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Calender.json"))
                        val progress by animateLottieCompositionAsState(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            isPlaying = true
                        )
                        
                        when (val state = todayTourState) {
                            is TodayTourState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                        .animateContentSize()
                                        .alpha(if (isPressed) 0.7f else 1f)
                                        .clickable { isPressed = true }
                                        .clip(RoundedCornerShape(16.dp))
                                        .height(120.dp)
                                ) {
                                    LottieAnimation(
                                        composition = composition,
                                        progress = { progress },
                                        modifier = Modifier.matchParentSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        AccentBlue.copy(alpha = 0.35f),
                                                        AccentBlueDark.copy(alpha = 0.35f)
                                                    )
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = PureWhite,
                                            strokeWidth = 3.dp,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Chargement de la tournée...",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = PureWhite
                                        )
                                    }
                                }
                            }

                            is TodayTourState.Success -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                        .animateContentSize()
                                        .clip(RoundedCornerShape(16.dp))
                                ) {
                                    LottieAnimation(
                                        composition = composition,
                                        progress = { progress },
                                        modifier = Modifier.matchParentSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        AccentBlue.copy(alpha = 0.35f),
                                                        AccentBlueDark.copy(alpha = 0.35f)
                                                    )
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { navController.navigate("tournee") }
                                                .alpha(if (isPressed) 0.7f else 1f)
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Bolt,
                                                    contentDescription = null,
                                                    tint = PureWhite,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Text(
                                                    text = "Tournée du jour",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PureWhite
                                                )
                                            }
                                            Text(
                                                text = "En cours",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PureWhite
                                            )
                                        }
                                        TodayTourCard(
                                            tourInfo   = state.tourInfo,
                                            statistics = state.statistics,
                                            navController = navController,
                                            modifier = Modifier.fillMaxWidth(),
                                            onStartTour        = { navController.navigate("tournee") },
                                            onViewDetails      = { navController.navigate("tournee") },
                                            onCompleteShipment = { /* TODO */ }
                                        )
                                    }
                                }
                            }

                            is TodayTourState.NoTour -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                        .clickable { navController.navigate("tournee") }
                                        .alpha(if (isPressed) 0.7f else 1f)
                                        .animateContentSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .height(140.dp)
                                ) {
                                    LottieAnimation(
                                        composition = composition,
                                        progress = { progress },
                                        modifier = Modifier.matchParentSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        AccentBlue.copy(alpha = 0.35f),
                                                        AccentBlueDark.copy(alpha = 0.35f)
                                                    )
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Aucune tournée planifiée",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PureWhite
                                        )
                                        Text(
                                            text = "Aucune livraison n'est prévue pour aujourd'hui",
                                            fontSize = 14.sp,
                                            color = PureWhite.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }

                            is TodayTourState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                        .clickable { todayTourViewModel.refresh() }
                                        .alpha(if (isPressed) 0.7f else 1f)
                                        .animateContentSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .height(140.dp)
                                ) {
                                    LottieAnimation(
                                        composition = composition,
                                        progress = { progress },
                                        modifier = Modifier.matchParentSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        AccentRed.copy(alpha = 0.35f),
                                                        AccentRed.copy(alpha = 0.35f)
                                                    )
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Error,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Erreur de chargement",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PureWhite
                                        )
                                        Text(
                                            text = state.message,
                                            fontSize = 14.sp,
                                            color = PureWhite.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Alerte maintenance ──
                    if (data.maintenanceAlert != null) {
                        item {
                            var isPressed by remember { mutableStateOf(false) }
                            val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Gears Lottie Animation.json"))
                            val progress by animateLottieCompositionAsState(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                isPlaying = true
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                                    .animateContentSize()
                                    .clickable { /* Navigate to maintenance details */ }
                                    .alpha(if (isPressed) 0.7f else 1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .height(160.dp)
                            ) {
                                LottieAnimation(
                                    composition = composition,
                                    progress = { progress },
                                    modifier = Modifier.matchParentSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    AccentBlue.copy(alpha = 0.35f),
                                                    AccentBlueDark.copy(alpha = 0.35f)
                                                )
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = "Warning",
                                            tint = AccentBlue,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Maintenance ${data.maintenanceAlert.type}",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PureWhite
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Urgent",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PureWhite
                                            )
                                        }
                                    }
                                    data.maintenanceAlert.vehicleName?.let { name ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.padding(start = 44.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.LocalShipping,
                                                contentDescription = null,
                                                tint = PureWhite.copy(alpha = 0.9f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "$name • ${data.maintenanceAlert.registration ?: ""}",
                                                fontSize = 15.sp,
                                                color = PureWhite.copy(alpha = 0.9f)
                                            )
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(start = 44.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = PureWhite.copy(alpha = 0.9f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Date : ${data.maintenanceAlert.nextDate ?: "Non définie"}",
                                            fontSize = 15.sp,
                                            color = PureWhite.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }


                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}