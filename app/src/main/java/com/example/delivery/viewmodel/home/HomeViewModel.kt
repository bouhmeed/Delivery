// HomeViewModel.kt – handles data loading for HomeScreen
package com.example.delivery.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.user.UserResponse
import com.example.delivery.models.driver.Driver
import com.example.delivery.models.vehicle.Vehicle
import com.example.delivery.models.vehicle.VehicleMaintenance
import com.example.delivery.models.vehicle.MaintenanceAlert
import com.example.delivery.repository.user.DirectUserRepository
import com.example.delivery.repository.driver.DirectDriverRepository
import com.example.delivery.repository.vehicle.DirectVehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

// UI state sealed interface
sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Success(
        val userInfo: UserResponse?,
        val driverInfo: Driver?,
        val vehicleInfo: Vehicle?,
        val vehicleMaintenance: List<VehicleMaintenance>,
        val maintenanceAlert: MaintenanceAlert?
    ) : HomeUiState
}

class HomeViewModel : ViewModel() {
    // Unread notifications count
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> get() = _unreadCount

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun loadData(email: String) {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            try {
                // 1️⃣ Fetch user
                val userRepo = DirectUserRepository()
                val userResult = userRepo.getUserByEmail(email)
                if (userResult.isFailure) {
                    _uiState.value = HomeUiState.Error("Erreur lors de la récupération de l'utilisateur: ${userResult.exceptionOrNull()?.message}")
                    return@launch
                }
                val user = userResult.getOrNull()
                Log.d("HomeViewModel", "User fetched: ${user?.id}, driverId: ${user?.driverId}")

                var driver: Driver? = null
                var vehicle: Vehicle? = null
                var maintenanceList: List<VehicleMaintenance> = emptyList()
                var alert: MaintenanceAlert? = null

                // 2️⃣ If user has driverId, fetch driver & vehicle
                user?.driverId?.let { driverIdStr ->
                    Log.d("HomeViewModel", "📞 User has driverId: $driverIdStr, fetching driver...")
                    try {
                        val driverId = driverIdStr.toInt()
                        val driverRepo = DirectDriverRepository()
                        val driverResult = driverRepo.getDriverById(driverIdStr)
                        if (driverResult.isSuccess) {
                            driver = driverResult.getOrNull()
                            Log.d("HomeViewModel", "✅ Driver fetched: ${driver?.id}")
                        } else {
                            Log.e("HomeViewModel", "❌ Failed to fetch driver: ${driverResult.exceptionOrNull()?.message}")
                        }
                        // Vehicle
                        val vehicleRepo = DirectVehicleRepository()
                        val vehicleResult = vehicleRepo.getVehicleByDriverId(driverIdStr)
                        if (vehicleResult.isSuccess) {
                            vehicle = vehicleResult.getOrNull()
                            Log.d("HomeViewModel", "✅ Vehicle fetched: ${vehicle?.id}")
                        }
                        // Maintenance
                        vehicle?.id?.let { vId ->
                            val maintResult = vehicleRepo.getVehicleMaintenance(vId.toString())
                            if (maintResult.isSuccess) {
                                maintenanceList = maintResult.getOrNull() ?: emptyList()
                                alert = vehicleRepo.calculateMaintenanceAlert(maintenanceList)
                                vehicle?.let { v ->
    alert = alert?.copy(vehicleName = v.name, registration = v.registration)
}
                            }
                        }
                    } catch (e: NumberFormatException) {
                        Log.e("HomeViewModel", "Invalid driverId format: $driverIdStr", e)
                    }
                } ?: Log.w("HomeViewModel", "⚠️ User does not have a driverId")

                _uiState.value = HomeUiState.Success(
                    userInfo = user,
                    driverInfo = driver,
                    vehicleInfo = vehicle,
                    vehicleMaintenance = maintenanceList,
                    maintenanceAlert = alert
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching user", e)
                _uiState.value = HomeUiState.Error("Erreur inattendue : ${e.message}")
            }
        }
    }

    // Load unread notification count (placeholder implementation)
    fun loadUnreadCount() {
        // TODO: Replace with actual repository call to fetch unread count
        viewModelScope.launch {
            // Simulate fetching count, set to 0 for now
            _unreadCount.value = 0
        }
    }


}
