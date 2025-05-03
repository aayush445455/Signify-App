// app/src/main/java/com/signify/app/ui/theme/Typography.kt
package com.signify.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography as M3Typography

val AppTypography = M3Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight  = FontWeight.Normal,
        fontSize    = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight  = FontWeight.Normal,
        fontSize    = 14.sp
    )
    // override more text styles if desired
)
