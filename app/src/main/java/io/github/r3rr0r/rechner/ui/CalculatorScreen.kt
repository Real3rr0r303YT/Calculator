package io.github.r3rr0r.rechner.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.r3rr0r.rechner.CalculatorViewModel
import io.github.r3rr0r.rechner.calc.CalcState
import io.github.r3rr0r.rechner.calc.Key

const val SUPPORT_URL = "https://buymeacoffee.com/3rr0r303YT"

private val Mono = FontFamily.Monospace

@Composable
fun CalculatorScreen(vm: CalculatorViewModel = viewModel()) {
    var showSupport by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(NColors.Black)) {
        Backdrop()
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Header(onSupport = { showSupport = true })
            Spacer(Modifier.height(16.dp))
            Display(
                state = vm.state,
                onBack = { vm.onKey(Key.Back) },
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
            Spacer(Modifier.height(18.dp))
            Keypad(onKey = vm::onKey)
        }
        if (showSupport) SupportDialog(onDismiss = { showSupport = false })
    }
}

/* ---------- Background: dot grid + soft glows for the glass to refract ---------- */

@Composable
private fun Backdrop() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            Brush.radialGradient(
                listOf(NColors.Red.copy(alpha = 0.20f), Color.Transparent),
                center = Offset(size.width * 0.95f, size.height * 0.12f),
                radius = size.width * 0.85f,
            )
        )
        drawRect(
            Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.06f), Color.Transparent),
                center = Offset(0f, size.height * 0.5f),
                radius = size.width,
            )
        )
        val step = 22.dp.toPx()
        val r = 1.dp.toPx()
        val dot = Color.White.copy(alpha = 0.07f)
        var y = step / 2
        while (y < size.height) {
            var x = step / 2
            while (x < size.width) {
                drawCircle(dot, r, Offset(x, y)); x += step
            }
            y += step
        }
    }
}

/* ---------- Header ---------- */

@Composable
private fun Header(onSupport: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
        DotText("RECHNER", pitch = 2.6.dp, color = NColors.White)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(6.dp).clip(CircleShape).background(NColors.Red))
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .size(48.dp)
                .glass(CircleShape)
                .clickable(onClick = onSupport)
                .semantics { contentDescription = "Support Development" },
            contentAlignment = Alignment.Center,
        ) {
            DotIcon(
                DotIcons.COFFEE,
                pitch = 2.5.dp,
                colors = mapOf('#' to NColors.White, 'r' to NColors.Red),
            )
        }
    }
}

/* ---------- Display ---------- */

@Composable
private fun Display(state: CalcState, onBack: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(32.dp)
    val mainColor by animateColorAsState(
        when {
            state.error -> NColors.Red
            state.evaluated -> NColors.White
            else -> NColors.White.copy(alpha = 0.45f)
        },
        label = "mainColor",
    )

    Column(
        modifier.glass(shape).padding(20.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(NColors.Red))
            Spacer(Modifier.width(8.dp))
            Text("STD · LIVE", color = NColors.Grey, fontFamily = Mono, fontSize = 11.sp, letterSpacing = 3.sp)
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack)
                    .semantics { contentDescription = "Löschen" },
                contentAlignment = Alignment.Center,
            ) {
                DotIcon(DotIcons.BACKSPACE, pitch = 2.4.dp, colors = mapOf('#' to NColors.White.copy(alpha = 0.8f)))
            }
        }

        Spacer(Modifier.weight(1f))

        val exprText: String
        val exprColor: Color
        if (state.evaluated) {
            exprText = "${state.history.orEmpty()} ="
            exprColor = NColors.Grey
        } else {
            exprText = state.expression.ifEmpty { "0" }
            exprColor = if (state.expression.isEmpty()) NColors.Grey else NColors.White
        }
        val fs = when {
            exprText.length > 24 -> 22.sp
            exprText.length > 14 -> 28.sp
            else -> 36.sp
        }
        Text(
            text = exprText,
            color = exprColor,
            fontFamily = Mono,
            fontWeight = FontWeight.Light,
            fontSize = fs,
            lineHeight = fs * 1.15f,
            textAlign = TextAlign.End,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(14.dp))
        DashedLine()
        Spacer(Modifier.height(14.dp))

        val mainText = when {
            state.error -> "Error"
            state.evaluated -> state.expression
            else -> state.preview?.let { "=$it" }.orEmpty()
        }
        DotDisplay(
            text = mainText,
            maxPitch = 7.dp,
            color = mainColor,
            ghost = Color.White.copy(alpha = 0.05f),
        )
    }
}

