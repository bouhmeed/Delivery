package com.example.delivery.repository

// Result class for state management
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
    
    companion object {
        fun <T> loading(): Result<T> = Loading
        fun <T> error(message: String): Result<T> = Error(message)
        fun <T> success(data: T): Result<T> = Success(data)
    }
}
