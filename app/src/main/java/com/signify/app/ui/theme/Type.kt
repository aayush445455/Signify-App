package com.signify.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SignifyTypography = Typography(
    displayLarge  = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
    bodyLarge     = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    labelLarge    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
)
