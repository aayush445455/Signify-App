// File: app/src/main/java/com/signify/app/ui/components/WaveNavigationBar.kt
package com.signify.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun WaveNavigationBar(
    icons: List<Pair<String, ImageVector>>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    backgroundColor: Color,
    height: Dp = 72.dp,
    waveRadius: Dp = 36.dp
) {
    val selectedTint   = MaterialTheme.colorScheme.primary
    val unselectedTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    // Remember the bar width
    val barWidthState = remember { mutableStateOf(0f) }

    // Animatable for the notch’s X position
    val notchAnim = remember { Animatable(0f) }
    LaunchedEffect(barWidthState.value, selectedIndex) {
        val bw = barWidthState.value
        if (bw > 0f) {
            val target = bw / icons.size * (selectedIndex + 0.5f)
            notchAnim.animateTo(
                target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessLow
                )
            )
        }
    }

    Box(Modifier.height(height)) {
        // 1) Draw the wave background
        Canvas(Modifier.fillMaxSize()) {
            barWidthState.value = size.width
            drawWaveWithNotch(
                centerX = notchAnim.value,
                radius  = waveRadius.toPx(),
                color   = backgroundColor
            )
        }

        // 2) Draw and pop the icons
        Row(
            Modifier
                .fillMaxSize()
                .padding(vertical = (height - waveRadius) / 2),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            icons.forEachIndexed { idx, (desc, icon) ->
                val isSel = idx == selectedIndex

                // Vertical offset spring
                val yOffsetDp by animateDpAsState(
                    targetValue = if (isSel) -waveRadius / 3f else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness    = Spring.StiffnessMedium
                    )
                )
                // Scale spring
                val scale by animateFloatAsState(
                    targetValue = if (isSel) 1.2f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness    = Spring.StiffnessLow
                    )
                )

                Box(
                    Modifier
                        .size(waveRadius)
                        .offset { IntOffset(0, yOffsetDp.roundToPx()) }
                        .clickable { onSelected(idx) }
                        .graphicsLayer { scaleX = scale; scaleY = scale },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector      = icon,
                        contentDescription = desc,
                        tint             = if (isSel) selectedTint else unselectedTint
                    )
                }
            }
        }
    }
}

// Non-@Composable drawing helper
private fun DrawScope.drawWaveWithNotch(
    centerX: Float,
    radius: Float,
    color: Color
) {
    val w = size.width
    val h = size.height
    val bumpW = radius * 1.5f

    val path = Path().apply {
        moveTo(0f, 0f)
        // left hump
        cubicTo(
            x1 = centerX * 0.25f,          y1 = 0f,
            x2 = centerX - radius - bumpW, y2 = radius * 0.6f,
            x3 = centerX - radius,         y3 = radius * 0.6f
        )
        // notch
        arcTo(
            rect              = Rect(centerX - radius, -radius, centerX + radius, radius),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo       = false
        )
        // right hump
        cubicTo(
            x1 = centerX + radius,           y1 = radius * 0.6f,
            x2 = centerX + radius + bumpW,   y2 = 0f,
            x3 = w,                          y3 = 0f
        )
        // bottom edge
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(path, color)
}
