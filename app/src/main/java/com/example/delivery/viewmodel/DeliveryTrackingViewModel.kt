package com.example.delivery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.DeliveryItem
import com.example.delivery.models.Trip
import com.example.delivery.models.TripWithDeliveries
import com.example.delivery.repository.DeliveryTrackingRepository
import com.example.delivery.repository.Result
import com.example.delivery.repository.ShipmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ViewModel for Delivery Tracking Screen
 */
class DeliveryTrackingViewModel : ViewModel() {
    
    private val repository = DeliveryTrackingRepository()
    private val shipmentRepository = ShipmentRepository()
    
    // Selected date state
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    
    private val _selectedTripId = MutableStateFlow<Int?>(null)
    val selectedTripId: StateFlow<Int?> = _selectedTripId.asStateFlow()
    
    // State for trip and deliveries
    private val _tripWithDeliveriesState = MutableStateFlow<TripWithDeliveriesState>(TripWithDeliveriesState.Loading)
    val tripWithDeliveriesState: StateFlow<TripWithDeliveriesState> = _tripWithDeliveriesState.asStateFlow()
    
    // State for individual operations
    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()
    
    // Refresh state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // Shipment dates for calendar
    private val _shipmentDates = MutableStateFlow<List<String>>(emptyList())
    val shipmentDates: StateFlow<List<String>> = _shipmentDates.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    /**
     * Select a trip
     */
    fun selectTrip(tripId: Int) {
        _selectedTripId.value = tripId
    }
    
