package io.github.r3rr0r.rechner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object NColors {
    val Black = Color(0xFF000000)
    val Surface = Color(0xFF121212)
    val White = Color(0xFFFFFFFF)
    val Red = Color(0xFFD71921)
    val Grey = Color(0xFF8A8A8A)
}

@Composable
fun RechnerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = NColors.Red,
            onPrimary = NColors.White,
            background = NColors.Black,
            onBackground = NColors.White,
            surface = NColors.Surface,
            onSurface = NColors.White,
        ),
        content = content,
    )
}

/** Transparent "tech" glass: soft white fill gradient + hairline gradient border. */
fun Modifier.glass(shape: Shape, fill: Float = 0.05f, stroke: Float = 0.14f): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = fill * 1.6f), Color.White.copy(alpha = fill * 0.5f))
        )
    )
    .border(
        1.dp,
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = stroke * 1.6f), Color.White.copy(alpha = stroke * 0.4f))
        ),
        shape,
    )
