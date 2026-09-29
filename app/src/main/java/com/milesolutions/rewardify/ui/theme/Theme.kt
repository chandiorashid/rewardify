package com.milesolutions.rewardify.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Indigo700,
    onPrimary = Color.White,
    primaryContainer = Indigo100,
    onPrimaryContainer = Indigo900,
    secondary = Emerald600,
    onSecondary = Color.White,
    secondaryContainer = Emerald100,
    onSecondaryContainer = Emerald700,
    tertiary = Gold500,
    background = Gray50,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray500,
    outline = Gray200,
    error = Danger500,
    onError = Color.White,
    errorContainer = Danger100,
    onErrorContainer = Danger500
)

private val DarkColors = darkColorScheme(
    primary = Indigo200,
    onPrimary = Indigo900,
    primaryContainer = Indigo700,
    onPrimaryContainer = Indigo100,
    secondary = Emerald500,
    onSecondary = Indigo900,
    secondaryContainer = Emerald700,
    onSecondaryContainer = Emerald100,
    tertiary = Gold400,
    background = Indigo900,
    onBackground = Color.White,
    surface = Indigo800,
    onSurface = Color.White,
    surfaceVariant = Indigo700,
    onSurfaceVariant = Indigo100,
    outline = Indigo700,
    error = Danger500,
    onError = Color.White
)

@Composable
fun RewardifyTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = RewardifyTypography,
        content = content
    )
}
