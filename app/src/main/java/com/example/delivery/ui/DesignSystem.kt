package com.example.delivery.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Système de design cohérent pour toute l'application
 * Centralise les couleurs, tailles et espacements
 */
object DesignSystem {
    
    // ========================================
    // PALETTE DE COULEURS PRINCIPALE
    // ========================================
    
    object Colors {
        // Couleurs primaires (Bleu)
        val PRIMARY_BLUE = Color(0xFF1976D2)          // Bleu principal
        val PRIMARY_BLUE_LIGHT = Color(0xFF42A5F5)     // Bleu clair
        val PRIMARY_BLUE_DARK = Color(0xFF0D47A1)      // Bleu foncé
        
        // Couleurs de succès (Vert)
        val SUCCESS_GREEN = Color(0xFF388E3C)          // Vert succès
        val SUCCESS_GREEN_LIGHT = Color(0xFF66BB6A)     // Vert clair
        val SUCCESS_GREEN_DARK = Color(0xFF2E7D32)      // Vert foncé (Navigation)
        
        // Couleurs d'avertissement (Orange)
        val WARNING_ORANGE = Color(0xFFFF9800)         // Orange attention
        val WARNING_ORANGE_LIGHT = Color(0xFFFFB74D)    // Orange clair
        val WARNING_ORANGE_DARK = Color(0xFFE65100)     // Orange foncé
        
        // Couleurs de validation
        val VALIDATION_BLUE = Color(0xFF1976D2)        // Bleu validation
        val NAVIGATION_GREEN = Color(0xFF2E7D32)       // Vert navigation
        val CALL_GREEN = Color(0xFF4CAF50)             // Vert appel
        
        // Couleurs neutres
        val DISABLED_GRAY = Color(0xFFBDBDBD)          // Gris désactivé
        val BACKGROUND_GRAY = Color(0xFFF5F5F5)        // Gris fond
        val SURFACE_WHITE = Color.White                 // Blanc surface
        val TEXT_PRIMARY = Color(0xFF212121)            // Texte principal
        val TEXT_SECONDARY = Color(0xFF757575)         // Texte secondaire
        val TEXT_HINT = Color(0xFFBDBDBD)              // Texte indice
        
        // Couleurs de statut
        val STATUS_COMPLETED = SUCCESS_GREEN             // Terminé
        val STATUS_IN_PROGRESS = WARNING_ORANGE          // En cours
        val STATUS_PENDING = VALIDATION_BLUE             // À faire
        val STATUS_CANCELLED = DISABLED_GRAY            // Annulé
        
        // Couleurs de bordure
        val OUTLINE_GREEN = Color(0xFF81C784)          // Vert contour
        val OUTLINE_BLUE = Color(0xFF64B5F6)           // Bleu contour
        val OUTLINE_GRAY = Color(0xFFE0E0E0)           // Gris contour
    }
    
    // ========================================
    // TAILLES STANDARDISÉES
    // ========================================
    
    object Sizes {
        // Boutons
        val BUTTON_HEIGHT_LARGE = 56.dp                 // Hauteur bouton large
        val BUTTON_HEIGHT_MEDIUM = 48.dp                // Hauteur bouton moyen
        val BUTTON_HEIGHT_SMALL = 40.dp                 // Hauteur bouton petit
        val BUTTON_HEIGHT_MINI = 32.dp                  // Hauteur bouton mini
        
        // Icônes
        val ICON_SIZE_LARGE = 24.dp                     // Icône grande
        val ICON_SIZE_MEDIUM = 18.dp                    // Icône moyenne
        val ICON_SIZE_SMALL = 16.dp                     // Icône petite
        val ICON_SIZE_MINI = 12.dp                     // Icône mini
        
        // Coins arrondis
        val CORNER_RADIUS_LARGE = 16.dp                 // Coins grands
        val CORNER_RADIUS_MEDIUM = 12.dp                // Coins moyens
        val CORNER_RADIUS_SMALL = 8.dp                  // Coins petits
        val CORNER_RADIUS_MINI = 4.dp                  // Coins mini
        
