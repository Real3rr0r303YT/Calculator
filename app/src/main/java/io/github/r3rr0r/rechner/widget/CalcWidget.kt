// Rechner – Copyright (C) 2026 Real3rr0r303YT – Made by 3rr0r303YT on TikTok.
// Licensed under the GNU General Public License v3 (see LICENSE).

package io.github.r3rr0r.rechner.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.r3rr0r.rechner.MainActivity
import io.github.r3rr0r.rechner.R
import io.github.r3rr0r.rechner.calc.CalcLogic
import io.github.r3rr0r.rechner.calc.CalcState
import io.github.r3rr0r.rechner.calc.Key

/**
 * A working calculator on the home screen: dot-matrix display and the full keypad.
 * Uses the same [CalcLogic] as the app; the input is kept in private SharedPreferences.
 */
class CalcWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        render(context, manager, ids, load(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_KEY) return
        val key = keyFor(intent.getStringExtra(EXTRA_KEY) ?: return) ?: return
        val state = CalcLogic.reduce(load(context), key)
        save(context, state)
        val manager = AppWidgetManager.getInstance(context)
        render(context, manager, manager.getAppWidgetIds(ComponentName(context, CalcWidget::class.java)), state)
    }

    companion object {
        private const val ACTION_KEY = "io.github.r3rr0r.rechner.widget.KEY"
        private const val EXTRA_KEY = "key"
        private const val PREFS = "calc_widget"

        /** Button id → label, row by row (same layout as the app). */
        private val KEYS = listOf(
            R.id.k_clear to "C", R.id.k_paren to "()", R.id.k_percent to "%", R.id.k_div to "÷",
            R.id.k7 to "7", R.id.k8 to "8", R.id.k9 to "9", R.id.k_mul to "×",
            R.id.k4 to "4", R.id.k5 to "5", R.id.k6 to "6", R.id.k_sub to "−",
            R.id.k1 to "1", R.id.k2 to "2", R.id.k3 to "3", R.id.k_add to "+",
            R.id.k0 to "0", R.id.k_dot to ".", R.id.k_back to "⌫", R.id.k_eq to "=",
        )

        private fun keyFor(label: String): Key? = when (label) {
            "C" -> Key.Clear
            "()" -> Key.Paren
            "%" -> Key.Percent
            "." -> Key.Dot
            "⌫" -> Key.Back
            "=" -> Key.Equals
            "÷", "×", "−", "+" -> Key.Op(label[0])
            else -> label.singleOrNull()?.takeIf { it.isDigit() }?.let { Key.Digit(it) }
        }

        private fun load(context: Context): CalcState {
            val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            return CalcState(
                expression = p.getString("expression", "") ?: "",
                preview = p.getString("preview", null),
                evaluated = p.getBoolean("evaluated", false),
                error = p.getBoolean("error", false),
            )
        }

        private fun save(context: Context, s: CalcState) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("expression", s.expression)
                .putString("preview", s.preview)
                .putBoolean("evaluated", s.evaluated)
                .putBoolean("error", s.error)
                .apply()
        }

        private fun render(context: Context, manager: AppWidgetManager, ids: IntArray, s: CalcState) {
            if (ids.isEmpty()) return
            val density = context.resources.displayMetrics.density
            val views = RemoteViews(context.packageName, R.layout.widget_calc)
            val shown = when {
                s.error -> "ERROR"
                s.expression.isEmpty() -> "0"
                else -> s.expression
            }
            views.setImageViewBitmap(
                R.id.widget_display,
                DotRender.text(shown, 6f * density, maxColumns = 70) { _, c ->
                    when {
                        s.error -> DotRender.RED
                        c in "+−×÷%()" -> DotRender.RED
                        else -> DotRender.WHITE
                    }
                },
            )
            views.setContentDescription(R.id.widget_display, shown)
            views.setTextViewText(R.id.widget_preview, s.preview?.let { "= $it" } ?: "")

            KEYS.forEachIndexed { i, (id, label) ->
                views.setOnClickPendingIntent(
                    id,
                    PendingIntent.getBroadcast(
                        context, i,
                        Intent(context, CalcWidget::class.java).setAction(ACTION_KEY).putExtra(EXTRA_KEY, label),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    ),
                )
            }
            val open = PendingIntent.getActivity(
                context, 100,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            views.setOnClickPendingIntent(R.id.widget_display, open)
            manager.updateAppWidget(ids, views)
        }
    }
}
