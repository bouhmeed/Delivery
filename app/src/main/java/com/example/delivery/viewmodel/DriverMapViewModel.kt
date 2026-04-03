package com.example.delivery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.DeliveryItem
import com.example.delivery.services.NavigationService
import com.example.delivery.services.NavigationState
import com.example.delivery.services.RouteInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Driver Map Screen
 * Manages navigation state, route calculation, and map interactions
 */
class DriverMapViewModel : ViewModel() {
    
    // Navigation service instance
    private var navigationService: NavigationService? = null
    
    // Current delivery destination
    private val _currentDelivery = MutableStateFlow<DeliveryItem?>(null)
    val currentDelivery: StateFlow<DeliveryItem?> = _currentDelivery.asStateFlow()
    
    // Navigation state
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Idle)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    // Route information
    private val _routeInfo = MutableStateFlow<RouteInfo?>(null)
    val routeInfo: StateFlow<RouteInfo?> = _routeInfo.asStateFlow()
    
    // Error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Initialize navigation service
     */
    fun initializeNavigation(service: NavigationService) {
        try {
            navigationService = service
            
            // Collect navigation state from service
            viewModelScope.launch {
                service.navigationState.collect { state ->
                    _navigationState.value = state
                    
                    when (state) {
                        is NavigationState.Error -> {
                            _errorMessage.value = state.message
                            _isLoading.value = false
                        }
                        NavigationState.CalculatingRoute -> {
                            _isLoading.value = true
                            _errorMessage.value = null
                        }
                        NavigationState.RouteReady -> {
                            _isLoading.value = false
                            _routeInfo.value = service.getRouteInfo()
                        }
                        else -> {
                            _isLoading.value = false
                        }
                    }
                }
            }
            
            // Collect error messages
            viewModelScope.launch {
                service.error.collect { error ->
                    _errorMessage.value = error
                }
            }
            
        } catch (e: Exception) {
            _errorMessage.value = "Erreur d'initialisation de la navigation: ${e.message}"
        }
    }
    
    /**
     * Set delivery destination and start navigation
     */
    fun setDeliveryDestination(delivery: DeliveryItem) {
        try {
            _currentDelivery.value = delivery
            _errorMessage.value = null
            
            // Extract coordinates from delivery
            val destinationLat = delivery.latitude
            val destinationLng = delivery.longitude
            
            if (destinationLat == null || destinationLng == null) {
                _errorMessage.value = "Coordonnées de destination non disponibles"
                return
            }
            
            // Calculate route to destination
            navigationService?.calculateRoute(destinationLat, destinationLng)
            
            // Add destination marker
            navigationService?.addDestinationMarker(
                latitude = destinationLat,
                longitude = destinationLng,
                title = delivery.fullAddress ?: "Destination"
            )
            
        } catch (e: Exception) {
            _errorMessage.value = "Erreur de configuration de la destination: ${e.message}"
        }
    }
    
    /**
     * Start navigation to current delivery
     */
    fun startNavigation() {
        val delivery = _currentDelivery.value
        if (delivery != null) {
            setDeliveryDestination(delivery)
        } else {
            _errorMessage.value = "Aucune destination sélectionnée"
        }
    }
    
    /**
     * Clear current route
     */
    fun clearRoute() {
        try {
            navigationService?.clearRoute()
            _routeInfo.value = null
            _currentDelivery.value = null
            _errorMessage.value = null
        } catch (e: Exception) {
            _errorMessage.value = "Erreur d'annulation de l'itinéraire: ${e.message}"
        }
    }
    
    /**
     * Stop navigation completely
     */
    fun stopNavigation() {
        try {
            navigationService?.stopNavigation()
            _routeInfo.value = null
            _currentDelivery.value = null
            _errorMessage.value = null
            _isLoading.value = false
        } catch (e: Exception) {
            _errorMessage.value = "Erreur d'arrêt de la navigation: ${e.message}"
        }
    }
    
    /**
     * Retry navigation calculation
     */
    fun retryNavigation() {
        _errorMessage.value = null
        val delivery = _currentDelivery.value
        if (delivery != null) {
            setDeliveryDestination(delivery)
        }
    }
    
    /**
     * Get formatted distance text
     */
    fun getDistanceText(): String {
        return _routeInfo.value?.formatDistance() ?: "N/A"
    }
    
    /**
     * Get formatted time text
     */
    fun getTimeText(): String {
        return _routeInfo.value?.formatTime() ?: "N/A"
    }
    
    /**
     * Check if navigation is active
     */
    fun isNavigationActive(): Boolean {
        return _navigationState.value is NavigationState.RouteReady ||
               _navigationState.value is NavigationState.CalculatingRoute
    }
    
    /**
     * Get current delivery address
     */
    fun getCurrentDeliveryAddress(): String {
        return _currentDelivery.value?.fullAddress ?: "Aucune destination"
    }
    
    /**
     * Get navigation status text
     */
    fun getNavigationStatusText(): String {
        return when (_navigationState.value) {
            is NavigationState.Idle -> "Navigation inactive"
            is NavigationState.MapReady -> "Carte prête"
            is NavigationState.TrackingLocation -> "Localisation en cours"
            is NavigationState.CalculatingRoute -> "Calcul de l'itinéraire..."
            is NavigationState.RouteReady -> "Itinéraire prêt"
            is NavigationState.Error -> "Erreur de navigation"
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        // Cleanup navigation service
        navigationService?.stopNavigation()
    }
}
