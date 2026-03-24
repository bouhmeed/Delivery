package com.example.delivery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.ShipmentSearchData
import com.example.delivery.models.ShipmentSearchState
import com.example.delivery.models.ScannerState
import com.example.delivery.repository.ShipmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShipmentSearchViewModel : ViewModel() {
    
    private val repository = ShipmentRepository()
    
    // États
    private val _searchState = MutableStateFlow<ShipmentSearchState>(ShipmentSearchState.Idle)
    val searchState: StateFlow<ShipmentSearchState> = _searchState.asStateFlow()
    
    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()
    
    private val _manualInput = MutableStateFlow("")
    val manualInput: StateFlow<String> = _manualInput.asStateFlow()
    
    // Driver ID (sera injecté depuis le HomeScreen)
    private var currentDriverId: Int = 0
    
    fun setDriverId(driverId: Int) {
        currentDriverId = driverId
    }
    
    /**
     * Scanner un code-barres
     */
    fun scanBarcode(barcode: String) {
        if (barcode.isBlank()) {
            _searchState.value = ShipmentSearchState.Error("Le code-barres ne peut pas être vide")
            return
        }
        
        _searchState.value = ShipmentSearchState.Loading
        
        viewModelScope.launch {
            repository.searchShipment(barcode, currentDriverId)
                .onSuccess { response ->
                    if (response.success && response.data != null) {
                        _searchState.value = ShipmentSearchState.Success(response.data)
                    } else {
                        _searchState.value = ShipmentSearchState.Error(
                            response.message ?: "Erreur lors de la recherche"
                        )
                    }
                }
                .onFailure { error ->
                    _searchState.value = ShipmentSearchState.Error(
                        error.message ?: "Erreur réseau"
                    )
                }
        }
    }
    
    /**
     * Recherche manuelle
     */
    fun searchManually(input: String) {
        if (input.isBlank()) {
            _searchState.value = ShipmentSearchState.Error("Veuillez entrer un code-barres ou numéro de suivi")
            return
        }
        
        _searchState.value = ShipmentSearchState.Loading
        
        viewModelScope.launch {
            // D'abord essayer par code-barres
            repository.searchShipment(input, currentDriverId)
                .onSuccess { response ->
                    if (response.success && response.data != null) {
                        _searchState.value = ShipmentSearchState.Success(response.data)
                    } else {
                        // Si échec, essayer par numéro de suivi
                        repository.searchByTrackingNumber(input, currentDriverId)
                            .onSuccess { trackingResponse ->
                                if (trackingResponse.success && trackingResponse.data != null) {
                                    _searchState.value = ShipmentSearchState.Success(trackingResponse.data)
                                } else {
                                    _searchState.value = ShipmentSearchState.Error(
                                        trackingResponse.message ?: "Colis introuvable dans le système"
                                    )
                                }
                            }
                            .onFailure { error ->
                                _searchState.value = ShipmentSearchState.Error(
                                    error.message ?: "Erreur lors de la recherche"
                                )
                            }
                    }
                }
                .onFailure { error ->
                    _searchState.value = ShipmentSearchState.Error(
                        error.message ?: "Erreur réseau"
                    )
                }
        }
    }
    
    /**
     * Mettre à jour l'entrée manuelle
     */
    fun updateManualInput(input: String) {
        _manualInput.value = input
    }
    
    /**
     * Effacer l'état de recherche
     */
    fun clearSearchState() {
        _searchState.value = ShipmentSearchState.Idle
        _manualInput.value = ""
    }
    
    /**
     * Marquer une livraison comme complétée
     */
    fun markAsDelivered(shipmentId: Int) {
        _searchState.value = ShipmentSearchState.Loading
        
        viewModelScope.launch {
            repository.markAsDelivered(shipmentId, currentDriverId)
                .onSuccess { response ->
                    if (response.success) {
                        // Recharger les données ou marquer comme succès
                        _searchState.value = ShipmentSearchState.Error(
                            "Livraison marquée comme complétée avec succès"
                        )
                    } else {
                        _searchState.value = ShipmentSearchState.Error(
                            response.message ?: "Erreur lors de la mise à jour"
                        )
                    }
                }
                .onFailure { error ->
                    _searchState.value = ShipmentSearchState.Error(
                        error.message ?: "Erreur réseau"
                    )
                }
        }
    }
}
