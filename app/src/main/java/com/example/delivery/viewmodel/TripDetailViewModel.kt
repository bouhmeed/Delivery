package com.example.delivery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.*
import com.example.delivery.network.ApiClient
import com.example.delivery.network.TripDetailApiService
import com.example.delivery.network.DeliverShipmentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TripDetailViewModel(
    private val apiService: TripDetailApiService = ApiClient.getRetrofit().create(TripDetailApiService::class.java)
) : ViewModel() {
    
    // États pour les détails du trajet
    private val _tripDetailState = MutableStateFlow<TripDetailState>(TripDetailState.Idle)
    val tripDetailState: StateFlow<TripDetailState> = _tripDetailState.asStateFlow()
    
    // États pour la liste des trajets
    private val _tripListState = MutableStateFlow<TripListState>(TripListState.Idle)
    val tripListState: StateFlow<TripListState> = _tripListState.asStateFlow()
    
    // États pour les filtres
    private val _dateFilter = MutableStateFlow(TripDateFilter.TODAY)
    val dateFilter: StateFlow<TripDateFilter> = _dateFilter.asStateFlow()
    
    private val _statusFilter = MutableStateFlow(TripStatusFilter.ALL)
    val statusFilter: StateFlow<TripStatusFilter> = _statusFilter.asStateFlow()
    
    // Driver ID courant
    private var currentDriverId: Int? = null
    
    // Pagination
    private var currentPage = 1
    private val pageSize = 20
    private var canLoadMore = true
    
    fun setDriverId(driverId: Int) {
        currentDriverId = driverId
        // Recharger les données avec le nouveau driverId
        loadTripList()
    }
    
    // Charger les détails d'un trajet spécifique
    fun loadTripDetails(tripId: Int) {
        if (tripId <= 0) {
            _tripDetailState.value = TripDetailState.Error("ID de trajet invalide")
            return
        }
        
        _tripDetailState.value = TripDetailState.Loading
        
        viewModelScope.launch {
            try {
                val response = apiService.getTripDetails(tripId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _tripDetailState.value = TripDetailState.Success(body.data)
                    } else {
                        _tripDetailState.value = TripDetailState.Error(
                            body.error ?: "Erreur lors du chargement des détails"
                        )
                    }
                } else {
                    _tripDetailState.value = TripDetailState.Error(
                        "Erreur serveur: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _tripDetailState.value = TripDetailState.Error(
                    "Erreur de connexion: ${e.message}"
                )
            }
        }
    }
    
    // Charger la liste des trajets avec filtres
    fun loadTripList(refresh: Boolean = false) {
        val driverId = currentDriverId ?: return
        
        if (refresh) {
            currentPage = 1
            canLoadMore = true
            _tripListState.value = TripListState.Loading
        } else if (_tripListState.value is TripListState.Loading) {
            return // Déjà en chargement
        }
        
        if (!canLoadMore && !refresh) {
            return // Plus de données à charger
        }
        
        viewModelScope.launch {
            try {
                val dateFilterValue = when (_dateFilter.value) {
                    TripDateFilter.TODAY -> LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    TripDateFilter.UPCOMING -> "upcoming"
                    TripDateFilter.PAST -> "past"
                    TripDateFilter.ALL -> null
                    else -> null
                }
                
                val statusFilterValue = when (_statusFilter.value) {
                    TripStatusFilter.ALL -> null
                    else -> _statusFilter.value.name.lowercase()
                }
                
                val response = apiService.getTrips(
                    driverId = driverId,
                    dateFilter = dateFilterValue,
                    statusFilter = statusFilterValue,
                    page = currentPage,
                    limit = pageSize
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        val currentData = if (refresh) emptyList() else {
                            (_tripListState.value as? TripListState.Success)?.trips ?: emptyList()
                        }
                        
                        val newTrips = if (refresh) {
                            body.data.trips
                        } else {
                            currentData + body.data.trips
                        }
                        
                        _tripListState.value = TripListState.Success(
                            trips = newTrips,
                            totalCount = body.data.totalCount,
                            currentPage = body.data.currentPage,
                            totalPages = body.data.totalPages
                        )
                        
                        currentPage++
                        canLoadMore = body.data.currentPage < body.data.totalPages
                    } else {
                        _tripListState.value = TripListState.Error(
                            body.error ?: "Erreur lors du chargement des trajets"
                        )
                    }
                } else {
                    _tripListState.value = TripListState.Error(
                        "Erreur serveur: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _tripListState.value = TripListState.Error(
                    "Erreur de connexion: ${e.message}"
                )
            }
        }
    }
    
    // Charger les trajets pour une date spécifique
    fun loadTripsForDate(date: LocalDate) {
        val driverId = currentDriverId ?: return
        
        _tripListState.value = TripListState.Loading
        
        viewModelScope.launch {
            try {
                val response = apiService.getTripsByDate(
                    driverId = driverId,
                    date = date.format(DateTimeFormatter.ISO_DATE),
                    page = 1,
                    limit = pageSize
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _tripListState.value = TripListState.Success(
                            trips = body.data.trips,
                            totalCount = body.data.totalCount,
                            currentPage = body.data.currentPage,
                            totalPages = body.data.totalPages
                        )
                    } else {
                        _tripListState.value = TripListState.Error(
                            body.error ?: "Erreur lors du chargement des trajets"
                        )
                    }
                } else {
                    _tripListState.value = TripListState.Error(
                        "Erreur serveur: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _tripListState.value = TripListState.Error(
                    "Erreur de connexion: ${e.message}"
                )
            }
        }
    }
    
    // Mettre à jour les filtres
    fun setDateFilter(filter: TripDateFilter) {
        _dateFilter.value = filter
        loadTripList(refresh = true)
    }
    
    fun setStatusFilter(filter: TripStatusFilter) {
        _statusFilter.value = filter
        loadTripList(refresh = true)
    }
    
    // Actions sur les trajets
    fun startTrip(tripId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.startTrip(tripId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        // Recharger les détails du trajet
                        loadTripDetails(tripId)
                        // Recharger la liste
                        loadTripList(refresh = true)
                    } else {
                        _tripDetailState.value = TripDetailState.Error(
                            "Erreur lors du démarrage: ${body.error}"
                        )
                    }
                } else {
                    _tripDetailState.value = TripDetailState.Error(
                        "Erreur serveur: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _tripDetailState.value = TripDetailState.Error(
                    "Erreur de connexion: ${e.message}"
                )
            }
        }
    }
    
    fun completeTrip(tripId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.completeTrip(tripId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        // Recharger les détails du trajet
                        loadTripDetails(tripId)
                        // Recharger la liste
                        loadTripList(refresh = true)
                    } else {
                        _tripDetailState.value = TripDetailState.Error(
                            "Erreur lors de la complétion: ${body.error}"
                        )
                    }
                } else {
                    _tripDetailState.value = TripDetailState.Error(
                        "Erreur serveur: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _tripDetailState.value = TripDetailState.Error(
                    "Erreur de connexion: ${e.message}"
                )
            }
        }
    }
    
    fun deliverShipment(tripId: Int, shipmentId: Int, request: DeliverShipmentRequest) {
        viewModelScope.launch {
            try {
                val response = apiService.deliverShipment(tripId, shipmentId, request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        // Recharger les détails du trajet
                        loadTripDetails(tripId)
                    } else {
                        _tripDetailState.value = TripDetailState.Error(
                            "Erreur lors de la livraison: ${body.error}"
                        )
                    }
                } else {
                    _tripDetailState.value = TripDetailState.Error(
                        "Erreur serveur: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _tripDetailState.value = TripDetailState.Error(
                    "Erreur de connexion: ${e.message}"
                )
            }
        }
    }
    
    // Réinitialiser les états
    fun clearTripDetails() {
        _tripDetailState.value = TripDetailState.Idle
    }
    
    fun clearTripList() {
        _tripListState.value = TripListState.Idle
        currentPage = 1
        canLoadMore = true
    }
}

// États pour les détails du trajet
sealed class TripDetailState {
    object Idle : TripDetailState()
    object Loading : TripDetailState()
    data class Success(val tripDetail: TripDetailData) : TripDetailState()
    data class Error(val message: String) : TripDetailState()
}

// États pour la liste des trajets
sealed class TripListState {
    object Idle : TripListState()
    object Loading : TripListState()
    data class Success(
        val trips: List<TripSummary>,
        val totalCount: Int,
        val currentPage: Int,
        val totalPages: Int
    ) : TripListState()
    data class Error(val message: String) : TripListState()
}

class TripDetailViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripDetailViewModel::class.java)) {
            return TripDetailViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
