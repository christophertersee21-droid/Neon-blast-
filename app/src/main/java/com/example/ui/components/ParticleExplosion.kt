package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Spark(
    val angle: Double,
    val speed: Float,
    val color: Color,
    val radius: Float
)

@Composable
fun ParticleExplosion(
    isActive: Boolean,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val progress = remember { Animatable(0f) }
    val sparks = remember {
        val colors = listOf(NeonCyan, NeonMagenta, NeonGreen, NeonAmber, Color.White)
        List(40) {
            Spark(
                angle = Random.nextDouble(0.0, Math.PI * 2),
                speed = Random.nextFloat() * 220f + 80f,
                color = colors.random(),
                radius = Random.nextFloat() * 3.5f + 2f
            )
        }
    }

    LaunchedEffect(isActive) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = LinearEasing)
        )
        onAnimationEnd()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val p = progress.value
        val alpha = (1f - p).coerceIn(0f, 1f)

        sparks.forEach { spark ->
            val dist = spark.speed * p
            val x = center.x + (cos(spark.angle) * dist).toFloat()
            val y = center.y + (sin(spark.angle) * dist).toFloat()

            drawCircle(
                color = spark.color.copy(alpha = alpha),
                radius = spark.radius * (1f - p * 0.4f),
                center = Offset(x, y)
            )
        }
    }
}
