package com.note11.engabi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController


private val LightColorPalette = lightColors(
    primary = Blue800,
    primaryVariant = Blue700,
    secondary = BlueAccent,
    secondaryVariant = RedAccent,
    background = Blue500,
    surface = Color.Transparent,
    onPrimary = White,
    onSecondary = White,
    onBackground = White
)

private val DarkColorPalette = darkColors(
    primary = Blue800,
    primaryVariant = Blue700,
    secondary = BlueAccent,
    secondaryVariant = RedAccent,
    background = Blue500,
    surface = Color.Transparent,
    onPrimary = White,
    onSecondary = White,
    onBackground = White
)

@Composable
fun EngabiTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = StatusBarColor
    )
    MaterialTheme(
        colors = if(!darkTheme) LightColorPalette else DarkColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}