    /**
     * Set selected date
     */
    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        _selectedTripId.value = null // Reset selected trip when date changes
    }
    
    /**
     * Load trip for selected date
     */
    fun loadTripForDate(driverId: Int, date: LocalDate = _selectedDate.value) {
        viewModelScope.launch {
            repository.getTripForDate(driverId, date.format(dateFormatter)).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _tripWithDeliveriesState.value = TripWithDeliveriesState.Loading
                    }
                    is Result.Success -> {
                        _tripWithDeliveriesState.value = TripWithDeliveriesState.Success(result.data)
                    }
                    is Result.Error -> {
                        _tripWithDeliveriesState.value = TripWithDeliveriesState.Error(result.message)
                    }
                }
            }
        }
    }
    
    /**
     * Load today's trip with deliveries (legacy method)
     */
    fun loadTodayTripWithDeliveries(driverId: Int) {
        loadTripForDate(driverId, LocalDate.now())
    }
    
    /**
     * Go to previous day
     */
    fun goToPreviousDay(driverId: Int) {
        val previousDate = _selectedDate.value.minusDays(1)
        _selectedDate.value = previousDate
        loadTripForDate(driverId, previousDate)
    }
    
    /**
     * Go to next day
     */
    fun goToNextDay(driverId: Int) {
        val nextDate = _selectedDate.value.plusDays(1)
        _selectedDate.value = nextDate
        loadTripForDate(driverId, nextDate)
    }
    
    /**
     * Go to today
     */
    fun goToToday(driverId: Int) {
        val today = LocalDate.now()
        _selectedDate.value = today
        loadTripForDate(driverId, today)
    }
    
    /**
     * Refresh data for current selected date
     */
    fun refresh(driverId: Int) {
        _isRefreshing.value = true
        viewModelScope.launch {
            repository.getTripForDate(driverId, _selectedDate.value.format(dateFormatter)).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _tripWithDeliveriesState.value = TripWithDeliveriesState.Success(result.data)
                    }
                    is Result.Error -> {
                        _tripWithDeliveriesState.value = TripWithDeliveriesState.Error(result.message)
                    }
                    is Result.Loading -> {
                        // Don't change state to loading during refresh
                    }
                }
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * Update delivery status
     */
    fun updateDeliveryStatus(shipmentId: Int, status: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            val result = repository.updateShipmentStatus(shipmentId, status)
            
            when (result) {
                is Result.Success -> {
                    _operationState.value = OperationState.Success("Statut mis à jour")
                    // Refresh data to show changes
                    refresh(0) // We need driverId here
                }
                is Result.Error -> {
                    _operationState.value = OperationState.Error(result.message)
                }
                is Result.Loading -> {
                    // Already in loading state
                }
            }
        }
    }
    
    /**
     * Update delivery status in TripShipmentLink and corresponding Shipment
     * Also triggers Trip auto-completion check if status is TERMINE
     * Uses v2 API endpoint for atomic dual update
     */
    fun updateTripShipmentStatus(tripShipmentLinkId: Int, newStatus: String, driverId: Int? = null) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            try {
                println("🔄 ViewModel: Mise à jour statut livraison TSL=$tripShipmentLinkId -> $newStatus")
                
                // Validate status is allowed for Shipment
                val allowedShipmentStatuses = listOf("TO_PLAN", "EXPEDITION", "DELIVERED")
                if (newStatus !in allowedShipmentStatuses) {
                    println("❌ ViewModel: Statut Shipment invalide: $newStatus")
                    _operationState.value = OperationState.Error("Statut invalide: $newStatus")
                    return@launch
                }
                
                // Use v2 repository method - backend handles dual update (TSL + Shipment) and auto-complete
                val result = shipmentRepository.updateTripShipmentStatusV2(tripShipmentLinkId, newStatus, driverId)
                
                when {
                    result.isSuccess -> {
                        val response = result.getOrNull()
                        val tripAutoCompleted = response?.data?.tripAutoCompleted ?: false
                        
                        println("✅ ViewModel: Statut mis à jour avec succès (tripAutoCompleted=$tripAutoCompleted)")
                        
                        val successMessage = if (tripAutoCompleted) {
                            "Statut mis à jour - Trip auto-complété"
                        } else {
                            "Statut mis à jour avec succès"
                        }
                        
                        _operationState.value = OperationState.Success(successMessage)
                        
                        // Refresh data for current driver if driverId is provided
                        driverId?.let { refresh(it) }
                    }
                    result.isFailure -> {
                        val exception = result.exceptionOrNull()
                        println("❌ ViewModel: Erreur mise à jour statut - ${exception?.message}")
                        _operationState.value = OperationState.Error(
                            exception?.message ?: "Erreur lors de la mise à jour du statut"
                        )
                    }
                }
            } catch (e: Exception) {
                println("❌ ViewModel: Exception lors de la mise à jour du statut - ${e.message}")
                _operationState.value = OperationState.Error(
                    "Erreur lors de la mise à jour du statut: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Complete delivery
     */
    fun completeDelivery(shipmentId: Int) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            val result = repository.completeDelivery(shipmentId)
            
            when (result) {
                is Result.Success -> {
                    _operationState.value = OperationState.Success("Livraison complétée")
                    // Refresh data to show changes
                    refresh(0) // We need driverId here
                }
                is Result.Error -> {
                    _operationState.value = OperationState.Error(result.message)
                }
                is Result.Loading -> {
                    // Already in loading state
                }
            }
        }
    }
    
    /**
     * Clear operation state
     */
    fun clearOperationState() {
        _operationState.value = OperationState.Idle
    }
    
    /**
     * Get delivery statistics from current deliveries - based on TripShipmentLink.status
     */
    private fun getDeliveryStats(deliveries: List<DeliveryItem>): DeliveryStats {
        val total = deliveries.size
        // Completed = DELIVERED
        val completed = deliveries.count { it.status == "DELIVERED" }
        // In progress = EXPEDITION
        val inProgress = deliveries.count { it.status == "EXPEDITION" }
        // Not started = TO_PLAN
        val notStarted = deliveries.count { it.status == "TO_PLAN" }
        
        return DeliveryStats(
            total = total,
            completed = completed,
            inProgress = inProgress,
            notStarted = notStarted,
            completionPercentage = if (total > 0) (completed * 100 / total) else 0
        )
    }
    
    /**
     * Format date for display
     */
    fun formatDateForDisplay(date: LocalDate = _selectedDate.value): String {
        val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return date.format(displayFormatter)
    }
    
    /**
     * Check if selected date is today
     */
    fun isToday(date: LocalDate = _selectedDate.value): Boolean {
        return date.isEqual(LocalDate.now())
    }
    
    /**
     * Check if selected date is in the past
     */
    fun isPast(date: LocalDate = _selectedDate.value): Boolean {
        return date.isBefore(LocalDate.now())
    }
    
    /**
     * Load shipment dates for calendar
     */
    fun loadShipmentDates(driverId: Int) {
        viewModelScope.launch {
            try {
                val result = shipmentRepository.getShipmentDates(driverId)
                when {
                    result.isSuccess -> {
                        val response = result.getOrNull()
                        if (response?.success == true) {
                            _shipmentDates.value = response.data ?: emptyList()
                            println("✅ ViewModel: ${_shipmentDates.value.size} dates d'expéditions chargées")
                        } else {
                            println("⚠️ ViewModel: Erreur chargement dates - ${response?.message}")
                        }
                    }
                    result.isFailure -> {
                        val exception = result.exceptionOrNull()
                        println("❌ ViewModel: Exception chargement dates - ${exception?.message}")
                    }
                }
            } catch (e: Exception) {
                println("❌ ViewModel: Exception loadShipmentDates - ${e.message}")
            }
        }
    }
}

/**
 * Sealed class for TripWithDeliveries state
 */
sealed class TripWithDeliveriesState {
    object Loading : TripWithDeliveriesState()
    data class Success(val data: TripWithDeliveries) : TripWithDeliveriesState()
    data class Error(val message: String) : TripWithDeliveriesState()
    object NoTripToday : TripWithDeliveriesState()
}

/**
 * Sealed class for operation state
 */
sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    data class Success(val message: String) : OperationState()
    data class Error(val message: String) : OperationState()
}

/**
 * Data class for delivery statistics
 */
data class DeliveryStats(
    val total: Int,
    val completed: Int,
    val inProgress: Int,
    val notStarted: Int,
    val completionPercentage: Int
)
