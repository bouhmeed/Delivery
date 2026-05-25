package com.example.delivery.viewmodel.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.TripHistory
import com.example.delivery.models.DriverStats
import com.example.delivery.models.user.UserResponse
import com.example.delivery.repository.DirectHistoryRepository
import com.example.delivery.repository.user.DirectUserRepository
import com.example.delivery.repository.Result as DbResult
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface HistoryUiState {
    object Loading : HistoryUiState
    data class Error(val message: String) : HistoryUiState
    data class Success(
        val userInfo: UserResponse?,
        val history: TripHistory?,
        val stats: DriverStats?
    ) : HistoryUiState
}

class HistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState

    private val directHistoryRepo = DirectHistoryRepository()
    private val directUserRepo = DirectUserRepository()

    fun loadHistoryData(email: String) {
        _uiState.value = HistoryUiState.Loading
        viewModelScope.launch {
            try {
                // 1. Fetch user
                val userResult = directUserRepo.getUserByEmail(email)
                if (userResult.isFailure) {
                    _uiState.value = HistoryUiState.Error("Erreur utilisateur: ${userResult.exceptionOrNull()?.message}")
                    return@launch
                }
                
                val user = userResult.getOrNull()
                val driverId = user?.driverId
                if (driverId == null) {
                    _uiState.value = HistoryUiState.Error("Aucun driver ID trouvé pour cet utilisateur.")
                    return@launch
                }

                val driverIdInt = driverId.toIntOrNull() ?: 5

                // 2. Fetch history and stats concurrently
                val historyDeferred = async { directHistoryRepo.getDriverHistory(driverIdInt) }
                val statsDeferred = async { directHistoryRepo.getDriverStats(driverIdInt) }

                val historyResult = historyDeferred.await()
                val statsResult = statsDeferred.await()

                var driverHistory: TripHistory? = null
                var driverStats: DriverStats? = null
                var errorMsg: String? = null

                if (historyResult is DbResult.Success) {
                    driverHistory = historyResult.data
                } else if (historyResult is DbResult.Error) {
                    errorMsg = historyResult.message
                }

                if (statsResult is DbResult.Success) {
                    driverStats = statsResult.data
                } else if (statsResult is DbResult.Error) {
                    errorMsg = statsResult.message
                }

                if (errorMsg != null) {
                    _uiState.value = HistoryUiState.Error(errorMsg)
                } else {
                    _uiState.value = HistoryUiState.Success(
                        userInfo = user,
                        history = driverHistory,
                        stats = driverStats
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error("Erreur: ${e.message}")
            }
        }
    }
}
