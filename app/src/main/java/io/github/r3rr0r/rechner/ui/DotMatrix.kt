package io.github.r3rr0r.rechner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp

/** 5×7 dot-matrix font, inspired by the Nothing Glyph / Ear LED look. */
internal object DotFont {
    const val ROWS = 7
    private const val DOT_RATIO = 0.36f

    private val glyphs: Map<Char, Array<String>> = mapOf(
        '0' to arrayOf(".###.", "#...#", "#..##", "#.#.#", "##..#", "#...#", ".###."),
        '1' to arrayOf("..#..", ".##..", "..#..", "..#..", "..#..", "..#..", ".###."),
        '2' to arrayOf(".###.", "#...#", "....#", "...#.", "..#..", ".#...", "#####"),
        '3' to arrayOf("#####", "...#.", "..#..", "...#.", "....#", "#...#", ".###."),
        '4' to arrayOf("...#.", "..##.", ".#.#.", "#..#.", "#####", "...#.", "...#."),
        '5' to arrayOf("#####", "#....", "####.", "....#", "....#", "#...#", ".###."),
        '6' to arrayOf("..##.", ".#...", "#....", "####.", "#...#", "#...#", ".###."),
        '7' to arrayOf("#####", "....#", "...#.", "..#..", ".#...", ".#...", ".#..."),
        '8' to arrayOf(".###.", "#...#", "#...#", ".###.", "#...#", "#...#", ".###."),
        '9' to arrayOf(".###.", "#...#", "#...#", ".####", "....#", "...#.", ".##.."),
        '.' to arrayOf(".", ".", ".", ".", ".", ".", "#"),
        '−' to arrayOf(".....", ".....", ".....", "#####", ".....", ".....", "....."),
        '+' to arrayOf(".....", "..#..", "..#..", "#####", "..#..", "..#..", "....."),
        '×' to arrayOf(".....", "#...#", ".#.#.", "..#..", ".#.#.", "#...#", "....."),
        '÷' to arrayOf(".....", "..#..", ".....", "#####", ".....", "..#..", "....."),
        '=' to arrayOf(".....", ".....", "#####", ".....", "#####", ".....", "....."),
        '±' to arrayOf("..#..", "..#..", "#####", "..#..", "..#..", ".....", "#####"),
        '%' to arrayOf("##...", "##..#", "...#.", "..#..", ".#...", "#..##", "...##"),
        '(' to arrayOf("..#", ".#.", "#..", "#..", "#..", ".#.", "..#"),
        ')' to arrayOf("#..", ".#.", "..#", "..#", "..#", ".#.", "#.."),
        'A' to arrayOf(".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#"),
        'C' to arrayOf(".###.", "#...#", "#....", "#....", "#....", "#...#", ".###."),
        'E' to arrayOf("#####", "#....", "#....", "####.", "#....", "#....", "#####"),
        'H' to arrayOf("#...#", "#...#", "#...#", "#####", "#...#", "#...#", "#...#"),
        'N' to arrayOf("#...#", "##..#", "#.#.#", "#.#.#", "#..##", "#...#", "#...#"),
        'R' to arrayOf("####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#"),
        'r' to arrayOf(".....", ".....", "#.##.", "##..#", "#....", "#....", "#...."),
        'o' to arrayOf(".....", ".....", ".###.", "#...#", "#...#", "#...#", ".###."),
        ' ' to arrayOf("...", "...", "...", "...", "...", "...", "..."),
    )

    fun glyph(c: Char): Array<String> = glyphs[c] ?: glyphs.getValue(' ')

    fun columns(text: String): Int =
        if (text.isEmpty()) 0 else text.sumOf { glyph(it)[0].length } + text.length - 1

    fun DrawScope.drawDots(text: String, pitch: Float, color: Color, ghost: Color?, x0: Float, y0: Float) {
        val r = pitch * DOT_RATIO
        var x = x0
        for (ch in text) {
            val g = glyph(ch)
            val w = g[0].length
            for (row in 0 until ROWS) for (col in 0 until w) {
                val c = if (g[row][col] == '#') color else ghost ?: continue
                drawCircle(c, r, Offset(x + col * pitch + pitch / 2, y0 + row * pitch + pitch / 2))
            }
            x += (w + 1) * pitch
        }
    }
}

/** Dot-matrix text with a fixed dot pitch (button labels, header). */
@Composable
fun DotText(text: String, pitch: Dp, color: Color, modifier: Modifier = Modifier, ghost: Color? = null) {
    val cols = DotFont.columns(text)
    Canvas(modifier.size(pitch * cols, pitch * DotFont.ROWS)) {
        with(DotFont) { drawDots(text, pitch.toPx(), color, ghost, 0f, 0f) }
    }
}

/** Right-aligned dot-matrix display that shrinks its pitch to fit the available width. */
@Composable
fun DotDisplay(text: String, maxPitch: Dp, color: Color, modifier: Modifier = Modifier, ghost: Color? = null) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val cols = DotFont.columns(text).coerceAtLeast(1)
        val pitch = minOf(maxPitch, maxWidth / cols)
        Canvas(Modifier.fillMaxWidth().height(maxPitch * DotFont.ROWS)) {
            val p = pitch.toPx()
            with(DotFont) {
                drawDots(text, p, color, ghost, size.width - p * cols, size.height - p * DotFont.ROWS)
            }
        }
    }
}

/** Small pixel icons drawn as dots. '#' and 'r' map to colours. */
object DotIcons {
    val COFFEE = listOf(
        ".r..r....",
        "..r..r...",
        ".r..r....",
        "#######..",
        "#.....###",
        "#.....#.#",
        "#.....###",
        ".#...#...",
        "..###....",
    )
    val HEART = listOf(
        ".##...##.",
        "####.####",
        "#########",
        "#########",
        ".#######.",
        "..#####..",
        "...###...",
        "....#....",
    )
    val BACKSPACE = listOf(
        "...######",
        "..#.....#",
        ".#..#.#.#",
        "#....#..#",
        ".#..#.#.#",
        "..#.....#",
        "...######",
    )
}

@Composable
fun DotIcon(pattern: List<String>, pitch: Dp, colors: Map<Char, Color>, modifier: Modifier = Modifier) {
    val rows = pattern.size
    val cols = pattern.maxOf { it.length }
    Canvas(modifier.size(pitch * cols, pitch * rows)) {
        val p = pitch.toPx()
        val r = p * 0.36f
        pattern.forEachIndexed { y, line ->
            line.forEachIndexed { x, ch ->
                colors[ch]?.let { drawCircle(it, r, Offset(x * p + p / 2, y * p + p / 2)) }
            }
        }
    }
}
