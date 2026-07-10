package com.example.delivery.screens.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.delivery.components.BottomNavigationBar
import com.example.delivery.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreen(navController: NavController) {
    val PureWhite = Color(0xFFFFFFFF)
    val FigmaShadowColor = Color(0xFF0C6BCE).copy(alpha = 0.10f)
    
    Scaffold(
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, spotColor = FigmaShadowColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF05204A).copy(alpha = 0.85f),
                                        Color(0xFF084A9E).copy(alpha = 0.85f)
                                    )
                                )
                            )
                    ) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Suivi mes Tournées",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { navController.navigate(Screen.Home.route) },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Retour",
                                        tint = PureWhite
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { /* Rafraîchir */ },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Rafraîchir",
                                        tint = PureWhite
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = PureWhite
                            )
                        )
                    }
                }
            }
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TourneeProgressionScreen dans une Card blanche
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    TourneeProgressionScreen()
                }
            }
        }
    }
}

