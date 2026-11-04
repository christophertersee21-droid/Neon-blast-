package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import kotlin.random.Random

data class BlockShape(
    val id: String,
    val name: String,
    val coordinates: List<Pair<Int, Int>>, // (rowOffset, colOffset)
    val width: Int,
    val height: Int,
    val color: Color
) {
    val blockCount: Int = coordinates.size

    companion object {
        val ALL_SHAPES = listOf(
            // 1x1 Single
            BlockShape("dot", "Single Dot", listOf(0 to 0), 1, 1, NeonCyan),

            // 2-blocks
            BlockShape("line2_h", "Line 2H", listOf(0 to 0, 0 to 1), 2, 1, NeonBlue),
            BlockShape("line2_v", "Line 2V", listOf(0 to 0, 1 to 0), 1, 2, NeonBlue),

            // 3-blocks
            BlockShape("line3_h", "Line 3H", listOf(0 to 0, 0 to 1, 0 to 2), 3, 1, NeonGreen),
            BlockShape("line3_v", "Line 3V", listOf(0 to 0, 1 to 0, 2 to 0), 1, 3, NeonGreen),
            BlockShape("corner_br", "Corner BR", listOf(0 to 0, 1 to 0, 1 to 1), 2, 2, NeonAmber),
            BlockShape("corner_bl", "Corner BL", listOf(0 to 1, 1 to 0, 1 to 1), 2, 2, NeonAmber),
            BlockShape("corner_tr", "Corner TR", listOf(0 to 0, 0 to 1, 1 to 0), 2, 2, NeonAmber),
            BlockShape("corner_tl", "Corner TL", listOf(0 to 0, 0 to 1, 1 to 1), 2, 2, NeonAmber),

            // 4-blocks
            BlockShape("square2", "Square 2x2", listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1), 2, 2, NeonMagenta),
            BlockShape("line4_h", "Line 4H", listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3), 4, 1, NeonPurple),
            BlockShape("line4_v", "Line 4V", listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0), 1, 4, NeonPurple),
            BlockShape("t_down", "T-Shape", listOf(0 to 0, 0 to 1, 0 to 2, 1 to 1), 3, 2, NeonCyan),
            BlockShape("t_up", "T-Up", listOf(1 to 0, 1 to 1, 1 to 2, 0 to 1), 3, 2, NeonCyan),
            BlockShape("l_normal", "L-Normal", listOf(0 to 0, 1 to 0, 2 to 0, 2 to 1), 2, 3, NeonBlue),
            BlockShape("l_inv", "L-Inverted", listOf(0 to 1, 1 to 1, 2 to 1, 2 to 0), 2, 3, NeonBlue),

            // 5-blocks & 3x3
            BlockShape("line5_h", "Line 5H", listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4), 5, 1, NeonGreen),
            BlockShape("line5_v", "Line 5V", listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0), 1, 5, NeonGreen),
            BlockShape("big_l", "Big Corner", listOf(0 to 0, 1 to 0, 2 to 0, 2 to 1, 2 to 2), 3, 3, NeonAmber),
            BlockShape("square3", "Square 3x3", (0..2).flatMap { r -> (0..2).map { c -> r to c } }, 3, 3, NeonMagenta)
        )

        fun randomShapes(count: Int = 3): List<BlockShape> {
            val pool = ALL_SHAPES.filter { it.id != "square3" || Random.nextFloat() < 0.15f }
            return (0 until count).map { pool.random() }
        }
    }
}
