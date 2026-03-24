package com.example.delivery.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delivery.models.ScanResult
import com.example.delivery.repository.ShipmentRepository
import kotlinx.coroutines.launch

class BarcodeScannerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = getApplication<Application>().applicationContext
    private val shipmentRepository = ShipmentRepository()
    
    val hasCameraPermission = mutableStateOf(
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    )
    
    val isScanning = mutableStateOf(false)
    val lastScannedBarcode = mutableStateOf<String?>(null)
    val scanResult = mutableStateOf<ScanResult>(ScanResult.Idle)
    
    fun requestCameraPermission() {
        hasCameraPermission.value = true
    }
    
    fun startScanning() {
        if (hasCameraPermission.value) {
            isScanning.value = true
            scanResult.value = ScanResult.Idle
        }
    }
    
    fun stopScanning() {
        isScanning.value = false
    }
    
    fun onBarcodeScanned(barcode: String) {
        lastScannedBarcode.value = barcode
        isScanning.value = false
        
        // Search for shipment with this barcode
        viewModelScope.launch {
            scanResult.value = ScanResult.Loading
            
            try {
                val result = shipmentRepository.searchShipment(barcode, driverId = 0)
                result.onSuccess { shipment ->
                    scanResult.value = ScanResult.Success(shipment)
                }.onFailure { error ->
                    scanResult.value = ScanResult.Error(error.message ?: "Shipment not found")
                }
            } catch (e: Exception) {
                scanResult.value = ScanResult.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetScan() {
        lastScannedBarcode.value = null
        scanResult.value = ScanResult.Idle
    }
}
