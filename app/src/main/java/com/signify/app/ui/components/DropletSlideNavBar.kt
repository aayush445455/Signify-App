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
fun DropletSlideNavBar(
    icons: List<Pair<String, ImageVector>>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    barColor: Color = MaterialTheme.colorScheme.surface,
    dropletColor: Color = MaterialTheme.colorScheme.primary,
    iconOnDroplet: Color = MaterialTheme.colorScheme.onPrimary,
    iconOff: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    barHeight: Dp = 60.dp,
    dropletSize: Dp = 48.dp,
    iconSize: Dp = 24.dp
) {
    val density = LocalDensity.current
    var widthPx by remember { mutableStateOf(0) }

    Box(
        Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(barColor)
            .onSizeChanged { widthPx = it.width }
    ) {
        if (widthPx > 0 && icons.isNotEmpty()) {
            // 1) compute center positions
            val segment = widthPx.toFloat() / icons.size
            val centers = List(icons.size) { i -> segment * (i + .5f) }

            // 2) Animatable for droplet X
            val dropletX = remember { Animatable(centers[selectedIndex]) }
            LaunchedEffect(selectedIndex, widthPx) {
                dropletX.animateTo(
                    centers[selectedIndex],
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessLow
                    )
                )
            }

            // 3) draw droplet circle
            Canvas(Modifier.fillMaxSize()) {
                val px = with(density) { dropletSize.toPx() / 2f }
                drawCircle(
                    color  = dropletColor,
                    radius = px,
                    center = Offset(dropletX.value, size.height / 2f)
                )
            }

            // 4) draw icons on top
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                icons.forEachIndexed { idx, (desc, icon) ->
                    val isSel = idx == selectedIndex
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
                        tint             = if (isSel) iconOnDroplet else iconOff,
                        modifier         = Modifier
                            .size(iconSize)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clickable { onSelected(idx) }
                    )
                }
            }
        }
    }
}
