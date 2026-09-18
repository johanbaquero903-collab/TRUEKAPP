package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = TruekappPrimaryDark,
    onPrimary = TruekappOnPrimaryContainer,
    primaryContainer = TruekappPrimary,
    onPrimaryContainer = TruekappPrimaryContainer,
    secondary = TruekappSecondaryDark,
    onSecondary = TruekappOnSecondaryContainer,
    secondaryContainer = TruekappSecondary,
    onSecondaryContainer = TruekappSecondaryContainer,
    tertiary = TruekappTertiary,
    background = TruekappBackgroundDark,
    surface = TruekappSurfaceDark,
    surfaceVariant = TruekappSurfaceVariantDark,
    onBackground = TruekappOnSurfaceDark,
    onSurface = TruekappOnSurfaceDark,
    outline = TruekappOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = TruekappPrimary,
    onPrimary = TruekappOnPrimary,
    primaryContainer = TruekappPrimaryContainer,
    onPrimaryContainer = TruekappOnPrimaryContainer,
    secondary = TruekappSecondary,
    onSecondary = TruekappOnSecondary,
    secondaryContainer = TruekappSecondaryContainer,
    onSecondaryContainer = TruekappOnSecondaryContainer,
    tertiary = TruekappTertiary,
    tertiaryContainer = TruekappTertiaryContainer,
    onTertiaryContainer = TruekappOnTertiaryContainer,
    background = TruekappBackgroundLight,
    surface = TruekappSurfaceLight,
    surfaceVariant = TruekappSurfaceVariantLight,
    onBackground = TruekappOnSurfaceLight,
    onSurface = TruekappOnSurfaceLight,
    outline = TruekappOutlineLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our signature Truekapp emerald & coral palette
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
