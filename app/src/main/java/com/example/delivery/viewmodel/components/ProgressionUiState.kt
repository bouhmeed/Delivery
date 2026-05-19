package com.example.delivery.viewmodel.components

sealed class ProgressionUiState {
    object Loading : ProgressionUiState()
    data class Success(val tripWithProgress: com.example.delivery.models.TripWithProgress) : ProgressionUiState()
    object NoTripToday : ProgressionUiState()
    data class Error(val message: String) : ProgressionUiState()
}
