package com.example.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.AiStrategyRepository
import com.example.data.repository.FirestoreRepository
import com.example.game.engine.BlockShape
import com.example.game.engine.GameBoard
import com.example.ui.components.BoardGridView
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonCard
import com.example.ui.components.NeonGalaxyBackground
import com.example.ui.components.NeonOutlinedButton
import com.example.ui.components.ParticleExplosion
import com.example.ui.components.PieceTrayView
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonSurface
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    user: UserProfile,
    repository: FirestoreRepository,
    aiRepository: AiStrategyRepository,
    onBackClicked: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var board by remember { mutableStateOf(GameBoard()) }
    var availablePieces by remember { mutableStateOf(BlockShape.randomShapes(3)) }
    var selectedPieceIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableLongStateOf(0L) }
    var linesClearedTotal by remember { mutableLongStateOf(0L) }
    var currentCombo by remember { mutableIntStateOf(0) }
    var maxCombo by remember { mutableIntStateOf(0) }

    var showExplosion by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(false) }
    var showSecondChanceDialog by remember { mutableStateOf(false) }
    var usedSecondChance by remember { mutableStateOf(false) }

    // AI Advisor Sheet
    var showAiAdvisor by remember { mutableStateOf(false) }
    var aiStrategyText by remember { mutableStateOf("") }
    var isAiLoading by remember { mutableStateOf(false) }

    fun checkGameOverOrNewPieces(currentPieces: List<BlockShape>) {
        if (currentPieces.isEmpty()) {
            val newShapes = BlockShape.randomShapes(3)
            availablePieces = newShapes
            selectedPieceIndex = null

            if (!board.canAnyFit(newShapes)) {
                if (!usedSecondChance && user.gameCoins >= 50) {
                    showSecondChanceDialog = true
                } else {
                    showGameOverDialog = true
                }
            }
        } else {
            if (!board.canAnyFit(currentPieces)) {
                if (!usedSecondChance && user.gameCoins >= 50) {
                    showSecondChanceDialog = true
                } else {
                    showGameOverDialog = true
                }
            }
        }
    }

    fun restartGame() {
        board = GameBoard()
        availablePieces = BlockShape.randomShapes(3)
        selectedPieceIndex = null
        score = 0L
        linesClearedTotal = 0L
        currentCombo = 0
        maxCombo = 0
        usedSecondChance = false
        showGameOverDialog = false
        showSecondChanceDialog = false
    }

    LaunchedEffect(showGameOverDialog) {
        if (showGameOverDialog && score > 0) {
            repository.recordGameSession(
                score = score,
                linesCleared = linesClearedTotal,
                combos = maxCombo.toLong(),
                durationSeconds = 60L
            )
        }
    }

    NeonGalaxyBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top HUD
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NeonCyan
                            )
                        }

                        // Score & Best
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SCORE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "$score",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = NeonCyan
                            )
                        }

                        // AI Advisor Button
                        IconButton(
                            onClick = {
                                showAiAdvisor = true
                                isAiLoading = true
                                aiStrategyText = ""
                                scope.launch {
                                    val piecesDesc = availablePieces.joinToString(", ") { "${it.name} (${it.width}x${it.height})" }
                                    aiRepository.analyzeBoardStrategy(
                                        boardState = board.getBoardStateString(),
                                        piecesDescription = piecesDesc.ifBlank { "None remaining in tray" },
                                        currentScore = score,
                                        currentCombo = currentCombo
                                    ).collect { chunk ->
                                        isAiLoading = false
                                        aiStrategyText += chunk
                                    }
                                    isAiLoading = false
                                }
                            },
                            modifier = Modifier.testTag("ai_advisor_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Strategy Advisor",
                                tint = NeonMagenta
                            )
                        }
                    }

                    // Stat Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonSurface)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Best: ${maxOf(score, user.bestScore)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonGold
                            )
                        }

                        if (currentCombo > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonMagenta.copy(alpha = 0.25f))
                                    .border(1.dp, NeonMagenta, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "COMBO x$currentCombo 🔥",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeonMagenta
                                )
                            }
                        }
                    }
                }

                // 8x8 Board Grid
                val selectedShape = selectedPieceIndex?.let { availablePieces.getOrNull(it) }

                BoardGridView(
                    grid = board.grid,
                    selectedShape = selectedShape,
                    hoverRow = null,
                    hoverCol = null,
                    onCellClicked = { r, c ->
                        val shape = selectedShape
                        if (shape != null) {
                            if (board.canPlace(shape, r, c)) {
                                val result = board.place(shape, r, c, currentCombo)
                                if (result.success) {
                                    score += result.pointsEarned
                                    linesClearedTotal += (result.clearedRows.size + result.clearedCols.size)
                                    currentCombo = result.newCombo
                                    if (currentCombo > maxCombo) maxCombo = currentCombo

                                    if (result.clearedRows.isNotEmpty() || result.clearedCols.isNotEmpty()) {
                                        showExplosion = true
                                    }

                                    // Remove placed piece
                                    val mutableList = availablePieces.toMutableList()
                                    selectedPieceIndex?.let { mutableList.removeAt(it) }
                                    availablePieces = mutableList
                                    selectedPieceIndex = if (mutableList.isNotEmpty()) 0 else null

                                    checkGameOverOrNewPieces(mutableList)
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Bottom Piece Tray
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (selectedShape != null) "Tap any board cell to place '${selectedShape.name}'" else "Select a block piece below",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedShape != null) NeonCyan else TextSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    PieceTrayView(
                        availablePieces = availablePieces,
                        selectedPieceIndex = selectedPieceIndex,
                        onPieceSelected = { index ->
                            selectedPieceIndex = if (selectedPieceIndex == index) null else index
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Particle Explosion Layer
            ParticleExplosion(
                isActive = showExplosion,
                onAnimationEnd = { showExplosion = false }
            )
        }
    }

    // Second Chance Dialog
    if (showSecondChanceDialog) {
        AlertDialog(
            onDismissRequest = {
                showSecondChanceDialog = false
                showGameOverDialog = true
            },
            title = {
                Text("SECOND CHANCE? ⚡", color = NeonCyan, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "You're out of moves! Use 50 Game Coins to clear the center 4x4 area and keep your score streak alive!",
                    color = Color.White
                )
            },
            confirmButton = {
                NeonButton(
                    text = "Revive (50 🪙)",
                    onClick = {
                        scope.launch {
                            val success = repository.deductCoinsForSecondChance(50L)
                            if (success) {
                                board.applySecondChance()
                                usedSecondChance = true
                                showSecondChanceDialog = false
                            } else {
                                showSecondChanceDialog = false
                                showGameOverDialog = true
                            }
                        }
                    }
                )
            },
            dismissButton = {
                NeonOutlinedButton(
                    text = "End Game",
                    glowColor = NeonMagenta,
                    onClick = {
                        showSecondChanceDialog = false
                        showGameOverDialog = true
                    }
                )
            },
            containerColor = NeonSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Game Over Dialog
    if (showGameOverDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("GAME OVER", color = NeonMagenta, fontWeight = FontWeight.Black)
            },
            text = {
                Column {
                    Text("Final Score: $score", color = NeonCyan, style = MaterialTheme.typography.titleLarge)
                    Text("Lines Cleared: $linesClearedTotal", color = Color.White)
                    Text("Max Combo: x$maxCombo", color = NeonAmber)
                    Text("Coins Earned: +${score / 10L} 🪙", color = NeonGold)
                    if (score > user.bestScore) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🎉 NEW PERSONAL BEST!", color = NeonGreen, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                NeonButton(
                    text = "Play Again",
                    onClick = { restartGame() }
                )
            },
            dismissButton = {
                NeonOutlinedButton(
                    text = "Menu",
                    glowColor = TextSecondary,
                    onClick = onBackClicked
                )
            },
            containerColor = NeonSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // AI Strategy Master Bottom Sheet (Thinking Mode with gemini-4-argon)
    if (showAiAdvisor) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAiAdvisor = false },
            sheetState = sheetState,
            containerColor = NeonSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = NeonMagenta,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AI Puzzle Master",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = NeonCyan
                            )
                            Text(
                                text = "Powered by Gemini 4 Argon • High Thinking",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonMagenta
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            isAiLoading = true
                            aiStrategyText = ""
                            scope.launch {
                                val piecesDesc = availablePieces.joinToString(", ") { "${it.name} (${it.width}x${it.height})" }
                                aiRepository.analyzeBoardStrategy(
                                    boardState = board.getBoardStateString(),
                                    piecesDescription = piecesDesc.ifBlank { "None remaining in tray" },
                                    currentScore = score,
                                    currentCombo = currentCombo
                                ).collect { chunk ->
                                    isAiLoading = false
                                    aiStrategyText += chunk
                                }
                                isAiLoading = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Advice",
                            tint = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isAiLoading && aiStrategyText.isBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = NeonCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing board geometry and tactical move sequences...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                } else {
                    NeonCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = NeonPurple
                    ) {
                        Text(
                            text = aiStrategyText.ifBlank { "Ready to analyze current board." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
