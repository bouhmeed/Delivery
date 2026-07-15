package com.example.delivery.viewmodel.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.delivery.DeliveryItem
import com.example.delivery.models.driver.Trip
import com.example.delivery.models.delivery.TripWithDeliveries
import com.example.delivery.models.delivery.MultipleTripsWithDeliveries
import com.example.delivery.repository.delivery.DirectDeliveryTrackingRepository
import com.example.delivery.repository.Result
import com.example.delivery.repository.delivery.DirectShipmentRepository
import com.example.delivery.network.api.delivery.StatusUpdateResponseV2
import com.example.delivery.network.api.delivery.StatusUpdateDataV2
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
    
    private val repository = DirectDeliveryTrackingRepository()
    private val shipmentRepository = DirectShipmentRepository()
    
    // Selected date state
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    
    private val _selectedTripId = MutableStateFlow<Int?>(null)
    val selectedTripId: StateFlow<Int?> = _selectedTripId.asStateFlow()
    
    // State for trip and deliveries
    private val _tripWithDeliveriesState = MutableStateFlow<TripWithDeliveriesState>(TripWithDeliveriesState.Loading)
    val tripWithDeliveriesState: StateFlow<TripWithDeliveriesState> = _tripWithDeliveriesState.asStateFlow()
    
    // State for multiple trips with deliveries
    private val _multipleTripsState = MutableStateFlow<MultipleTripsState>(MultipleTripsState.Loading)
    val multipleTripsState: StateFlow<MultipleTripsState> = _multipleTripsState.asStateFlow()
    
    // State for individual operations
    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()
    
    // Refresh state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // Shipment dates for calendar
    private val _shipmentDates = MutableStateFlow<List<String>>(emptyList())
    val shipmentDates: StateFlow<List<String>> = _shipmentDates.asStateFlow()
    
    // Filter state
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()
    
    // Filtered deliveries
    private val _filteredDeliveries = MutableStateFlow<List<DeliveryItem>>(emptyList())
    val filteredDeliveries: StateFlow<List<DeliveryItem>> = _filteredDeliveries.asStateFlow()
    
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
        println("🔍 ViewModel: loadTripForDate called with driverId=$driverId, date=${date.format(dateFormatter)}")
        viewModelScope.launch {
            repository.getTripForDate(driverId, date.format(dateFormatter)).collect { result ->
                println("🔍 ViewModel: Result received - ${result::class.simpleName}")
                when (result) {
                    is Result.Loading -> {
                        println("🔍 ViewModel: Setting state to Loading")
                        _tripWithDeliveriesState.value = TripWithDeliveriesState.Loading
                    }
                    is Result.Success -> {
                        println("🔍 ViewModel: Success - trip=${result.data.trip}, deliveries count=${result.data.deliveries.size}")
                        _tripWithDeliveriesState.value = TripWithDeliveriesState.Success(result.data)
                        applyFilters(result.data.deliveries)
                    }
                    is Result.Error -> {
                        println("🔍 ViewModel: Error - ${result.message}")
                        _tripWithDeliveriesState.value = TripWithDeliveriesState.Error(result.message)
                    }
                }
            }
        }
    }
    
    /**
     * Load all trips for selected date (for multiple trips per day)
     */
    fun loadAllTripsForDate(driverId: Int, date: LocalDate = _selectedDate.value) {
        println("🔍 ViewModel: loadAllTripsForDate called with driverId=$driverId, date=${date.format(dateFormatter)}")
        viewModelScope.launch {
            repository.getAllTripsForDate(driverId, date.format(dateFormatter)).collect { result ->
                println("🔍 ViewModel: Result received - ${result::class.simpleName}")
                when (result) {
                    is Result.Loading -> {
                        println("🔍 ViewModel: Setting multiple trips state to Loading")
                        _multipleTripsState.value = MultipleTripsState.Loading
                    }
                    is Result.Success -> {
                        println("🔍 ViewModel: Success - trips count=${result.data.trips.size}")
                        _multipleTripsState.value = MultipleTripsState.Success(result.data)
                        // Combine all deliveries from all trips for filtering
                        val allDeliveries = result.data.trips.flatMap { it.deliveries }
                        applyFilters(allDeliveries)
                    }
                    is Result.Error -> {
                        println("🔍 ViewModel: Error - ${result.message}")
                        _multipleTripsState.value = MultipleTripsState.Error(result.message)
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
        loadAllTripsForDate(driverId, previousDate)
    }
    
    /**
     * Go to next day
     */
    fun goToNextDay(driverId: Int) {
        val nextDate = _selectedDate.value.plusDays(1)
        _selectedDate.value = nextDate
        loadAllTripsForDate(driverId, nextDate)
    }
    
    /**
     * Go to today
     */
    fun goToToday(driverId: Int) {
        val today = LocalDate.now()
        _selectedDate.value = today
        loadAllTripsForDate(driverId, today)
    }
    
    /**
     * Refresh data for current selected date
     */
    fun refresh(driverId: Int) {
        _isRefreshing.value = true
        // Clear current state to force reload
        _multipleTripsState.value = MultipleTripsState.Loading
        _filteredDeliveries.value = emptyList()
        
        viewModelScope.launch {
            repository.getAllTripsForDate(driverId, _selectedDate.value.format(dateFormatter)).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _multipleTripsState.value = MultipleTripsState.Success(result.data)
                        // Combine all deliveries from all trips for filtering
                        val allDeliveries = result.data.trips.flatMap { it.deliveries }
                        applyFilters(allDeliveries)
                    }
                    is Result.Error -> {
                        _multipleTripsState.value = MultipleTripsState.Error(result.message)
                    }
                    is Result.Loading -> {
                        // Don't change state to loading during refresh (already set above)
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
                val allowedShipmentStatuses = listOf("TO_PLAN", "EXPEDITION", "LIVRE")
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
                        
                        // Note: Refresh is handled by UI layer via LaunchedEffect(operationState)
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
        // Completed = LIVRE or DELIVERED (for backward compatibility)
        val completed = deliveries.count { it.status == "LIVRE" || it.status == "DELIVERED" }
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
    
    /**
     * Update filter state
     */
    fun updateFilterState(newState: FilterState) {
        _filterState.value = newState
        // Re-apply filters with new state
        tripWithDeliveriesState.value.let { state ->
            if (state is TripWithDeliveriesState.Success) {
                applyFilters(state.data.deliveries)
            }
        }
    }
    
    /**
     * Update selected statuses filter
     */
    fun updateStatusFilter(statuses: Set<String>) {
        _filterState.value = _filterState.value.copy(selectedStatuses = statuses)
        reapplyFilters()
    }
    
    /**
     * Update selected types filter
     */
    fun updateTypeFilter(types: Set<String>) {
        _filterState.value = _filterState.value.copy(selectedTypes = types)
        reapplyFilters()
    }
    
    /**
     * Update customer search query
     */
    fun updateCustomerQuery(query: String) {
        _filterState.value = _filterState.value.copy(customerQuery = query)
        reapplyFilters()
    }
    
    /**
     * Update sort option
     */
    fun updateSortOption(sortOption: SortOption) {
        _filterState.value = _filterState.value.copy(sortBy = sortOption)
        reapplyFilters()
    }
    
    /**
     * Update sort order
     */
    fun updateSortOrder(sortOrder: SortOrder) {
        _filterState.value = _filterState.value.copy(sortOrder = sortOrder)
        reapplyFilters()
    }
    
    /**
     * Clear all filters
     */
    fun clearFilters() {
        _filterState.value = FilterState()
        reapplyFilters()
    }
    
    /**
     * Reapply current filters to current deliveries
     */
    private fun reapplyFilters() {
        multipleTripsState.value.let { state ->
            if (state is MultipleTripsState.Success) {
                val allDeliveries = state.data.trips.flatMap { it.deliveries }
                applyFilters(allDeliveries)
            }
        }
    }
    
    /**
     * Apply filters and sorting to deliveries list
     */
    private fun applyFilters(deliveries: List<DeliveryItem>) {
        val filtered = deliveries.filter { delivery ->
            // Hide TO_PLAN shipments from UI
            val notToPlan = delivery.status != "TO_PLAN"
            
            // Status filter
            val statusMatch = _filterState.value.selectedStatuses.isEmpty() || 
                delivery.status in _filterState.value.selectedStatuses
            
            // Type filter
            val typeMatch = _filterState.value.selectedTypes.isEmpty() || 
                (delivery.type != null && delivery.type in _filterState.value.selectedTypes)
            
            // Customer search filter
            val customerMatch = _filterState.value.customerQuery.isBlank() || 
                (delivery.clientName?.contains(_filterState.value.customerQuery, ignoreCase = true) == true)
            
            notToPlan && statusMatch && typeMatch && customerMatch
        }.sortedWith(compareBy<DeliveryItem> { delivery ->
            when (_filterState.value.sortBy) {
                SortOption.DISTANCE -> delivery.distanceKm ?: Double.MAX_VALUE
                SortOption.QUANTITY -> delivery.quantity.toDouble()
                SortOption.DURATION -> (delivery.estimatedDuration ?: 0).toDouble()
                SortOption.SEQUENCE -> delivery.sequence.toDouble()
            }
        }.let { comparator ->
            if (_filterState.value.sortOrder == SortOrder.DESC) comparator.reversed() else comparator
        })
        
        _filteredDeliveries.value = filtered
    }
}

/**
 * Filter state data class
 */
data class FilterState(
    val selectedStatuses: Set<String> = emptySet(),
    val selectedTypes: Set<String> = emptySet(),
    val customerQuery: String = "",
    val sortBy: SortOption = SortOption.DISTANCE,
    val sortOrder: SortOrder = SortOrder.ASC
)

/**
 * Sort options enum
 */
enum class SortOption {
    SEQUENCE, DISTANCE, QUANTITY, DURATION
}

/**
 * Sort order enum
 */
enum class SortOrder {
    ASC, DESC
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
 * Sealed class for MultipleTripsWithDeliveries state
 */
sealed class MultipleTripsState {
    object Loading : MultipleTripsState()
    data class Success(val data: MultipleTripsWithDeliveries) : MultipleTripsState()
    data class Error(val message: String) : MultipleTripsState()
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
