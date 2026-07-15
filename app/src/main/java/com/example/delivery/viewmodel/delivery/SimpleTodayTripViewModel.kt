package com.example.delivery.viewmodel.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.repository.delivery.SimpleTodayTripRepository
import com.example.delivery.repository.delivery.SimpleTripInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

sealed interface SimpleTripState {
    object Loading : SimpleTripState
    data class Success(val tripInfo: SimpleTripInfo) : SimpleTripState
    data class Error(val message: String) : SimpleTripState
}

class SimpleTodayTripViewModel : ViewModel() {
    private val repository = SimpleTodayTripRepository()
    private val TAG = "SimpleTodayTripViewModel"
    
    private val _state = MutableStateFlow<SimpleTripState>(SimpleTripState.Loading)
    val state: StateFlow<SimpleTripState> = _state
    
    fun loadTripInfo(driverId: Int) {
        Log.d(TAG, "🔄 loadTripInfo called with driverId: $driverId")
        viewModelScope.launch {
            _state.value = SimpleTripState.Loading
            Log.d(TAG, "⏳ State set to Loading")
            
            repository.getTodayTripInfo(driverId)
                .onSuccess { tripInfo ->
                    Log.d(TAG, "✅ Success - hasTrip: ${tripInfo.hasTrip}, shipments: ${tripInfo.shipmentCount}, tripNumber: ${tripInfo.tripNumber}")
                    _state.value = SimpleTripState.Success(tripInfo)
                    Log.d(TAG, "✅ State set to Success")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Error: ${error.message}", error)
                    _state.value = SimpleTripState.Error(error.message ?: "Unknown error")
                    Log.d(TAG, "❌ State set to Error")
                }
        }
    }
}
