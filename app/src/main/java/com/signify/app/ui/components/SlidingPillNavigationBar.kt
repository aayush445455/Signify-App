// File: app/src/main/java/com/signify/app/ui/components/SlidingPillNavigationBar.kt
package com.signify.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SlidingPillNavigationBar(
    icons: List<Pair<String, ImageVector>>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    barColor: Color = MaterialTheme.colorScheme.surface,
    pillColor: Color = MaterialTheme.colorScheme.primary,
    iconTintOn: Color = MaterialTheme.colorScheme.onPrimary,
    iconTintOff: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    barHeight: Dp = 56.dp,
    pillHeight: Dp = 4.dp,
    pillWidth: Dp = 36.dp,
    iconSize: Dp = 24.dp
) {
    val density = LocalDensity.current

    // ➊ Measure total width in px
    var widthPx by remember { mutableIntStateOf(0) }
    Box(
        Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(barColor)
            .onSizeChanged { widthPx = it.width }
    ) {
        if (widthPx > 0 && icons.isNotEmpty()) {
            // ➋ Compute each segment in Dp
            val segmentDp = with(density) { (widthPx.toFloat() / icons.size).toDp() }

            // ➌ Target pill X offset and animate it
            val targetX = segmentDp * selectedIndex + (segmentDp - pillWidth) / 2
            val animX by animateDpAsState(
                targetValue   = targetX,
                animationSpec = spring(dampingRatio = 0.5f)
            )

            // ➍ Draw the pill
            Box(
                Modifier
                    .offset(x = animX, y = barHeight - pillHeight)
                    .width(pillWidth)
                    .height(pillHeight)
                    .clip(RoundedCornerShape(pillHeight / 2))
                    .background(pillColor)
            )

            // ➎ Draw icons on top
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                icons.forEachIndexed { idx, (desc, icon) ->
                    val isSel = idx == selectedIndex
                    val scale by animateFloatAsState(
                        targetValue = if (isSel) 1.2f else 1f,
                        animationSpec = spring(dampingRatio = 0.5f)
                    )
                    Icon(
                        imageVector      = icon,
                        contentDescription= desc,
                        tint             = if (isSel) iconTintOn else iconTintOff,
                        modifier = Modifier
                            .scale(scale)
                            .size(iconSize)
                            .clickable { onSelected(idx) }
                    )
                }
            }
        }
    }
}
