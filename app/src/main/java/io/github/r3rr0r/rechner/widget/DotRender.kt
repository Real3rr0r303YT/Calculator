// Rechner – Copyright (C) 2026 Real3rr0r303YT – Made by 3rr0r303YT on TikTok.
// Licensed under the GNU General Public License v3 (see LICENSE).

package io.github.r3rr0r.rechner.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Draws Nothing-style dot-matrix text and dot rings into bitmaps for RemoteViews.
 * Self-contained 5×7 font – no font files, no Compose, works in any widget host.
 */
object DotRender {

    const val WHITE = 0xFFFFFFFF.toInt()
    const val GREY = 0xFF8C8C8C.toInt()
    const val DIM = 0xFF262626.toInt()
    const val RED = 0xFFDA291C.toInt()

    private const val ROWS = 7

    private val FONT: Map<Char, Array<String>> = buildMap {
        fun g(c: Char, vararg rows: String) = put(c, arrayOf(*rows))
        g('0', ".###.", "#...#", "#..##", "#.#.#", "##..#", "#...#", ".###.")
        g('1', "..#..", ".##..", "..#..", "..#..", "..#..", "..#..", ".###.")
        g('2', ".###.", "#...#", "....#", "...#.", "..#..", ".#...", "#####")
        g('3', "#####", "...#.", "..#..", "...#.", "....#", "#...#", ".###.")
        g('4', "...#.", "..##.", ".#.#.", "#..#.", "#####", "...#.", "...#.")
        g('5', "#####", "#....", "####.", "....#", "....#", "#...#", ".###.")
        g('6', "..##.", ".#...", "#....", "####.", "#...#", "#...#", ".###.")
        g('7', "#####", "....#", "...#.", "..#..", ".#...", ".#...", ".#...")
        g('8', ".###.", "#...#", "#...#", ".###.", "#...#", "#...#", ".###.")
        g('9', ".###.", "#...#", "#...#", ".####", "....#", "...#.", ".##..")
        g('A', ".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#")
        g('B', "####.", "#...#", "#...#", "####.", "#...#", "#...#", "####.")
        g('C', ".###.", "#...#", "#....", "#....", "#....", "#...#", ".###.")
        g('D', "####.", "#...#", "#...#", "#...#", "#...#", "#...#", "####.")
        g('E', "#####", "#....", "#....", "####.", "#....", "#....", "#####")
        g('F', "#####", "#....", "#....", "####.", "#....", "#....", "#....")
        g('G', ".###.", "#...#", "#....", "#.###", "#...#", "#...#", ".####")
        g('H', "#...#", "#...#", "#...#", "#####", "#...#", "#...#", "#...#")
        g('I', "###", ".#.", ".#.", ".#.", ".#.", ".#.", "###")
        g('J', "..###", "...#.", "...#.", "...#.", "#..#.", "#..#.", ".##..")
        g('K', "#...#", "#..#.", "#.#..", "##...", "#.#..", "#..#.", "#...#")
        g('L', "#....", "#....", "#....", "#....", "#....", "#....", "#####")
        g('M', "#...#", "##.##", "#.#.#", "#.#.#", "#...#", "#...#", "#...#")
        g('N', "#...#", "#...#", "##..#", "#.#.#", "#..##", "#...#", "#...#")
        g('O', ".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###.")
        g('P', "####.", "#...#", "#...#", "####.", "#....", "#....", "#....")
        g('Q', ".###.", "#...#", "#...#", "#...#", "#.#.#", "#..#.", ".##.#")
        g('R', "####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#")
        g('S', ".####", "#....", "#....", ".###.", "....#", "....#", "####.")
        g('T', "#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#..")
        g('U', "#...#", "#...#", "#...#", "#...#", "#...#", "#...#", ".###.")
        g('V', "#...#", "#...#", "#...#", "#...#", "#...#", ".#.#.", "..#..")
        g('W', "#...#", "#...#", "#...#", "#.#.#", "#.#.#", "#.#.#", ".#.#.")
        g('X', "#...#", "#...#", ".#.#.", "..#..", ".#.#.", "#...#", "#...#")
        g('Y', "#...#", "#...#", ".#.#.", "..#..", "..#..", "..#..", "..#..")
        g('Z', "#####", "....#", "...#.", "..#..", ".#...", "#....", "#####")
        g('Ä', "#...#", ".###.", "#...#", "#...#", "#####", "#...#", "#...#")
        g('Ö', "#...#", ".###.", "#...#", "#...#", "#...#", "#...#", ".###.")
        g('Ü', "#...#", ".....", "#...#", "#...#", "#...#", "#...#", ".###.")
        g(':', ".", ".", "#", ".", "#", ".", ".")
        g('.', ".", ".", ".", ".", ".", ".", "#")
        g(',', ".", ".", ".", ".", ".", "#", "#")
        g('!', "#", "#", "#", "#", "#", ".", "#")
        g('•', "...", "...", ".#.", "###", ".#.", "...", "...")
        g('%', "##...", "##..#", "...#.", "..#..", ".#...", "#..##", "...##")
        g('-', "...", "...", "...", "###", "...", "...", "...")
        g('−', ".....", ".....", ".....", "#####", ".....", ".....", ".....")
        g('+', ".....", "..#..", "..#..", "#####", "..#..", "..#..", ".....")
        g('=', ".....", ".....", "#####", ".....", "#####", ".....", ".....")
        g('×', ".....", "#...#", ".#.#.", "..#..", ".#.#.", "#...#", ".....")
        g('÷', ".....", "..#..", ".....", "#####", ".....", "..#..", ".....")
        g('/', "....#", "....#", "...#.", "..#..", ".#...", "#....", "#....")
        g('(', "..#", ".#.", "#..", "#..", "#..", ".#.", "..#")
        g(')', "#..", ".#.", "..#", "..#", "..#", ".#.", "#..")
        g('?', ".###.", "#...#", "....#", "...#.", "..#..", ".....", "..#..")
        g(' ', "..", "..", "..", "..", "..", "..", "..")
    }

