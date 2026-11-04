package com.example.game.engine

import androidx.compose.ui.graphics.Color

const val BOARD_SIZE = 8

data class PlacementResult(
    val success: Boolean,
    val clearedRows: List<Int> = emptyList(),
    val clearedCols: List<Int> = emptyList(),
    val pointsEarned: Long = 0L,
    val newCombo: Int = 0,
    val isComboActive: Boolean = false
)

class GameBoard(
    val size: Int = BOARD_SIZE,
    initialGrid: Array<Array<Color?>>? = null
) {
    val grid: Array<Array<Color?>> = initialGrid?.map { it.clone() }?.toTypedArray()
        ?: Array(size) { arrayOfNulls<Color?>(size) }

    fun copy(): GameBoard {
        val newGrid = Array(size) { r ->
            Array(size) { c -> grid[r][c] }
        }
        return GameBoard(size, newGrid)
    }

    fun canPlace(shape: BlockShape, startRow: Int, startCol: Int): Boolean {
        for ((rOff, cOff) in shape.coordinates) {
            val r = startRow + rOff
            val c = startCol + cOff
            if (r !in 0 until size || c !in 0 until size) {
                return false
            }
            if (grid[r][c] != null) {
                return false
            }
        }
        return true
    }

    fun canAnyFit(shapes: List<BlockShape>): Boolean {
        if (shapes.isEmpty()) return true
        for (shape in shapes) {
            for (r in 0 until size) {
                for (c in 0 until size) {
                    if (canPlace(shape, r, c)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun place(shape: BlockShape, startRow: Int, startCol: Int, currentCombo: Int): PlacementResult {
        if (!canPlace(shape, startRow, startCol)) {
            return PlacementResult(success = false)
        }

        // Place blocks
        for ((rOff, cOff) in shape.coordinates) {
            grid[startRow + rOff][startCol + cOff] = shape.color
        }

        // Check full rows
        val fullRows = mutableListOf<Int>()
        for (r in 0 until size) {
            if (grid[r].all { it != null }) {
                fullRows.add(r)
            }
        }

        // Check full columns
        val fullCols = mutableListOf<Int>()
        for (c in 0 until size) {
            var colFull = true
            for (r in 0 until size) {
                if (grid[r][c] == null) {
                    colFull = false
                    break
                }
            }
            if (colFull) {
                fullCols.add(c)
            }
        }

        // Clear cells
        for (r in fullRows) {
            for (c in 0 until size) {
                grid[r][c] = null
            }
        }
        for (c in fullCols) {
            for (r in 0 until size) {
                grid[r][c] = null
            }
        }

        val totalLines = fullRows.size + fullCols.size
        val newCombo = if (totalLines > 0) currentCombo + 1 else 0
        val isComboActive = newCombo > 1

        // Calculate score
        val baseScore = shape.blockCount * 10L
        val lineScore = if (totalLines > 0) {
            val lineBase = totalLines * 100L
            val multiLineBonus = if (totalLines > 1) (totalLines * 50L * totalLines) else 0L
            val comboBonus = if (newCombo > 1) (newCombo * 30L) else 0L
            lineBase + multiLineBonus + comboBonus
        } else 0L

        return PlacementResult(
            success = true,
            clearedRows = fullRows,
            clearedCols = fullCols,
            pointsEarned = baseScore + lineScore,
            newCombo = newCombo,
            isComboActive = isComboActive
        )
    }

    fun applySecondChance() {
        // Clear a 4x4 center area to give space
        for (r in 2..5) {
            for (c in 2..5) {
                grid[r][c] = null
            }
        }
    }

    fun getBoardStateString(): String {
        val sb = StringBuilder()
        for (r in 0 until size) {
            for (c in 0 until size) {
                sb.append(if (grid[r][c] != null) "X" else ".")
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}
