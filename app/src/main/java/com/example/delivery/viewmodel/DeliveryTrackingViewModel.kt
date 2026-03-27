package com.example.delivery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.DeliveryItem
import com.example.delivery.models.Trip
import com.example.delivery.models.TripWithDeliveries
import com.example.delivery.repository.DeliveryTrackingRepository
import com.example.delivery.repository.Result
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
     * Get delivery statistics
     */
    fun getDeliveryStats(deliveries: List<DeliveryItem>): DeliveryStats {
        val total = deliveries.size
        val completed = deliveries.count { it.podDone }
        val inProgress = deliveries.count { !it.podDone && it.status == "EN_COURS" }
        val notStarted = deliveries.count { !it.podDone && it.status == "NON_DEMARRE" }
        
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
