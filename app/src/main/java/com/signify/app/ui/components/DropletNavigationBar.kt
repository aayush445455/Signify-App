// File: app/src/main/java/com/signify/app/ui/components/DropletNavigationBar.kt
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DropletNavigationBar(
    icons: List<Pair<String, ImageVector>>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    barColor: Color = MaterialTheme.colorScheme.surface,
    dropletColor: Color = MaterialTheme.colorScheme.primary,
    iconTint: Color = MaterialTheme.colorScheme.onPrimary,
    iconSize: Dp = 24.dp,
    barHeight: Dp = 56.dp,
    dropletSize: Dp = 40.dp
) {
    val density = LocalDensity.current

    // 1) measure width in px
    var widthPx by remember { mutableStateOf(0) }
    Box(
        Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(barColor)
            .onSizeChanged { widthPx = it.width }
    ) {
        if (widthPx > 0 && icons.isNotEmpty()) {
            // 2) compute center positions in px
            val segmentPx = widthPx.toFloat() / icons.size
            val centersPx = List(icons.size) { i -> segmentPx * (i + 0.5f) }

            // 3) Animatable for droplet X position
            val dropletX = remember { Animatable(centersPx[selectedIndex]) }
            LaunchedEffect(selectedIndex, widthPx) {
                dropletX.animateTo(
                    centersPx[selectedIndex],
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessLow
                    )
                )
            }

            // 4) draw the droplet circle beneath icons
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = dropletColor,
                    radius = with(density) { dropletSize.toPx() / 2f },
                    center = Offset(dropletX.value, size.height / 2)
                )
            }

            // 5) icon row on top
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
                        targetValue = if (isSel) 1.2f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness    = Spring.StiffnessMedium
                        )
                    )
                    Icon(
                        imageVector      = icon,
                        contentDescription= desc,
                        tint             = if (isSel) iconTint else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier         = Modifier
                            .size(iconSize)
                            .graphicsLayer { this.scaleX = scale; this.scaleY = scale }
                            .clickable { onSelected(idx) }
                    )
                }
            }
        }
    }
}
