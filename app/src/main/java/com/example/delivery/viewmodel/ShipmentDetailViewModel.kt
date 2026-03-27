package com.example.delivery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.ShipmentDetailFull
import com.example.delivery.repository.ShipmentDetailRepository
import com.example.delivery.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour les détails d'une livraison
 */
class ShipmentDetailViewModel : ViewModel() {
    
    private val repository = ShipmentDetailRepository()
    
    // État pour les détails de la livraison
    private val _shipmentDetailState = MutableStateFlow<ShipmentDetailState>(ShipmentDetailState.Loading)
    val shipmentDetailState: StateFlow<ShipmentDetailState> = _shipmentDetailState.asStateFlow()
    
    // État pour les opérations (mise à jour statut, complétion)
    private val _operationState = MutableStateFlow<ShipmentOperationState?>(null)
    val operationState: StateFlow<ShipmentOperationState?> = _operationState.asStateFlow()
    
    /**
     * Charger les détails complets d'une livraison
     */
    fun loadShipmentDetails(shipmentId: Int) {
        viewModelScope.launch {
            repository.getShipmentDetails(shipmentId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _shipmentDetailState.value = ShipmentDetailState.Loading
                    }
                    is Result.Success -> {
                        _shipmentDetailState.value = ShipmentDetailState.Success(result.data)
                    }
                    is Result.Error -> {
                        _shipmentDetailState.value = ShipmentDetailState.Error(result.message)
                    }
                }
            }
        }
    }
    
    /**
     * Mettre à jour le statut d'une livraison
     */
    fun updateShipmentStatus(shipmentId: Int, status: String) {
        viewModelScope.launch {
            _operationState.value = ShipmentOperationState.Loading
            
            val result = repository.updateShipmentStatus(shipmentId, status)
            
            when (result) {
                is Result.Success -> {
                    _operationState.value = ShipmentOperationState.Success(result.data)
                }
                is Result.Error -> {
                    _operationState.value = ShipmentOperationState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    /**
     * Marquer une livraison comme complétée (POD done)
     */
    fun completeShipment(shipmentId: Int) {
        viewModelScope.launch {
            _operationState.value = ShipmentOperationState.Loading
            
            val result = repository.completeShipment(shipmentId)
            
            when (result) {
                is Result.Success -> {
                    _operationState.value = ShipmentOperationState.Success(result.data)
                }
                is Result.Error -> {
                    _operationState.value = ShipmentOperationState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    /**
     * Effacer l'état d'opération
     */
    fun clearOperationState() {
        _operationState.value = null
    }
}

/**
 * État sealed class pour les détails de livraison
 */
sealed class ShipmentDetailState {
    object Loading : ShipmentDetailState()
    data class Success(val data: ShipmentDetailFull) : ShipmentDetailState()
    data class Error(val message: String) : ShipmentDetailState()
}

/**
 * État sealed class pour les opérations
 */
sealed class ShipmentOperationState {
    object Loading : ShipmentOperationState()
    data class Success(val message: String) : ShipmentOperationState()
    data class Error(val message: String) : ShipmentOperationState()
}
