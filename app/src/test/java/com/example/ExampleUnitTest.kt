package com.example

import androidx.compose.ui.graphics.Color
import com.example.game.engine.BlockShape
import com.example.game.engine.GameBoard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testGameBoardPlacementAndLineClearing() {
    val board = GameBoard()
    val dot = BlockShape("dot", "Dot", listOf(0 to 0), 1, 1, Color.Cyan)

    // Place across entire row 0 except col 7
    for (c in 0 until 7) {
      val res = board.place(dot, 0, c, currentCombo = 0)
      assertTrue(res.success)
      assertEquals(0, res.clearedRows.size)
    }

    // Place on row 0, col 7 to complete the row
    val finalRes = board.place(dot, 0, 7, currentCombo = 0)
    assertTrue(finalRes.success)
    assertEquals(1, finalRes.clearedRows.size)
    assertEquals(0, finalRes.clearedRows[0])

    // Verify row 0 is cleared
    for (c in 0 until 8) {
      assertEquals(null, board.grid[0][c])
    }
  }

  @Test
  fun testRejectOverlappingPlacement() {
    val board = GameBoard()
    val dot = BlockShape("dot", "Dot", listOf(0 to 0), 1, 1, Color.Cyan)

    assertTrue(board.canPlace(dot, 3, 3))
    val r1 = board.place(dot, 3, 3, 0)
    assertTrue(r1.success)
    assertFalse(board.canPlace(dot, 3, 3))
  }

  @Test
  fun testMultipleLineClearingSimultaneous() {
    val board = GameBoard()
    val dot = BlockShape("dot", "Dot", listOf(0 to 0), 1, 1, Color.Cyan)

    // Fill row 0 and col 0 except at (0,0)
    for (i in 1 until 8) {
      board.place(dot, 0, i, 0)
      board.place(dot, i, 0, 0)
    }

    // Placing at (0,0) should simultaneously clear row 0 AND col 0
    val res = board.place(dot, 0, 0, 0)
    assertTrue(res.success)
    assertEquals(1, res.clearedRows.size)
    assertEquals(1, res.clearedCols.size)
    assertEquals(0, res.clearedRows[0])
    assertEquals(0, res.clearedCols[0])
  }
}
