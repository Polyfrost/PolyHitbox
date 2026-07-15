package org.polyfrost.polyhitbox.config

import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.Properties
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Property.Display
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.Visualizer
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * PolyHitbox settings.
 *
 * Every [HitboxCategory] gets its own identically-shaped tab (the OneConfig "category"), each led by
 * an "Enabled" switch: on General it toggles the whole renderer, elsewhere it enables that category's
 * style overrides. Annotation-based config can't express this (a `@Switch`'s `category` is fixed per
 * class), so the tree is built programmatically in [makeTree].
 */
object ModConfig : Config(
    "polyhitbox.json",
    "/assets/polyhitbox/polyhitbox.svg",
    "PolyHitbox",
    Config.Category.COMBAT,
) {
    var enabled = false

    init {
        preload()
    }

    override fun makeTree(): Tree {
        val tree = Tree.tree(id)
        for (category in HitboxCategory.entries) {
            addCategory(tree, category)
        }
        return tree
    }

    private fun addCategory(tree: Tree, category: HitboxCategory) {
        // Property IDs are "<category>_<field>". The separator must not be a dot: the JSON serializer
        // treats dots in a property ID as nested-path separators, so the saved value never maps back
        // onto the flat property on load and every field silently resets to its default.
        val key = category.name
        // Each category is its own tab. A single default-named subcategory keeps the list flat (no
        // section header), so every tab lines up with the same layout.
        val tab = category.displayName
        val sub = "General"
        val cfg = { category.config }
        val isDefault = category == HitboxCategory.DEFAULT

        // The "Enabled" switch atop each tab: on General it is the master renderer toggle; on every
        // other tab it enables that category's style overrides (otherwise it falls back to General).
        val enableProp: Property<Boolean> = if (isDefault) {
            switch("enabled", "Enabled", "Enable hitbox rendering.", { enabled }, { enabled = it }, tab, sub)
        } else {
            switch(
                "${key}_overwriteDefault", "Override General",
                "Override the General styling for ${category.displayName}.",
                { cfg().overwriteDefault }, { cfg().overwriteDefault = it }, tab, sub,
            )
        }

        val showCondition = radio(
            "${key}_showCondition", "Show Condition",
            "When to draw this hitbox. \"Debug (F3+B)\" follows the vanilla hitbox toggle.",
            { cfg().showCondition }, { cfg().showCondition = it },
            arrayOf("Always", "Debug (F3+B)", "Hovered", "Never"), tab, sub,
        )
        val lineStyle = dropdown(
            "${key}_lineStyle", "Line Style", "",
            { cfg().lineStyle }, { cfg().lineStyle = it },
            arrayOf("Normal", "Proportioned", "Dashed"), tab, sub,
        )
        val dashFactor = sliderInt(
            "${key}_dashFactor", "Dash Factor", "",
            { cfg().dashFactor }, { cfg().dashFactor = it }, 1f, 20f, 1f, tab, sub,
        )
        val hoverColor = switch("${key}_hoverColor", "Different Color on Hover", "", { cfg().hoverColor }, { cfg().hoverColor = it }, tab, sub)

        val showSide = checkbox("${key}_showSide", "Sides", "", { cfg().showSide }, { cfg().showSide = it }, tab, sub)
        val sideColor = color("${key}_sideColor", "Side Color", { cfg().sideColor }, { cfg().sideColor = it }, tab, sub)
        val sideHoverColor = color("${key}_sideHoverColor", "Hovered Side Color", { cfg().sideHoverColor }, { cfg().sideHoverColor = it }, tab, sub)

        val showOutline = checkbox("${key}_showOutline", "Outline", "", { cfg().showOutline }, { cfg().showOutline = it }, tab, sub)
        val outlineColor = color("${key}_outlineColor", "Outline Color", { cfg().outlineColor }, { cfg().outlineColor = it }, tab, sub)
        val outlineHoverColor = color("${key}_outlineHoverColor", "Hovered Outline Color", { cfg().outlineHoverColor }, { cfg().outlineHoverColor = it }, tab, sub)
        val outlineThickness = sliderFloat("${key}_outlineThickness", "Outline Thickness", "", { cfg().outlineThickness }, { cfg().outlineThickness = it }, 1f, 5f, 0.5f, tab, sub)

        val showEyeHeight = checkbox("${key}_showEyeHeight", "Eye Height", "", { cfg().showEyeHeight }, { cfg().showEyeHeight = it }, tab, sub)
        val eyeHeightColor = color("${key}_eyeHeightColor", "Eye Height Color", { cfg().eyeHeightColor }, { cfg().eyeHeightColor = it }, tab, sub)
        val eyeHeightHoverColor = color("${key}_eyeHeightHoverColor", "Hovered Eye Height Color", { cfg().eyeHeightHoverColor }, { cfg().eyeHeightHoverColor = it }, tab, sub)
        val eyeHeightThickness = sliderFloat("${key}_eyeHeightThickness", "Eye Height Thickness", "", { cfg().eyeHeightThickness }, { cfg().eyeHeightThickness = it }, 1f, 5f, 0.5f, tab, sub)

        val showViewRay = checkbox("${key}_showViewRay", "View Ray", "", { cfg().showViewRay }, { cfg().showViewRay = it }, tab, sub)
        val viewRayColor = color("${key}_viewRayColor", "View Ray Color", { cfg().viewRayColor }, { cfg().viewRayColor = it }, tab, sub)
        val viewRayHoverColor = color("${key}_viewRayHoverColor", "Hovered View Ray Color", { cfg().viewRayHoverColor }, { cfg().viewRayHoverColor = it }, tab, sub)
        val viewRayThickness = sliderFloat("${key}_viewRayThickness", "View Ray Thickness", "", { cfg().viewRayThickness }, { cfg().viewRayThickness = it }, 1f, 5f, 0.5f, tab, sub)

        tree.put(enableProp)
        val body = listOf(
            showCondition, lineStyle, dashFactor, hoverColor,
            showSide, sideColor, sideHoverColor,
            showOutline, outlineColor, outlineHoverColor, outlineThickness,
            showEyeHeight, eyeHeightColor, eyeHeightHoverColor, eyeHeightThickness,
            showViewRay, viewRayColor, viewRayHoverColor, viewRayThickness,
        )
        body.forEach { tree.put(it) }

        // Dependencies (visibility). The Property-based overload registers a callback on the
        // dependency so the dependent re-evaluates when it changes; the Supplier-only overload is
        // evaluated once and never reacts, so it must be paired with an explicit revaluate trigger.
        // Everything below the tab's "Enabled" switch is hidden while it is off.
        body.forEach { it.addDisplayCondition(enableProp, true) }
        dashFactor.addDisplayCondition(Supplier { shown(cfg().lineStyle == 2) })
        // A callback returning true vetoes the change; return false so lineStyle still updates while
        // re-evaluating the dash-factor visibility as a side effect.
        lineStyle.addCallback(Predicate<Int> { dashFactor.revaluateDisplay(); false })

        sideColor.addDisplayCondition(showSide, true)
        outlineColor.addDisplayCondition(showOutline, true)
        outlineThickness.addDisplayCondition(showOutline, true)
        eyeHeightColor.addDisplayCondition(showEyeHeight, true)
        eyeHeightThickness.addDisplayCondition(showEyeHeight, true)
        viewRayColor.addDisplayCondition(showViewRay, true)
        viewRayThickness.addDisplayCondition(showViewRay, true)

        for ((hoverProp, showProp) in listOf(
            sideHoverColor to showSide,
            outlineHoverColor to showOutline,
            eyeHeightHoverColor to showEyeHeight,
            viewRayHoverColor to showViewRay,
        )) {
            hoverProp.addDisplayCondition(showProp, true)
            hoverProp.addDisplayCondition(hoverColor, true)
        }
    }

    private fun shown(condition: Boolean): Display = if (condition) Display.SHOWN else Display.HIDDEN

    private fun <T : Any> functional(
        id: String, title: String, desc: String,
        getter: () -> T, setter: (T) -> Unit, type: Class<T>,
        visualizer: Class<out Visualizer>, category: String, subcategory: String,
    ): Property<T> {
        val p = Properties.functional(Supplier { getter() }, Consumer { setter(it) }, id, title, desc, type)
        p.addMetadata("category", category)
        p.addMetadata("subcategory", subcategory)
        p.addMetadata("visualizer", visualizer)
        return p
    }

    private fun switch(id: String, title: String, desc: String, getter: () -> Boolean, setter: (Boolean) -> Unit, category: String, subcategory: String) =
        functional(id, title, desc, getter, setter, Boolean::class.javaObjectType, Visualizer.SwitchVisualizer::class.java, category, subcategory)

    private fun checkbox(id: String, title: String, desc: String, getter: () -> Boolean, setter: (Boolean) -> Unit, category: String, subcategory: String) =
        functional(id, title, desc, getter, setter, Boolean::class.javaObjectType, Visualizer.CheckboxVisualizer::class.java, category, subcategory)

    private fun dropdown(id: String, title: String, desc: String, getter: () -> Int, setter: (Int) -> Unit, options: Array<String>, category: String, subcategory: String): Property<Int> {
        val p = functional(id, title, desc, getter, setter, Int::class.javaObjectType, Visualizer.DropdownVisualizer::class.java, category, subcategory)
        p.addMetadata("options", options)
        return p
    }

    private fun radio(id: String, title: String, desc: String, getter: () -> Int, setter: (Int) -> Unit, options: Array<String>, category: String, subcategory: String): Property<Int> {
        val p = functional(id, title, desc, getter, setter, Int::class.javaObjectType, Visualizer.RadioVisualizer::class.java, category, subcategory)
        p.addMetadata("options", options)
        return p
    }

    private fun color(id: String, title: String, getter: () -> PolyColor, setter: (PolyColor) -> Unit, category: String, subcategory: String): Property<PolyColor> {
        val p = functional(id, title, "", getter, setter, PolyColor::class.java, Visualizer.ColorVisualizer::class.java, category, subcategory)
        p.addMetadata("alpha", true)
        return p
    }

    private fun sliderInt(id: String, title: String, desc: String, getter: () -> Int, setter: (Int) -> Unit, min: Float, max: Float, step: Float, category: String, subcategory: String): Property<Int> {
        val p = functional(id, title, desc, getter, setter, Int::class.javaObjectType, Visualizer.SliderVisualizer::class.java, category, subcategory)
        p.addMetadata("min", min); p.addMetadata("max", max); p.addMetadata("step", step)
        return p
    }

    private fun sliderFloat(id: String, title: String, desc: String, getter: () -> Float, setter: (Float) -> Unit, min: Float, max: Float, step: Float, category: String, subcategory: String): Property<Float> {
        val p = functional(id, title, desc, getter, setter, Float::class.javaObjectType, Visualizer.SliderVisualizer::class.java, category, subcategory)
        p.addMetadata("min", min); p.addMetadata("max", max); p.addMetadata("step", step)
        return p
    }
}
