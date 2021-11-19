package com.note11.engabi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController


private val LightColorPalette = lightColors(
    primary = LightBlue,
    primaryVariant = DarkBlue,
    secondary = DarkRedRecording,
    background = LightGray100,
    surface = Color.Transparent,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = PureBlack
)

@Composable
fun EngabiTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = StatusBarColor
    )
    MaterialTheme(
        colors = LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}