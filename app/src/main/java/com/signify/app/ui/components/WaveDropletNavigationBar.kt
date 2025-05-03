// File: app/src/main/java/com/signify/app/ui/components/WaveDropletNavigationBar.kt
package com.signify.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WaveDropletNavigationBar(
    icons: List<Pair<String, ImageVector>>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    barColor: Color = MaterialTheme.colorScheme.surface,
    dropletColor: Color = MaterialTheme.colorScheme.primary,
    iconTintOn: Color = MaterialTheme.colorScheme.onPrimary,
    iconTintOff: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    barHeight: Dp = 64.dp,
    dropletDiameter: Dp = 48.dp
) {
    // measure full width in px
    var widthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Box(
        Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(barColor)
            .onSizeChanged { widthPx = it.width }
    ) {
        if (widthPx > 0 && icons.isNotEmpty()) {
            // 1) compute centers for each tab (px)
            val segment = widthPx.toFloat() / icons.size
            val centers = List(icons.size) { i -> segment * (i + 0.5f) }

            // 2) notch animatable (px)
            val animX = remember { Animatable(centers[selectedIndex]) }
            LaunchedEffect(selectedIndex, widthPx) {
                animX.animateTo(
                    centers[selectedIndex],
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessLow
                    )
                )
            }

            // 3) draw the wave bar with a notch
            Canvas(Modifier.fillMaxSize()) {
                val h = size.height
                val r = with(density) { dropletDiameter.toPx() } * 0.6f // notch radius
                val cx = animX.value
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(cx - r * 1.2f, 0f)
                    arcTo(
                        rect             = Rect(cx - r, -r, cx + r, r),
                        startAngleDegrees= 180f,
                        sweepAngleDegrees= -180f,
                        forceMoveTo      = false
                    )
                    lineTo(size.width, 0f)
                    lineTo(size.width, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path, barColor)
                // 4) draw the droplet circle
                drawCircle(
                    color  = dropletColor,
                    radius = with(density) { dropletDiameter.toPx() / 2f },
                    center = Offset(cx, h / 2f)
                )
            }

            // 5) icons on top
            Row(
                Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                icons.forEachIndexed { idx, (desc, icon) ->
                    val isSel = idx == selectedIndex
                    // scale animation
                    val scale by animateFloatAsState(
                        targetValue   = if (isSel) 1.2f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness    = Spring.StiffnessMedium
                        )
                    )
                    Icon(
                        imageVector      = icon,
                        contentDescription= desc,
                        tint             = if (isSel) iconTintOn else iconTintOff,
                        modifier         = Modifier
                            .size(dropletDiameter * 0.6f)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clickable { onSelected(idx) }
                    )
                }
            }
        }
    }
}
