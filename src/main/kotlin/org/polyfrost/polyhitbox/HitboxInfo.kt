package org.polyfrost.polyhitbox

import org.polyfrost.oneconfig.api.config.v1.Properties.ktProperty
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.Visualizer
import org.polyfrost.polyui.color.PolyColor
import org.polyfrost.polyui.color.argb
import org.polyfrost.polyui.color.asMutable

class HitboxInfo(private val id: String) {
    var showMode = 0
        private set
    val eyeline = ElementInfo(true, argb(0xFFFF0000.toInt()), "Eyeline")
    val viewRay = ElementInfo(true, argb(0xFF0000FF.toInt()), "View Ray")
    val outline = ElementInfo(true, argb(0xFFFFFFFF.toInt()), "Outline")
    val sides = ElementInfo(false, argb(0x14FFFFFF), "Sides")

    var isAccurate = false
        private set
    var useDistanceBasedWidth = true
        private set
    private var distanceFactor = 4.0F
    var dashFactor = 10
        private set

    private var differentColorOnHover = true

    var isTargeted = false
    var sqrDistance = 1.0F

    private var _tree: Tree? = null

    var tree: Tree
        get() = _tree ?: Tree.tree(id).apply {
            _tree = this
            put(ktProperty(::showMode, "Show Mode").apply {
                addMetadata("visualizer", Visualizer.RadioVisualizer::class.java)
                addMetadata("options", arrayOf("Always", "When Targeted", "Never"))
            })
            put(ktProperty(::isAccurate, "Accurate").apply {
                addMetadata(
                    "visualizer",
                    Visualizer.SwitchVisualizer::class.java
                )
            })
            put(ktProperty(::useDistanceBasedWidth, "Distance Based Width").apply {
                addMetadata(
                    "visualizer",
                    Visualizer.SwitchVisualizer::class.java
                )
            })
            put(ktProperty(::distanceFactor, "Distance Factor").apply {
                addMetadata(
                    "visualizer",
                    Visualizer.SliderVisualizer::class.java
                )
            })
            put(ktProperty(::dashFactor, "Dash Factor").apply {
                addMetadata(
                    "visualizer",
                    Visualizer.NumberVisualizer::class.java
                )
            })
            put(ktProperty(::differentColorOnHover, "Different Color on Hover").apply {
                addMetadata(
                    "visualizer",
                    Visualizer.SwitchVisualizer::class.java
                )
            })
            put(eyeline.tree)
            put(viewRay.tree)
            put(outline.tree)
            put(sides.tree)
        }
        set(value) {
            _tree = value
        }


    inner class ElementInfo(isShown: Boolean, initialColor: PolyColor, private val id: String) {
        var isShown = isShown
            private set
        var isDashed = false
            private set
        var width = 1.0F
            get() = if (useDistanceBasedWidth) field * (distanceFactor / sqrDistance) else field
            private set
        var colorNormal = initialColor
            private set
        var colorHovered: PolyColor = argb(initialColor.argb).asMutable().apply { alpha -= 0.2F }
            private set

        fun getColor() = if (isTargeted && differentColorOnHover) colorHovered else colorNormal

        private var _tree: Tree? = null

        val tree: Tree
            get() = _tree ?: Tree(id, id, null, null).apply {
                _tree = this
                put(ktProperty(::isShown, "Enabled").apply {
                    addMetadata(
                        "visualizer",
                        Visualizer.SwitchVisualizer::class.java
                    )
                })
                put(ktProperty(::isDashed, "Dashed").apply {
                    addMetadata(
                        "visualizer",
                        Visualizer.SwitchVisualizer::class.java
                    )
                })
                put(ktProperty(::width, "Width").apply {
                    addMetadata(
                        "visualizer",
                        Visualizer.SliderVisualizer::class.java
                    )
                })
                put(ktProperty(::colorNormal, "Color").apply {
                    addMetadata(
                        "visualizer",
                        Visualizer.ColorVisualizer::class.java
                    )
                })
                put(ktProperty(::colorHovered, "Hovered Color").apply {
                    addMetadata(
                        "visualizer",
                        Visualizer.ColorVisualizer::class.java
                    )
                })
            }
    }
}