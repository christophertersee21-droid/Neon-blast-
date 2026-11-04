package com.example.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ThinkingConfig
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "AiStrategyRepo"

class AiStrategyRepository {

    // Mandated: use gemini-4-argon with thinkingLevel set to ThinkingLevel.HIGH
    // Do not set maxOutputTokens
    private val model by lazy {
        val config = generationConfig {
            thinkingConfig = ThinkingConfig.Builder().setThinkingLevel(ThinkingLevel.HIGH).build()
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-4-argon",
            generationConfig = config
        )
    }

    fun analyzeBoardStrategy(
        boardState: String,
        piecesDescription: String,
        currentScore: Long,
        currentCombo: Int
    ): Flow<String> = flow {
        val prompt = """
            You are the Master Strategist and AI Grandmaster for NEON BLAST, an 8x8 neon block puzzle game.
            
            Current Game State:
            - Current Score: $currentScore
            - Active Combo: $currentCombo
            - 8x8 Board Grid (where 'X' = occupied block, '.' = empty cell):
            $boardState
            
            - Available 3 Pieces to place:
            $piecesDescription
            
            Think deeply and carefully through all potential placements, line clear opportunities (horizontal and vertical lines), survival risks, and combo continuation.
            
            Provide:
            1. **Optimal Move Sequence**: Which piece to place first, second, and third, and where (row, col).
            2. **Deep Tactical Analysis**: How this sequence maximizes score, clears lines, or prevents a board lock.
            3. **Pro Tip for Next Moves**: What board spaces to keep clear for large incoming pieces (like 3x3 or 5-lines).
            
            Be clear, inspiring, and concise.
        """.trimIndent()

        try {
            withTimeout(60_000L) {
                model.generateContentStream(prompt).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    if (text.isNotEmpty()) {
                        emit(text)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI thinking strategy generation failed", e)
            emit("AI Strategy Advisor encountered an issue: ${e.localizedMessage ?: "Unable to complete reasoning."}")
        }
    }
}
