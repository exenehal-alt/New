package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = DeepIndigoInk,
    onPrimary = Color.White,
    primaryContainer = WarmSandContainer,
    onPrimaryContainer = DeepIndigoInk,
    secondary = TerracottaAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF6E0D6),
    onSecondaryContainer = Color(0xFF5C2612),
    tertiary = ForestSage,
    onTertiary = Color.White,
    background = AlabasterBg,
    onBackground = EspressoText,
    surface = CreamPaperSurface,
    onSurface = EspressoText,
    surfaceVariant = WarmSandContainer,
    onSurfaceVariant = MutedSepiaText,
    outline = HairlineBorder,
    outlineVariant = Color(0xFFE7DFD2)
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftGoldPrimary,
    onPrimary = CharcoalInkBg,
    primaryContainer = SlateContainer,
    onPrimaryContainer = SoftGoldPrimary,
    secondary = SoftTerracotta,
    onSecondary = CharcoalInkBg,
    secondaryContainer = Color(0xFF4A2B20),
    onSecondaryContainer = Color(0xFFFAD8CA),
    tertiary = SoftSage,
    onTertiary = CharcoalInkBg,
    background = CharcoalInkBg,
    onBackground = IvoryText,
    surface = SlatePaperSurface,
    onSurface = IvoryText,
    surfaceVariant = SlateContainer,
    onSurfaceVariant = MutedSilverText,
    outline = DarkBorder,
    outlineVariant = Color(0xFF282E3B)
)

@Composable
fun PandulipiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
