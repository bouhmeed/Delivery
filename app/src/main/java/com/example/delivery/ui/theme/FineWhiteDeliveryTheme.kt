package com.example.delivery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Thème épuré blanc - Style "Fine"
private val FineWhiteLightColorScheme = lightColorScheme(
    primary = Color(0xFF2C3E50),           // Bleu marine élégant
    onPrimary = Color(0xFFFFFFFF),           // Blanc pur
    primaryContainer = Color(0xFFF8F9FA),      // Blanc cassé très clair
    onPrimaryContainer = Color(0xFF2C3E50),  // Bleu marine
    secondary = Color(0xFF6B7280),           // Gris bleuté
    onSecondary = Color(0xFFFFFFFF),           // Blanc
    secondaryContainer = Color(0xFFF5F5F5),    // Gris très clair
    onSecondaryContainer = Color(0xFF6B7280),  // Gris bleuté
    tertiary = Color(0xFFD4AF37),           // Or élégant
    onTertiary = Color(0xFFFFFFFF),           // Blanc
    tertiaryContainer = Color(0xFFFFF8E1),    // Or très clair
    onTertiaryContainer = Color(0xFFD4AF37),  // Or élégant
    background = Color(0xFFFFFFFF),            // Blanc pur
    onBackground = Color(0xFFF5F5F5),        // Gris très clair
    surface = Color(0xFFFFFFFF),              // Blanc pur
    onSurface = Color(0xFFF5F5F5),            // Gris très clair
    surfaceVariant = Color(0xFFFAFAFA),       // Blanc cassé ultra clair
    onSurfaceVariant = Color(0xFF6B7280),    // Gris bleuté
    outline = Color(0xFFE5E7EB),           // Gris très clair
    outlineVariant = Color(0xFFD1D5DB),     // Gris moyen
    error = Color(0xFFE53E3E),             // Rouge élégant
    onError = Color(0xFFFFFFFF),             // Blanc
    errorContainer = Color(0xFFFFE3E0),     // Rouge très clair
    onErrorContainer = Color(0xFFE53E3E)
)

private val FineWhiteDarkColorScheme = darkColorScheme(
    primary = Color(0xFF2C3E50),           // Bleu marine élégant
    onPrimary = Color(0xFFFFFFFF),           // Blanc pur
    primaryContainer = Color(0xFFF8F9FA),      // Blanc cassé très clair
    onPrimaryContainer = Color(0xFF2C3E50),  // Bleu marine
    secondary = Color(0xFF6B7280),           // Gris bleuté
    onSecondary = Color(0xFFFFFFFF),           // Blanc
    secondaryContainer = Color(0xFFF5F5F5),    // Gris très clair
    onSecondaryContainer = Color(0xFF6B7280),  // Gris bleuté
    tertiary = Color(0xFFD4AF37),           // Or élégant
    onTertiary = Color(0xFFFFFFFF),           // Blanc
    tertiaryContainer = Color(0xFFFFF8E1),    // Or très clair
    onTertiaryContainer = Color(0xFFD4AF37),  // Or élégant
    background = Color(0xFF1F2937),            // Gris foncé
    onBackground = Color(0xFFF5F5F5),        // Gris très clair
    surface = Color(0xFF2D3748),              // Gris moyen
    onSurface = Color(0xFFF5F5F5),            // Gris très clair
    surfaceVariant = Color(0xFF4B5563),       // Gris bleuté foncé
    onSurfaceVariant = Color(0xFF6B7280),    // Gris bleuté
    outline = Color(0xFFE5E7EB),           // Gris très clair
    outlineVariant = Color(0xFFD1D5DB),     // Gris moyen
    error = Color(0xFFE53E3E),             // Rouge élégant
    onError = Color(0xFFFFFFFF),             // Blanc
    errorContainer = Color(0xFFFFE3E0),     // Rouge très clair
    onErrorContainer = Color(0xFFE53E3E)
)

// Composition locale pour permettre le changement de thème
val LocalFineWhiteTheme = staticCompositionLocalOf { false }

@Composable
fun FineWhiteDeliveryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useFineTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        useFineTheme -> when {
            darkTheme -> FineWhiteDarkColorScheme
            else -> FineWhiteLightColorScheme
        }
        else -> when {
            darkTheme -> darkColorScheme(
                primary = Primary,
                onPrimary = OnPrimary,
                primaryContainer = PrimaryContainer,
                onPrimaryContainer = OnPrimaryContainer,
                secondary = Secondary,
                onSecondary = OnSecondary,
                secondaryContainer = SecondaryContainer,
                onSecondaryContainer = OnSecondaryContainer,
                tertiary = Tertiary,
                onTertiary = OnTertiary,
                tertiaryContainer = TertiaryContainer,
                onTertiaryContainer = OnTertiaryContainer,
                background = Background,
                onBackground = OnBackground,
                surface = Surface,
                onSurface = OnSurface,
                surfaceVariant = SurfaceVariant,
                onSurfaceVariant = OnSurfaceVariant,
                outline = Outline,
                outlineVariant = OutlineVariant,
                error = Error,
                onError = OnError,
                errorContainer = ErrorContainer,
                onErrorContainer = OnErrorContainer
            )
            else -> lightColorScheme(
                primary = Primary,
                onPrimary = OnPrimary,
                primaryContainer = PrimaryContainer,
                onPrimaryContainer = OnPrimaryContainer,
                secondary = Secondary,
                onSecondary = OnSecondary,
                secondaryContainer = SecondaryContainer,
                onSecondaryContainer = OnSecondaryContainer,
                tertiary = Tertiary,
                onTertiary = OnTertiary,
                tertiaryContainer = TertiaryContainer,
                onTertiaryContainer = OnTertiaryContainer,
                background = Background,
                onBackground = OnBackground,
                surface = Surface,
                onSurface = OnSurface,
                surfaceVariant = SurfaceVariant,
                onSurfaceVariant = OnSurfaceVariant,
                outline = Outline,
                outlineVariant = OutlineVariant,
                error = Error,
                onError = OnError,
                errorContainer = ErrorContainer,
                onErrorContainer = OnErrorContainer
            )
        }
    }
    
    CompositionLocalProvider(LocalFineWhiteTheme provides useFineTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

// Extensions pour faciliter l'utilisation du thème épuré
object FineWhiteThemeExtensions {
    @Composable
    fun useFineTheme(): Boolean {
        return LocalFineWhiteTheme.current
    }
}