    private fun glyph(c: Char): Array<String> =
        FONT[c] ?: FONT[c.uppercaseChar()] ?: when (c.uppercaseChar()) {
            'À', 'Á', 'Â' -> FONT['A']
            'È', 'É', 'Ê' -> FONT['E']
            'ß' -> FONT['S']
            '*' -> FONT['×']
            else -> null
        } ?: FONT['?']!!

    /** Width of [text] in dot columns (1 empty column between glyphs). */
    fun columns(text: String): Int =
        text.sumOf { glyph(it)[0].length } + (text.length - 1).coerceAtLeast(0)

    /**
     * Renders [text] with dots of [dotPx] diameter. [color] picks the colour per character
     * index (e.g. a red colon), [maxColumns] cuts long text from the left (keeps the end).
     */
    fun text(
        text: String,
        dotPx: Float,
        maxColumns: Int = Int.MAX_VALUE,
        color: (index: Int, char: Char) -> Int = { _, _ -> WHITE },
    ): Bitmap {
        var s = text.ifEmpty { " " }
        while (s.length > 1 && columns(s) > maxColumns) s = s.drop(1)
        val pitch = dotPx * 1.45f
        val cols = columns(s)
        val bmp = Bitmap.createBitmap(
            ceil(cols * pitch).toInt().coerceAtLeast(1),
            ceil(ROWS * pitch).toInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var x0 = 0
        s.forEachIndexed { i, c ->
            val rows = glyph(c)
            paint.color = color(i, c)
            rows.forEachIndexed { y, row ->
                row.forEachIndexed { x, dot ->
                    if (dot == '#') {
                        canvas.drawCircle((x0 + x) * pitch + pitch / 2, y * pitch + pitch / 2, dotPx / 2, paint)
                    }
                }
            }
            x0 += rows[0].length + 1
        }
        return bmp
    }

    /**
     * A ring of [count] dots; the first [fraction] (clockwise from 12 o'clock) in [on],
     * the rest in [off]. Optional [label] in dot-matrix text in the centre.
     */
    fun ring(
        sizePx: Int,
        fraction: Float,
        on: Int,
        off: Int = DIM,
        count: Int = 40,
        label: String? = null,
        labelColor: Int = WHITE,
    ): Bitmap {
        val size = sizePx.coerceAtLeast(16)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val dot = size / 22f
        val r = size / 2f - dot
        val lit = (fraction.coerceIn(0f, 1f) * count).roundToInt()
        for (i in 0 until count) {
            val a = -PI / 2 + 2 * PI * i / count
            paint.color = if (i < lit) on else off
            canvas.drawCircle(size / 2f + (r * cos(a)).toFloat(), size / 2f + (r * sin(a)).toFloat(), dot / 2, paint)
        }
        if (label != null) {
            val maxW = size * 0.62f
            var dotPx = size / 16f
            while (dotPx > 1f && columns(label) * dotPx * 1.45f > maxW) dotPx *= 0.9f
            val t = text(label, dotPx) { _, _ -> labelColor }
            canvas.drawBitmap(t, (size - t.width) / 2f, (size - t.height) / 2f, null)
            t.recycle()
        }
        return bmp
    }
}
