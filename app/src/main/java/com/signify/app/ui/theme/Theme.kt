package com.signify.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary     = Gold,
    onPrimary   = OnGold,
    background  = Navy,
    onBackground= Cream,
    surface     = NavyVariant,
    onSurface   = Cream,
)

private val LightColors = lightColorScheme(
    primary     = Gold,
    onPrimary   = OnGold,
    background  = Navy,
    onBackground= Cream,
    surface     = NavyVariant,
    onSurface   = Cream,
)

@Composable
fun SignifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography  = SignifyTypography,
        shapes      = SignifyShapes,
        content     = content
    )
}
