package com.example.delivery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.TripProgress
import com.example.delivery.models.TripWithProgress
import com.example.delivery.repository.ProgressionRepository
import com.example.delivery.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgressionViewModel(
    private val repository: ProgressionRepository = ProgressionRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ProgressionUiState>(ProgressionUiState.Loading)
    val uiState: StateFlow<ProgressionUiState> = _uiState.asStateFlow()
    
    private val _driverId = MutableStateFlow<Int?>(null)
    val driverId: StateFlow<Int?> = _driverId.asStateFlow()
    
    fun setDriverId(id: Int) {
        _driverId.value = id
        loadTripProgress()
    }
    
    fun loadTripProgress() {
        val driverId = _driverId.value ?: return
        
        viewModelScope.launch {
            repository.getTodayTripProgress(driverId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.value = ProgressionUiState.Loading
                    }
                    is Result.Success -> {
                        val data = result.data
                        if (data == null) {
                            _uiState.value = ProgressionUiState.NoTripToday
                        } else {
                            _uiState.value = ProgressionUiState.Success(data)
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = ProgressionUiState.Error(result.message)
                    }
                }
            }
        }
    }
    
    fun refreshProgress() {
        val driverId = _driverId.value ?: return
        viewModelScope.launch {
            repository.refreshTripProgress(driverId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // Keep current data but show refreshing
                    }
                    is Result.Success -> {
                        val data = result.data
                        if (data == null) {
                            _uiState.value = ProgressionUiState.NoTripToday
                        } else {
                            _uiState.value = ProgressionUiState.Success(data)
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = ProgressionUiState.Error(result.message)
                    }
                }
            }
        }
    }
}
