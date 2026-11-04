package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import kotlin.random.Random

private data class Star(val x: Float, val y: Float, val radius: Float, val alpha: Float, val speed: Float)

@Composable
fun NeonGalaxyBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val stars = remember {
        List(60) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 1.8f + 0.6f,
                alpha = Random.nextFloat() * 0.6f + 0.2f,
                speed = Random.nextFloat() * 0.4f + 0.2f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Dark galaxy gradient background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NeonBackground,
                        Color(0xFF0C0E1E),
                        Color(0xFF0F1126),
                        NeonBackground
                    )
                )
            )

            // Subtle neon nebula glow spots
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonPurple.copy(alpha = 0.12f * (0.8f + pulse * 0.4f)), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.15f),
                    radius = size.width * 0.6f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.08f * (0.8f + pulse * 0.4f)), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.75f),
                    radius = size.width * 0.65f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonMagenta.copy(alpha = 0.07f * (0.8f + pulse * 0.3f)), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.45f),
                    radius = size.width * 0.5f
                )
            )

            // Animated stars
            stars.forEach { star ->
                val starPulse = (star.alpha + (pulse * star.speed)).coerceIn(0.1f, 1f)
                drawCircle(
                    color = Color.White.copy(alpha = starPulse),
                    radius = star.radius,
                    center = Offset(star.x * size.width, star.y * size.height)
                )
            }
        }

        content()
    }
}
