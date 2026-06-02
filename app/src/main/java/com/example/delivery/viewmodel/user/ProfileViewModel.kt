package com.example.delivery.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.user.ProfileResponse
import com.example.delivery.models.user.DriverStatsSummary
import com.example.delivery.models.user.UserResponse
import com.example.delivery.models.user.DriverProfile
import com.example.delivery.models.vehicle.VehicleMaintenance
import com.example.delivery.repository.user.DirectProfileRepository
import com.example.delivery.repository.user.DirectUserRepository
import com.example.delivery.repository.vehicle.DirectVehicleRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data class Success(
        val userInfo: UserResponse?,
        val profile: ProfileResponse?,
        val stats: DriverStatsSummary?,
        val maintenance: List<VehicleMaintenance>
    ) : ProfileUiState
}

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val directUserRepo = DirectUserRepository()
    private val directProfileRepo = DirectProfileRepository()
    private val directVehicleRepo = DirectVehicleRepository()

    fun loadProfileData(email: String) {
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            try {
                // 1. Get user info
                val userResult = directUserRepo.getUserByEmail(email)
                if (userResult.isFailure) {
                    _uiState.value = ProfileUiState.Error("Erreur utilisateur: ${userResult.exceptionOrNull()?.message}")
                    return@launch
                }
                
                val user = userResult.getOrNull()
                val driverId = user?.driverId
                if (driverId == null) {
                    _uiState.value = ProfileUiState.Error("Aucun driver ID trouvé pour cet utilisateur.")
                    return@launch
                }

                // 2. Fetch all other data concurrently using async
                val profileDeferred = async { directProfileRepo.getDriverProfile(driverId.toIntOrNull() ?: 5) }
                val statsDeferred = async { directProfileRepo.getDriverStats(driverId.toIntOrNull() ?: 5) }
                val vehicleDeferred = async { directVehicleRepo.getVehicleByDriverId(driverId) }

                val profileResult = profileDeferred.await()
                val statsResult = statsDeferred.await()
                val vehicleResult = vehicleDeferred.await()

                var maintenanceList: List<VehicleMaintenance> = emptyList()

                // If vehicle exists, fetch maintenance
                if (vehicleResult.isSuccess) {
                    val vehicle = vehicleResult.getOrNull()
                    vehicle?.id?.let { vehicleId ->
                        val maintResult = directVehicleRepo.getVehicleMaintenance(vehicleId.toString())
                        if (maintResult.isSuccess) {
                            maintenanceList = maintResult.getOrNull() ?: emptyList()
                        }
                    }
                }

                if (profileResult.isSuccess && statsResult.isSuccess) {
                    _uiState.value = ProfileUiState.Success(
                        userInfo = user,
                        profile = profileResult.getOrNull(),
                        stats = statsResult.getOrNull(),
                        maintenance = maintenanceList
                    )
                } else {
                    val errorMsg = profileResult.exceptionOrNull()?.message ?: statsResult.exceptionOrNull()?.message ?: "Erreur de chargement"
                    _uiState.value = ProfileUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Erreur: ${e.message}")
            }
        }
    }

    fun updateProfile(driverId: String, updatedProfile: DriverProfile, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val driverIdInt = driverId.toIntOrNull() ?: 5
                val result = directProfileRepo.updateDriverProfile(driverIdInt, updatedProfile)
                if (result.isSuccess) {
                    onResult(true, null)
                } else {
                    onResult(false, result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
