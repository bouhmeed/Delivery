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

import com.example.delivery.components.VehicleMaintenanceSection
import com.example.delivery.components.delivery.SimpleTripCard
import com.example.delivery.components.delivery.VehicleMaintenanceCard
import com.example.delivery.viewmodel.delivery.SimpleTodayTripViewModel
import com.example.delivery.viewmodel.delivery.SimpleTripState
import com.example.delivery.viewmodel.delivery.VehicleMaintenanceViewModel
import com.example.delivery.viewmodel.delivery.VehicleMaintenanceState
import com.example.delivery.components.CommonTopAppBar
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.auth.AuthManager
import com.example.delivery.viewmodel.home.HomeViewModel
import com.example.delivery.viewmodel.home.HomeUiState
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
    
    val simpleTripViewModel: SimpleTodayTripViewModel = viewModel()
    val tripState by simpleTripViewModel.state.collectAsStateWithLifecycle()

    val vehicleMaintenanceViewModel: VehicleMaintenanceViewModel = viewModel()
    val maintenanceState by vehicleMaintenanceViewModel.state.collectAsStateWithLifecycle()

    // Load trip info when driver is available
    LaunchedEffect(uiState) {
        android.util.Log.d("HomeScreen", "🔄 LaunchedEffect triggered - uiState: ${uiState::class.simpleName}")
        if (uiState is HomeUiState.Success) {
            val driverId = (uiState as HomeUiState.Success).driverInfo?.id?.toIntOrNull()
            android.util.Log.d("HomeScreen", "✅ Success state - driverId: $driverId")
            driverId?.let { 
                android.util.Log.d("HomeScreen", "📞 Calling loadTripInfo with driverId: $it")
                simpleTripViewModel.loadTripInfo(it)
                android.util.Log.d("HomeScreen", "🔧 Calling loadMaintenanceInfo with driverId: $it")
                vehicleMaintenanceViewModel.loadMaintenanceInfo(it)
            } ?: android.util.Log.w("HomeScreen", "⚠️ driverId is null in Success state")
        } else {
            android.util.Log.d("HomeScreen", "⏳ Waiting for Success state, current: ${uiState::class.simpleName}")
        }
    }

    // Chargement initial
    LaunchedEffect(userEmail) {
        android.util.Log.d("HomeScreen", "🔄 Initial LaunchedEffect - userEmail: $userEmail")
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
            // ── Vehicle Maintenance Card ──
            item {
                val currentMaintenanceState = maintenanceState
                android.util.Log.d("HomeScreen", "🔧 Rendering maintenance state: ${currentMaintenanceState::class.simpleName}")
                when (currentMaintenanceState) {
                    is VehicleMaintenanceState.Loading -> {
                        android.util.Log.d("HomeScreen", "⏳ Rendering Loading state")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(100.dp)
                                .background(
                                    color = Color(0xFFF5F5F5),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AccentBlue)
                        }
                    }
                    is VehicleMaintenanceState.Success -> {
                        android.util.Log.d("HomeScreen", "✅ Rendering Success state - hasMaintenance: ${currentMaintenanceState.maintenanceInfo.hasMaintenance}")
                        VehicleMaintenanceCard(
                            maintenanceInfo = currentMaintenanceState.maintenanceInfo,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    is VehicleMaintenanceState.Error -> {
                        android.util.Log.d("HomeScreen", "❌ Rendering Error state: ${currentMaintenanceState.message}")
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(100.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Erreur de chargement",
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // ───────────────────────────────────────────────
            // ── Today's Trip Card ──
            item {
                val currentState = tripState
                android.util.Log.d("HomeScreen", "🎨 Rendering trip state: ${currentState::class.simpleName}")
                when (currentState) {
                    is SimpleTripState.Loading -> {
                        android.util.Log.d("HomeScreen", "⏳ Rendering Loading state")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(100.dp)
                                .background(
                                    color = Color(0xFFF5F5F5),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AccentBlue)
                        }
                    }
                    is SimpleTripState.Success -> {
                        android.util.Log.d("HomeScreen", "✅ Rendering Success state - hasTrip: ${currentState.tripInfo.hasTrip}")
                        SimpleTripCard(
                            tripInfo = currentState.tripInfo,
                            navController = navController,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    is SimpleTripState.Error -> {
                        android.util.Log.d("HomeScreen", "❌ Rendering Error state: ${currentState.message}")
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(100.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Erreur de chargement",
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}