        // Espacements
        val SPACING_XLARGE = 24.dp                     // Espacement très grand
        val SPACING_LARGE = 16.dp                      // Espacement grand
        val SPACING_MEDIUM = 12.dp                     // Espacement moyen
        val SPACING_SMALL = 8.dp                       // Espacement petit
        val SPACING_MINI = 4.dp                        // Espacement mini
        val SPACING_MICRO = 2.dp                       // Espacement micro
        
        // Épaisseurs
        val BORDER_THICK = 3.dp                         // Bordure épaisse
        val BORDER_MEDIUM = 2.dp                        // Bordure moyenne
        val BORDER_THIN = 1.dp                         // Bordure fine
        
        // Ombres et élévations
        val ELEVATION_NONE = 0.dp                       // Pas d'élévation
        val ELEVATION_SMALL = 2.dp                      // Élévation petite
        val ELEVATION_MEDIUM = 4.dp                     // Élévation moyenne
        val ELEVATION_LARGE = 8.dp                      // Élévation grande
    }
    
    // ========================================
    // TYPOGRAPHIE
    // ========================================
    
    object Typography {
        // Poids de police
        const val WEIGHT_LIGHT = "Light"
        const val WEIGHT_NORMAL = "Normal"
        const val WEIGHT_MEDIUM = "Medium"
        const val WEIGHT_SEMI_BOLD = "SemiBold"
        const val WEIGHT_BOLD = "Bold"
        
        // Tailles de texte (référence)
        // Utiliser Material3 Typography standards
    }
    
    // ========================================
    // COMPOSANTS PRÉDÉFINIS
    // ========================================
    
    object Components {
        // Card
        val CARD_CORNER_RADIUS = Sizes.CORNER_RADIUS_MEDIUM
        val CARD_ELEVATION = Sizes.ELEVATION_SMALL
        
        // Button
        val BUTTON_CORNER_RADIUS = Sizes.CORNER_RADIUS_MEDIUM
        val BUTTON_ELEVATION = Sizes.ELEVATION_SMALL
        val BUTTON_ELEVATION_PRESSED = Sizes.ELEVATION_MEDIUM
        
        // TextField
        val TEXT_FIELD_CORNER_RADIUS = Sizes.CORNER_RADIUS_SMALL
        val TEXT_FIELD_BORDER_WIDTH = Sizes.BORDER_THIN
        
        // Chip/Badge
        val CHIP_CORNER_RADIUS = Sizes.CORNER_RADIUS_MINI
        val CHIP_HORIZONTAL_PADDING = Sizes.SPACING_SMALL
        val CHIP_VERTICAL_PADDING = Sizes.SPACING_MINI
    }
    
    // ========================================
    // UTILITAIRES
    // ========================================
    
    object Utils {
        // Fonction pour obtenir la couleur de statut
        fun getStatusColor(isCompleted: Boolean, status: String? = null): Color {
            return when {
                isCompleted -> Colors.STATUS_COMPLETED
                status == "EN_COURS" -> Colors.STATUS_IN_PROGRESS
                status == "NON_DEMARRE" -> Colors.STATUS_PENDING
                else -> Colors.STATUS_PENDING
            }
        }
        
        // Fonction pour obtenir la couleur de bouton selon le type
        fun getButtonColor(type: ButtonType): Color {
            return when (type) {
                ButtonType.PRIMARY -> Colors.PRIMARY_BLUE
                ButtonType.SUCCESS -> Colors.SUCCESS_GREEN
                ButtonType.NAVIGATION -> Colors.NAVIGATION_GREEN
                ButtonType.VALIDATION -> Colors.VALIDATION_BLUE
                ButtonType.CALL -> Colors.CALL_GREEN
                ButtonType.WARNING -> Colors.WARNING_ORANGE
                ButtonType.DISABLED -> Colors.DISABLED_GRAY
            }
        }
    }
    
    // ========================================
    // TYPES DE BOUTONS
    // ========================================
    
    enum class ButtonType {
        PRIMARY,
        SUCCESS,
        NAVIGATION,
        VALIDATION,
        CALL,
        WARNING,
        DISABLED
    }
}
