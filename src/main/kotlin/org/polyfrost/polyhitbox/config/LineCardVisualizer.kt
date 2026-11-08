@file:OptIn(ExperimentalOneConfigUiApi::class)

package org.polyfrost.polyhitbox.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Visualizer
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.components.Text
import org.polyfrost.oneconfig.api.ui.v1.components.Tooltip
import org.polyfrost.oneconfig.api.ui.v1.theme.OneConfigTheme
import org.polyfrost.oneconfig.internal.ui.components.settings.CheckboxIndicator
import java.util.function.Predicate

class LineCard(
    val color: Property<PolyColor>,
    val hoverColor: Property<PolyColor>,
    val iframeColor: Property<PolyColor>,
    val thickness: Property<Float>?,
    val hoverToggle: Property<Boolean>,
    val iframeToggle: Property<Boolean>,
) {
    companion object {
        const val KEY = "polyhitbox_lineCard"
    }
}

class LineCardVisualizer : Visualizer {
    @Composable
    @Suppress("UNCHECKED_CAST")
    override fun visualize(prop: Property<*>) {
        val card = prop.getMetadata<LineCard>(LineCard.KEY) ?: return
        val toggle = prop as Property<Boolean>
        val shown = observe(toggle) == true
        val hover = observe(card.hoverToggle) == true
        val iframe = observe(card.iframeToggle) == true
        val contentAlpha = if (shown) 1f else 0.4f

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GROUP_GAP)) {
            Row(Modifier.alpha(contentAlpha), horizontalArrangement = Arrangement.spacedBy(SWATCH_GAP)) {
                Swatch("Color", card.color)
                if (hover) Swatch("Hovered color", card.hoverColor)
                if (iframe) Swatch("I-Frame color", card.iframeColor)
            }
            Box(Modifier.width(SPINNER_WIDTH).alpha(contentAlpha), contentAlignment = Alignment.CenterEnd) {
                val thickness = card.thickness
                if (thickness != null) {
                    Visualizer[Visualizer.NumberVisualizer::class.java]?.visualize(thickness)
                } else {
                    Spacer(Modifier.width(SPINNER_WIDTH))
                }
            }
            CheckboxIndicator(shown, onToggle = { toggle.set(!shown) })
        }
    }

    @Composable
    private fun Swatch(label: String, prop: Property<PolyColor>) {
        Tooltip(
            text = { Text(label, Modifier, OneConfigTheme.current.textColor, 12.sp) },
            modifier = Modifier,
            anchor = Alignment.TopCenter,
        ) {
            Visualizer[Visualizer.ColorVisualizer::class.java]?.visualize(prop)
        }
    }

    @Composable
    private fun <T> observe(prop: Property<T>): T? {
        var value by remember(prop) { mutableStateOf(prop.get()) }
        DisposableEffect(prop) {
            val listener = Predicate<T> { value = it; false }
            prop.addCallback(listener)
            onDispose { prop.removeCallback(listener) }
        }
        return value
    }

    companion object {
        private val SPINNER_WIDTH = 96.dp
        private val SWATCH_GAP = 6.dp
        private val GROUP_GAP = 14.dp

        private val CONTROL_SIZE = 24.dp

        val MAX_WIDTH = CONTROL_SIZE * 3 + SWATCH_GAP * 2 + GROUP_GAP + SPINNER_WIDTH + GROUP_GAP + CONTROL_SIZE
    }
}
