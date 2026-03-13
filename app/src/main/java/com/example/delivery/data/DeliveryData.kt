package com.example.delivery.data

import java.time.LocalDate

// Données partagées pour toute l'application
object DeliveryData {
    
    // Informations du chauffeur connecté
    val currentDriver = DriverInfo(
        name = "Ahmed Khemir",
        email = "chauffeur@delivery.com",
        vehicle = "Camion Volvo FH16",
        immatriculation = "XY-456-ZZ"
    )
    
    // Tournée du jour
    val todayTour = TourInfo(
        date = LocalDate.now(),
        city = "Paris",
        expeditionCount = 3,
        driverName = currentDriver.name,
        vehicle = "${currentDriver.vehicle} - Immat: ${currentDriver.immatriculation}",
        expeditions = listOf(
            ExpeditionInfo(
                id = "Ship-923-1",
                destination = "Paris - Client X",
                volume = "85 m³",
                weight = "650 kg",
                estimatedTime = "~30 min",
                status = "En cours"
            ),
            ExpeditionInfo(
                id = "Ship-923-2",
                destination = "Paris - Client Y",
                volume = "120.5 m³",
                weight = "980 kg",
                estimatedTime = "~45 min",
                status = "En attente"
            ),
            ExpeditionInfo(
                id = "Ship-923-3",
                destination = "Paris - Client Z",
                volume = "95.3 m³",
                weight = "720 kg",
                estimatedTime = "~35 min",
                status = "En attente"
            )
        )
    )
    
    // Statistiques du jour (calculées à partir des données réelles)
    val todayStats = DailyStats(
        totalDeliveries = todayTour.expeditionCount,
        completedDeliveries = todayTour.expeditions.count { it.status == "Terminé" },
        totalKm = 45.2,
        totalWeight = todayTour.expeditions.sumOf { 
            it.weight.replace(" kg", "").toDoubleOrNull() ?: 0.0 
        },
        totalVolume = todayTour.expeditions.sumOf { 
            it.volume.replace(" m³", "").toDoubleOrNull() ?: 0.0 
        }
    )
    
    // Historique des livraisons (derniers 7 jours)
    val deliveryHistory = listOf(
        HistoryItem(
            date = LocalDate.now().minusDays(1),
            city = "Lyon",
            deliveriesCount = 2,
            status = "Terminé",
            totalKm = 38.5
        ),
        HistoryItem(
            date = LocalDate.now().minusDays(2),
            city = "Marseille",
            deliveriesCount = 4,
            status = "Terminé",
            totalKm = 62.3
        ),
        HistoryItem(
            date = LocalDate.now().minusDays(3),
            city = "Bordeaux",
            deliveriesCount = 3,
            status = "Terminé",
            totalKm = 41.7
        ),
        HistoryItem(
            date = LocalDate.now().minusDays(4),
            city = "Nantes",
            deliveriesCount = 2,
            status = "Terminé",
            totalKm = 35.8
        ),
        HistoryItem(
            date = LocalDate.now().minusDays(5),
            city = "Lille",
            deliveriesCount = 5,
            status = "Terminé",
            totalKm = 48.9
        )
    )
}

// Classes de données
data class DriverInfo(
    val name: String,
    val email: String,
    val vehicle: String,
    val immatriculation: String
)

data class TourInfo(
    val date: LocalDate,
    val city: String,
    val expeditionCount: Int,
    val driverName: String,
    val vehicle: String,
    val expeditions: List<ExpeditionInfo>
)

data class ExpeditionInfo(
    val id: String,
    val destination: String,
    val volume: String,
    val weight: String,
    val estimatedTime: String,
    val status: String = "En attente" // En attente, En cours, Terminé
)

data class DailyStats(
    val totalDeliveries: Int,
    val completedDeliveries: Int,
    val totalKm: Double,
    val totalWeight: Double,
    val totalVolume: Double
) {
    val completionPercentage: Int
        get() = if (totalDeliveries > 0) {
            (completedDeliveries * 100) / totalDeliveries
        } else 0
}

data class HistoryItem(
    val date: LocalDate,
    val city: String,
    val deliveriesCount: Int,
    val status: String,
    val totalKm: Double
)
