package com.vansh.familytree.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    background = BackgroundColor,
    surface = SurfaceColor,
    surfaceVariant = SurfaceVariantColor,
    onPrimary = OnPrimaryColor,
    onSecondary = OnSecondaryColor,
    onBackground = OnBackgroundColor,
    onSurface = OnSurfaceColor,
    outline = BorderColor
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDarkColor,
    secondary = SecondaryDarkColor,
    background = BackgroundDarkColor,
    surface = SurfaceDarkColor,
    surfaceVariant = SurfaceVariantDarkColor,
    onPrimary = OnPrimaryDarkColor,
    onSecondary = OnSecondaryDarkColor,
    onBackground = OnBackgroundDarkColor,
    onSurface = OnSurfaceDarkColor,
    outline = BorderDarkColor
)

@Composable
fun VanshTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VanshTypography,
        content = content
    )
}
