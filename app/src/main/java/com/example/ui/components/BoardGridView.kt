package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.game.engine.BOARD_SIZE
import com.example.game.engine.BlockShape
import com.example.ui.theme.NeonCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonSurface

@Composable
fun BoardGridView(
    grid: Array<Array<Color?>>,
    selectedShape: BlockShape?,
    hoverRow: Int?,
    hoverCol: Int?,
    onCellClicked: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val glowAnimation = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) {
        glowAnimation.animateTo(0.8f, tween(1500))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(16.dp, RoundedCornerShape(18.dp), ambientColor = NeonCyan, spotColor = NeonCyan)
            .clip(RoundedCornerShape(18.dp))
            .background(NeonSurface)
            .border(2.dp, Brush.sweepGradient(listOf(NeonCyan, NeonCardBorder, NeonCyan)), RoundedCornerShape(18.dp))
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedShape) {
                    detectTapGestures { offset ->
                        val cellSize = size.width / BOARD_SIZE.toFloat()
                        val col = (offset.x / cellSize).toInt().coerceIn(0, BOARD_SIZE - 1)
                        val row = (offset.y / cellSize).toInt().coerceIn(0, BOARD_SIZE - 1)
                        onCellClicked(row, col)
                    }
                }
        ) {
            val cellW = size.width / BOARD_SIZE.toFloat()
            val cellH = size.height / BOARD_SIZE.toFloat()
            val padding = cellW * 0.08f

            // 1. Draw grid background cells
            for (r in 0 until BOARD_SIZE) {
                for (c in 0 until BOARD_SIZE) {
                    val x = c * cellW + padding
                    val y = r * cellH + padding
                    val w = cellW - padding * 2
                    val h = cellH - padding * 2

                    // Empty slot background
                    drawRoundRect(
                        color = Color(0xFF14172B),
                        topLeft = Offset(x, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(w * 0.18f, h * 0.18f)
                    )

                    drawRoundRect(
                        color = Color(0x33334166),
                        topLeft = Offset(x, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(w * 0.18f, h * 0.18f),
                        style = Stroke(width = 1.2f)
                    )
                }
            }

            // 2. Draw preview of selected shape if hovering or previewing
            if (selectedShape != null && hoverRow != null && hoverCol != null) {
                for ((rOff, cOff) in selectedShape.coordinates) {
                    val r = hoverRow + rOff
                    val c = hoverCol + cOff
                    if (r in 0 until BOARD_SIZE && c in 0 until BOARD_SIZE) {
                        val x = c * cellW + padding
                        val y = r * cellH + padding
                        val w = cellW - padding * 2
                        val h = cellH - padding * 2

                        draw3DNeonBlock(
                            x = x,
                            y = y,
                            w = w,
                            h = h,
                            color = selectedShape.color.copy(alpha = 0.55f),
                            isGhost = true
                        )
                    }
                }
            }

            // 3. Draw occupied blocks with 3D beveled specular lighting
            for (r in 0 until BOARD_SIZE) {
                for (c in 0 until BOARD_SIZE) {
                    val blockColor = grid[r][c] ?: continue
                    val x = c * cellW + padding
                    val y = r * cellH + padding
                    val w = cellW - padding * 2
                    val h = cellH - padding * 2

                    draw3DNeonBlock(x, y, w, h, blockColor, isGhost = false)
                }
            }
        }
    }
}

fun DrawScope.draw3DNeonBlock(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    color: Color,
    isGhost: Boolean = false
) {
    val corner = w * 0.2f
    val bevel = w * 0.16f

    // Outer Glow if not ghost
    if (!isGhost) {
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(x + w / 2f, y + h / 2f),
                radius = w * 0.9f
            ),
            topLeft = Offset(x - w * 0.1f, y - h * 0.1f),
            size = Size(w * 1.2f, h * 1.2f)
        )
    }

    // Base block face
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(corner, corner)
    )

    // 3D Top-Left Highlight (Bevel reflection)
    val highlightPath = Path().apply {
        moveTo(x, y + h)
        lineTo(x + bevel, y + h - bevel)
        lineTo(x + bevel, y + bevel)
        lineTo(x + w - bevel, y + bevel)
        lineTo(x + w, y)
        lineTo(x, y)
        close()
    }
    drawPath(
        path = highlightPath,
        color = Color.White.copy(alpha = if (isGhost) 0.3f else 0.55f)
    )

    // 3D Bottom-Right Shadow
    val shadowPath = Path().apply {
        moveTo(x, y + h)
        lineTo(x + bevel, y + h - bevel)
        lineTo(x + w - bevel, y + h - bevel)
        lineTo(x + w - bevel, y + bevel)
        lineTo(x + w, y)
        lineTo(x + w, y + h)
        close()
    }
    drawPath(
        path = shadowPath,
        color = Color.Black.copy(alpha = 0.45f)
    )

    // Inner glossy glass center
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.35f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.2f)
            ),
            startY = y + bevel,
            endY = y + h - bevel
        ),
        topLeft = Offset(x + bevel, y + bevel),
        size = Size(w - bevel * 2, h - bevel * 2),
        cornerRadius = CornerRadius(corner * 0.6f, corner * 0.6f)
    )

    // Outer crisp neon stroke
    drawRoundRect(
        color = color.copy(alpha = 0.9f),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = 1.5f)
    )
}
