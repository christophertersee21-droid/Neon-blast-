package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.game.engine.BlockShape
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonSurface

@Composable
fun PieceTrayView(
    availablePieces: List<BlockShape>,
    selectedPieceIndex: Int?,
    onPieceSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..2) {
            val shape = availablePieces.getOrNull(i)
            val isSelected = selectedPieceIndex == i

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1.0f,
                animationSpec = tween(200),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp)
                    .padding(4.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else NeonSurface.copy(alpha = 0.6f))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) NeonCyan else Color(0x333A4468),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable(enabled = shape != null) {
                        onPieceSelected(i)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (shape != null) {
                    SinglePiecePreview(shape = shape)
                }
            }
        }
    }
}

@Composable
fun SinglePiecePreview(
    shape: BlockShape,
    modifier: Modifier = Modifier
) {
    // Determine canvas box based on shape dimensions
    val maxDim = maxOf(shape.width, shape.height, 3)
    val boxSize = 72.dp

    Box(
        modifier = modifier.size(boxSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(boxSize)) {
            val unit = size.width / (maxDim + 0.5f)
            val padding = unit * 0.1f
            val blockW = unit - padding * 2

            val totalShapeW = shape.width * unit
            val totalShapeH = shape.height * unit
            val startX = (size.width - totalShapeW) / 2f
            val startY = (size.height - totalShapeH) / 2f

            for ((rOff, cOff) in shape.coordinates) {
                val x = startX + cOff * unit + padding
                val y = startY + rOff * unit + padding

                draw3DNeonBlock(
                    x = x,
                    y = y,
                    w = blockW,
                    h = blockW,
                    color = shape.color,
                    isGhost = false
                )
            }
        }
    }
}
