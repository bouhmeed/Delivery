package com.example.delivery.viewmodel.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.repository.delivery.VehicleMaintenanceInfo
import com.example.delivery.repository.delivery.VehicleMaintenanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

sealed interface VehicleMaintenanceState {
    object Loading : VehicleMaintenanceState
    data class Success(val maintenanceInfo: VehicleMaintenanceInfo) : VehicleMaintenanceState
    data class Error(val message: String) : VehicleMaintenanceState
}

class VehicleMaintenanceViewModel : ViewModel() {
    private val repository = VehicleMaintenanceRepository()
    private val TAG = "VehicleMaintenanceViewModel"
    
    private val _state = MutableStateFlow<VehicleMaintenanceState>(VehicleMaintenanceState.Loading)
    val state: StateFlow<VehicleMaintenanceState> = _state
    
    fun loadMaintenanceInfo(driverId: Int) {
        Log.d(TAG, "🔄 loadMaintenanceInfo called with driverId: $driverId")
        viewModelScope.launch {
            _state.value = VehicleMaintenanceState.Loading
            Log.d(TAG, "⏳ State set to Loading")
            
            repository.getVehicleMaintenanceInfo(driverId)
                .onSuccess { maintenanceInfo ->
                    Log.d(TAG, "✅ Success - hasMaintenance: ${maintenanceInfo.hasMaintenance}, isUrgent: ${maintenanceInfo.isUrgent}")
                    _state.value = VehicleMaintenanceState.Success(maintenanceInfo)
                    Log.d(TAG, "✅ State set to Success")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Error: ${error.message}", error)
                    _state.value = VehicleMaintenanceState.Error(error.message ?: "Unknown error")
                    Log.d(TAG, "✅ State set to Error")
                }
        }
    }
}
