package com.example.delivery.models

sealed class ScanResult {
    object Idle : ScanResult()
    object Loading : ScanResult()
    data class Success(val shipment: Any) : ScanResult()
    data class Error(val message: String) : ScanResult()
}
