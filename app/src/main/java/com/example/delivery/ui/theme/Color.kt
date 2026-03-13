package com.example.delivery.ui.theme

import androidx.compose.ui.graphics.Color

// Couleurs classiques et professionnelles
val PrimaryBlue = Color(0xFF1976D2)      // Bleu Material Design classique
val PrimaryDark = Color(0xFF0D47A1)       // Bleu foncé
val AccentOrange = Color(0xFFFF9800)       // Orange accent
val BackgroundLight = Color(0xFFF5F5F5)   // Gris clair
val SurfaceWhite = Color(0xFFFFFFFF)      // Blanc pur

// Couleurs principales
val Primary = PrimaryBlue
val OnPrimary = Color.White
val PrimaryContainer = Color(0xFFE3F2FD)   // Bleu très clair
val OnPrimaryContainer = PrimaryDark

// Couleurs secondaires
val Secondary = Color(0xFF424242)         // Gris foncé
val OnSecondary = Color.White
val SecondaryContainer = Color(0xFFE0E0E0) // Gris moyen
val OnSecondaryContainer = Color(0xFF212121)

// Couleurs tertiaires
val Tertiary = AccentOrange
val OnTertiary = Color.White
val TertiaryContainer = Color(0xFFFFF3E0)  // Orange très clair
val OnTertiaryContainer = Color(0xFFE65100)

// Couleurs de surface
val Surface = SurfaceWhite
val OnSurface = Color(0xFF212121)         // Gris très foncé
val SurfaceVariant = Color(0xFFF5F5F5)   // Gris très clair
val OnSurfaceVariant = Color(0xFF757575)  // Gris moyen

// Couleurs d'arrière-plan
val Background = BackgroundLight
val OnBackground = Color(0xFF212121)

// Couleurs d'erreur
val Error = Color(0xFFD32F2F)             // Rouge Material Design
val OnError = Color.White
val ErrorContainer = Color(0xFFFFEBEE)     // Rouge très clair
val OnErrorContainer = Color(0xFFB71C1C)

// Couleurs de contour
val Outline = Color(0xFFBDBDBD)           // Gris clair
val OutlineVariant = Color(0xFFE0E0E0)

// Couleurs pour les états
val Success = Color(0xFF4CAF50)           // Vert Material Design
val Warning = Color(0xFFFF9800)           // Orange (réutilisé)
val Info = PrimaryBlue