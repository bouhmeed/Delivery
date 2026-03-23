package com.example.delivery.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.delivery.network.ApiClient
import com.example.delivery.network.TripApiService
import com.example.delivery.models.Trip
import android.widget.Toast
import kotlinx.coroutines.launch
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripTestScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tripApiService = ApiClient.instance.create(TripApiService::class.java)
    
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Test Table Trip",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            val response: Response<List<Trip>> = tripApiService.getAllTrips()
                            if (response.isSuccessful) {
                                trips = response.body() ?: emptyList()
                                testResult = "✅ Succès: ${trips.size} trajets trouvés"
                                Toast.makeText(context, testResult, Toast.LENGTH_SHORT).show()
                            } else {
                                testResult = "❌ Erreur: ${response.code()}"
                                Toast.makeText(context, testResult, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            testResult = "❌ Exception: ${e.message}"
                            Toast.makeText(context, testResult, Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text("Tous les trajets")
            }
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            val response: Response<List<Trip>> = tripApiService.getTripsByDriver("3")
                            if (response.isSuccessful) {
                                val driverTrips = response.body() ?: emptyList()
                                testResult = "✅ Chauffeur 3: ${driverTrips.size} trajets"
                                Toast.makeText(context, testResult, Toast.LENGTH_SHORT).show()
                            } else {
                                testResult = "❌ Erreur: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            testResult = "❌ Exception: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text("Chauffeur 3")
            }
        }
        
        if (testResult.isNotEmpty()) {
            Text(
                text = testResult,
                style = MaterialTheme.typography.bodyMedium,
                color = if (testResult.startsWith("✅")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
        
        if (isLoading) {
            CircularProgressIndicator()
        }
        
        if (trips.isNotEmpty()) {
            Text(
                text = "Résultats (${trips.size}):",
                style = MaterialTheme.typography.titleMedium
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trips.take(10)) { trip ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text("ID: ${trip.id}")
                            Text("Date: ${trip.tripDate}")
                            Text("Chauffeur: ${trip.driverId}")
                            Text("Véhicule: ${trip.vehicleId}")
                            Text("Statut: ${trip.status}")
                            Text("Trip ID: ${trip.tripId ?: "N/A"}")
                        }
                    }
                }
            }
        }
    }
}
