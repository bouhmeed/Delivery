package com.example.delivery.ui.theme

import androidx.compose.ui.graphics.Color

// Thème épuré blanc et moderne - Style "Fine"
object FineWhiteTheme {
    // Palette principale - blanc épuré avec accents subtils
    val Primary = Color(0xFF2C3E50)           // Bleu marine élégant
    val OnPrimary = Color(0xFFFFFFFF)           // Blanc pur
    
    val PrimaryContainer = Color(0xFFF8F9FA)      // Blanc cassé très clair
    val OnPrimaryContainer = Color(0xFF2C3E50)  // Bleu marine
    
    // Palette secondaire - gris moderne
    val Secondary = Color(0xFF6B7280)           // Gris bleuté
    val OnSecondary = Color(0xFFFFFFFF)           // Blanc
    
    val SecondaryContainer = Color(0xFFF5F5F5)    // Gris très clair
    val OnSecondaryContainer = Color(0xFF6B7280)  // Gris bleuté
    
    // Palette tertiaire - accents dorés
    val Tertiary = Color(0xFFD4AF37)           // Or élégant
    val OnTertiary = Color(0xFFFFFFFF)           // Blanc
    
    val TertiaryContainer = Color(0xFFFFF8E1)    // Or très clair
    val OnTertiaryContainer = Color(0xFFD4AF37)  // Or élégant
    
    // Surface - blanc pur avec ombres subtiles
    val Surface = Color(0xFFFFFFFF)              // Blanc pur
    val OnSurface = Color(0xFFF5F5F5)            // Gris très clair
    val SurfaceVariant = Color(0xFFFAFAFA)       // Blanc cassé ultra clair
    val OnSurfaceVariant = Color(0xFF6B7280)    // Gris bleuté
    
    // Arrière-plan - blanc pur
    val Background = Color(0xFFFFFFFF)            // Blanc pur
    val OnBackground = Color(0xFFF5F5F5)        // Gris très clair
    
    // Contours - gris subtil
    val Outline = Color(0xFFE5E7EB)           // Gris très clair
    val OutlineVariant = Color(0xFFD1D5DB)     // Gris moyen
    
    // États - couleurs modernes
    val Success = Color(0xFF10B981)           // Vert élégant
    val Warning = Color(0xFFE5940B)           // Orange moderne
    val Error = Color(0xFFE53E3E)             // Rouge élégant
    
    val OnSuccess = Color(0xFFFFFFFF)
    val OnWarning = Color(0xFFFFFFFF)
    val OnError = Color(0xFFFFFFFF)
    
    val SuccessContainer = Color(0xFFE8F5E8)
    val WarningContainer = Color(0xFFFFF3E0)
    val ErrorContainer = Color(0xFFFFE3E0)
    
    val OnSuccessContainer = Color(0xFF10B981)
    val OnWarningContainer = Color(0xFFE5940B)
    val OnErrorContainer = Color(0xFFE53E3E)
    
    // Texte - hiérarchie typographique moderne
    val OnSurfaceText = Color(0xFF1F2937)     // Gris foncé pour texte principal
    val OnSurfaceVariantText = Color(0xFF6B7280)  // Gris bleuté pour texte secondaire
    val OnBackgroundText = Color(0xFF6B7280)     // Gris bleuté pour texte sur fond blanc
    
    // Accents et détails
    val Accent = Color(0xFF2C3E50)           // Bleu marine principal
    val AccentLight = Color(0xFF4A90E2)          // Bleu clair
    val AccentSubtle = Color(0xFFF0F4F8)          // Gris très subtil
    
    // Ombres et profondeur
    val Shadow = Color(0x1A000000)             // Ombre subtile
    val ElevationLow = Color(0x0A000000)          // Élévation faible
    val ElevationMedium = Color(0x14000000)        // Élévation moyenne
}