@Composable
private fun DashedLine() {
    Canvas(Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = Color.White.copy(alpha = 0.18f),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 4.dp.toPx())),
        )
    }
}

/* ---------- Keypad ---------- */

private enum class KeyStyle { Number, Function, Operator, Accent }

private data class KeyDef(val label: String, val style: KeyStyle, val key: Key, val desc: String = label)

private val KEYS = listOf(
    listOf(
        KeyDef("AC", KeyStyle.Function, Key.Clear, "Alles löschen"),
        KeyDef("( )", KeyStyle.Function, Key.Paren, "Klammer"),
        KeyDef("%", KeyStyle.Function, Key.Percent, "Prozent"),
        KeyDef("÷", KeyStyle.Operator, Key.Op('÷'), "geteilt"),
    ),
    listOf(
        KeyDef("7", KeyStyle.Number, Key.Digit('7')),
        KeyDef("8", KeyStyle.Number, Key.Digit('8')),
        KeyDef("9", KeyStyle.Number, Key.Digit('9')),
        KeyDef("×", KeyStyle.Operator, Key.Op('×'), "mal"),
    ),
    listOf(
        KeyDef("4", KeyStyle.Number, Key.Digit('4')),
        KeyDef("5", KeyStyle.Number, Key.Digit('5')),
        KeyDef("6", KeyStyle.Number, Key.Digit('6')),
        KeyDef("−", KeyStyle.Operator, Key.Op('−'), "minus"),
    ),
    listOf(
        KeyDef("1", KeyStyle.Number, Key.Digit('1')),
        KeyDef("2", KeyStyle.Number, Key.Digit('2')),
        KeyDef("3", KeyStyle.Number, Key.Digit('3')),
        KeyDef("+", KeyStyle.Operator, Key.Op('+'), "plus"),
    ),
    listOf(
        KeyDef("±", KeyStyle.Function, Key.Sign, "Vorzeichen wechseln"),
        KeyDef("0", KeyStyle.Number, Key.Digit('0')),
        KeyDef(".", KeyStyle.Number, Key.Dot, "Komma"),
        KeyDef("=", KeyStyle.Accent, Key.Equals, "gleich"),
    ),
)

@Composable
private fun Keypad(onKey: (Key) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KEYS.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { def ->
                    KeyButton(def, onClick = { onKey(def.key) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KeyButton(def: KeyDef, onClick: () -> Unit, modifier: Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.9f else 1f,
        spring(dampingRatio = 0.45f, stiffness = 700f),
        label = "keyScale",
    )
    val haptic = LocalHapticFeedback.current

    val base = modifier
        .aspectRatio(1f)
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .semantics { contentDescription = def.desc; role = Role.Button }

    val styled = when (def.style) {
        KeyStyle.Accent -> base.clip(CircleShape).background(NColors.Red)
        KeyStyle.Operator -> base.clip(CircleShape)
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.2.dp, Color.White.copy(alpha = 0.55f), CircleShape)
        KeyStyle.Function -> base.glass(CircleShape, fill = 0.07f, stroke = 0.16f)
        KeyStyle.Number -> base.glass(CircleShape, fill = 0.04f, stroke = 0.12f)
    }

    val labelColor = when {
        def.style == KeyStyle.Accent -> NColors.White
        def.key == Key.Clear -> NColors.Red
        def.style == KeyStyle.Function -> NColors.White.copy(alpha = 0.75f)
        else -> NColors.White
    }

    Box(
        styled
            .background(if (pressed) Color.White.copy(alpha = 0.10f) else Color.Transparent)
            .clickable(interactionSource = interaction, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        DotText(def.label, pitch = 3.2.dp, color = labelColor)
    }
}

/* ---------- Buy Me a Coffee ---------- */

@Composable
private fun SupportDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val shape = RoundedCornerShape(32.dp)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .clip(shape)
                .background(Color(0xFF0A0A0A))
                .border(1.dp, Color.White.copy(alpha = 0.14f), shape)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DotIcon(DotIcons.HEART, pitch = 4.dp, colors = mapOf('#' to NColors.Red))
            Spacer(Modifier.height(22.dp))
            Text(
                "RECHNER IST OPEN SOURCE",
                color = NColors.White,
                fontFamily = Mono,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Kostenlos, werbefrei, ohne Tracking. Wenn dir die App gefällt, unterstütze die Weiterentwicklung.",
                color = NColors.Grey,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    runCatching { uriHandler.openUri(SUPPORT_URL) }
                    onDismiss()
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = NColors.Red, contentColor = NColors.White),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Support Development", fontFamily = Mono, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onDismiss) {
                Text("Später", color = NColors.Grey, fontFamily = Mono)
            }
        }
    }
}
