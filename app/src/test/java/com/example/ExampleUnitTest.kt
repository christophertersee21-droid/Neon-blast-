package com.example

import androidx.compose.ui.graphics.Color
import com.example.data.model.AppConfig
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalRequest
import com.example.data.model.isUserAdminEmail
import com.example.game.engine.BlockShape
import com.example.game.engine.GameBoard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit and Realtime Database schema/model verification tests.
 */
class ExampleUnitTest {

    @Test
    fun testAdminEmailsAuthorization() {
        // Required 3 admin emails
        val admin1 = "chrstphrnch@gmail.com"
        val admin2 = "christopherenoch767@gmail.com"
        val admin3 = "Christophertersee21@gmail.com"

        assertTrue("chrstphrnch@gmail.com must be admin", isUserAdminEmail(admin1))
        assertTrue("christopherenoch767@gmail.com must be admin", isUserAdminEmail(admin2))
        assertTrue("Christophertersee21@gmail.com must be admin (case-insensitive)", isUserAdminEmail(admin3))

        // Normal users must NOT be admin
        assertFalse("Random email should not be admin", isUserAdminEmail("regularplayer@gmail.com"))
        assertFalse("Empty email should not be admin", isUserAdminEmail(""))
        assertFalse("Null email should not be admin", isUserAdminEmail(null))
    }

    @Test
    fun testRealtimeAppConfigDefaults() {
        val config = AppConfig()

        // Minimum withdrawal must be 100 Naira
        assertEquals(100.0, config.minWithdrawalNgn, 0.001)
        assertEquals("NGN", config.currency)
        assertEquals("₦", config.currencySymbol)

        // Nigeria-only access must be active by default
        assertTrue(config.nigeriaOnlyEnabled)

        // AdMob must be active with valid test IDs
        assertTrue(config.adMobEnabled)
        assertTrue(config.adMobBannerId.isNotBlank())
        assertTrue(config.adMobInterstitialId.isNotBlank())
        assertTrue(config.adMobRewardedId.isNotBlank())
        assertTrue(config.rewardedAdCoins > 0)
        assertTrue(config.rewardedAdNgn > 0)
    }

    @Test
    fun testRealtimeUserProfileAndAdminPrivileges() {
        val regularUser = UserProfile(
            userId = "usr_123",
            email = "player@example.com",
            country = "NG",
            eligibleBalance = 150.0
        )
        assertFalse(regularUser.hasAdminPrivileges())
        assertEquals("NG", regularUser.country)

        val adminUser = UserProfile(
            userId = "adm_1",
            email = "chrstphrnch@gmail.com",
            isAdmin = true
        )
        assertTrue(adminUser.hasAdminPrivileges())
    }

    @Test
    fun testWithdrawalRequestValidation() {
        val validWithdrawal = WithdrawalRequest(
            id = "w1",
            userId = "usr_123",
            amount = 100.0,
            currency = "NGN",
            bankName = "Access Bank",
            accountNumber = "0123456789",
            accountName = "Enoch Christopher"
        )
        assertEquals(100.0, validWithdrawal.amount, 0.001)
        assertEquals("NGN", validWithdrawal.currency)
        assertEquals(10, validWithdrawal.accountNumber.length)
        assertTrue(validWithdrawal.amount >= 100.0)
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
