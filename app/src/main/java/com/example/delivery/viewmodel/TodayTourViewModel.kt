package com.example.delivery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.TodayTourResponse
import com.example.delivery.models.TourInfo
import com.example.delivery.models.TourStatistics
import com.example.delivery.models.ApiResponse
import com.example.delivery.models.Shipment
import com.example.delivery.repository.TodayTourRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TodayTourViewModel(
    private val repository: TodayTourRepository = TodayTourRepository()
) : ViewModel() {
    
    private val _todayTourState = MutableStateFlow<TodayTourState>(TodayTourState.Loading)
    val todayTourState: StateFlow<TodayTourState> = _todayTourState.asStateFlow()
    
    private val _currentDriverId = MutableStateFlow<Int?>(null)
    val currentDriverId: StateFlow<Int?> = _currentDriverId.asStateFlow()
    
    fun setDriverId(driverId: Int) {
        _currentDriverId.value = driverId
        loadTodayTour()
    }
    
    fun loadTodayTour() {
        val driverId = _currentDriverId.value ?: return
        
        _todayTourState.value = TodayTourState.Loading
        
        viewModelScope.launch {
            repository.getTodayTour(driverId).fold(
                onSuccess = { response ->
                    if (response.success) {
                        if (response.data.hasTour && response.data.tour != null && response.data.statistics != null) {
                            _todayTourState.value = TodayTourState.Success(
                                tourInfo = response.data.tour!!,
                                statistics = response.data.statistics!!,
                                shipments = response.data.shipments
                            )
                        } else {
                            _todayTourState.value = TodayTourState.NoTour(
                                message = response.data.message ?: "Aucune tournée prévue pour aujourd'hui"
                            )
                        }
                    } else {
                        _todayTourState.value = TodayTourState.Error(
                            message = response.error ?: "Erreur inconnue"
                        )
                    }
                },
                onFailure = { error ->
                    _todayTourState.value = TodayTourState.Error(
                        message = error.message ?: "Erreur de connexion"
                    )
                }
            )
        }
    }
    
    fun completeShipment(shipmentId: Int) {
        viewModelScope.launch {
            repository.completeShipment(shipmentId).fold(
                onSuccess = { response ->
                    if (response.success) {
                        // Recharger les données pour mettre à jour les statistiques
                        loadTodayTour()
                    }
                },
                onFailure = { error ->
                    _todayTourState.value = TodayTourState.Error(
                        message = "Erreur lors de la mise à jour: ${error.message}"
                    )
                }
            )
        }
    }
    
    fun refresh() {
        loadTodayTour()
    }
}

sealed class TodayTourState {
    object Loading : TodayTourState()
    
    data class Success(
        val tourInfo: TourInfo,
        val statistics: TourStatistics,
        val shipments: List<com.example.delivery.models.Shipment>
    ) : TodayTourState()
    
    data class NoTour(
        val message: String
    ) : TodayTourState()
    
    data class Error(
        val message: String
    ) : TodayTourState()
}

class TodayTourViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodayTourViewModel::class.java)) {
            return TodayTourViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
