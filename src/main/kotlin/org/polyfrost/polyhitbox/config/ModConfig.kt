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
 * The per-category options are identical in shape but must each live under their own subcategory,
 * which annotation-based config can't express (a `@Switch`'s `category` is fixed per class). The
 * tree is therefore built programmatically in [makeTree], one option group per [HitboxCategory].
 */
object ModConfig : Config(
    "polyhitbox.json",
    "/assets/polyhitbox/polyhitbox.svg",
    "PolyHitbox",
    Config.Category.COMBAT,
) {
    private const val GENERAL = "General"
    private const val CATEGORIES = "Categories"

    var enabled = true

    init {
        preload()
    }

    override fun makeTree(): Tree {
        val tree = Tree.tree(id)
        tree.put(
            switch("enabled", "Enabled", "Enable hitbox rendering.", { enabled }, { enabled = it }, GENERAL, GENERAL),
        )
        for (category in HitboxCategory.entries) {
            addCategory(tree, category)
        }
        return tree
    }

    private fun addCategory(tree: Tree, category: HitboxCategory) {
        val id = category.name
        val sub = category.displayName
        val cfg = { category.config }

        val enableProp: Property<Boolean>? = if (category != HitboxCategory.DEFAULT) {
            switch(
                "$id.overwriteDefault", "Enable",
                "Override the general styling for ${category.displayName}.",
                { cfg().overwriteDefault }, { cfg().overwriteDefault = it }, CATEGORIES, sub,
            )
        } else null

        val showCondition = dropdown(
            "$id.showCondition", "Show Condition",
            "When to draw this hitbox. \"Debug (F3+B)\" follows the vanilla hitbox toggle.",
            { cfg().showCondition }, { cfg().showCondition = it },
            arrayOf("Always", "Debug (F3+B)", "Hovered", "Never"), CATEGORIES, sub,
        )
        val lineStyle = dropdown(
            "$id.lineStyle", "Line Style", "",
            { cfg().lineStyle }, { cfg().lineStyle = it },
            arrayOf("Normal", "Proportioned", "Dashed"), CATEGORIES, sub,
        )
        val dashFactor = sliderInt(
            "$id.dashFactor", "Dash Factor", "",
            { cfg().dashFactor }, { cfg().dashFactor = it }, 1f, 20f, 1f, CATEGORIES, sub,
        )
        val hoverColor = switch("$id.hoverColor", "Different Color on Hover", "", { cfg().hoverColor }, { cfg().hoverColor = it }, CATEGORIES, sub)

        val showSide = checkbox("$id.showSide", "Sides", "", { cfg().showSide }, { cfg().showSide = it }, CATEGORIES, sub)
        val sideColor = color("$id.sideColor", "Side Color", { cfg().sideColor }, { cfg().sideColor = it }, CATEGORIES, sub)
        val sideHoverColor = color("$id.sideHoverColor", "Hovered Side Color", { cfg().sideHoverColor }, { cfg().sideHoverColor = it }, CATEGORIES, sub)

        val showOutline = checkbox("$id.showOutline", "Outline", "", { cfg().showOutline }, { cfg().showOutline = it }, CATEGORIES, sub)
        val outlineColor = color("$id.outlineColor", "Outline Color", { cfg().outlineColor }, { cfg().outlineColor = it }, CATEGORIES, sub)
        val outlineHoverColor = color("$id.outlineHoverColor", "Hovered Outline Color", { cfg().outlineHoverColor }, { cfg().outlineHoverColor = it }, CATEGORIES, sub)
        val outlineThickness = sliderFloat("$id.outlineThickness", "Outline Thickness", "", { cfg().outlineThickness }, { cfg().outlineThickness = it }, 1f, 5f, 0.5f, CATEGORIES, sub)

        val showEyeHeight = checkbox("$id.showEyeHeight", "Eye Height", "", { cfg().showEyeHeight }, { cfg().showEyeHeight = it }, CATEGORIES, sub)
        val eyeHeightColor = color("$id.eyeHeightColor", "Eye Height Color", { cfg().eyeHeightColor }, { cfg().eyeHeightColor = it }, CATEGORIES, sub)
        val eyeHeightHoverColor = color("$id.eyeHeightHoverColor", "Hovered Eye Height Color", { cfg().eyeHeightHoverColor }, { cfg().eyeHeightHoverColor = it }, CATEGORIES, sub)
        val eyeHeightThickness = sliderFloat("$id.eyeHeightThickness", "Eye Height Thickness", "", { cfg().eyeHeightThickness }, { cfg().eyeHeightThickness = it }, 1f, 5f, 0.5f, CATEGORIES, sub)

        val showViewRay = checkbox("$id.showViewRay", "View Ray", "", { cfg().showViewRay }, { cfg().showViewRay = it }, CATEGORIES, sub)
        val viewRayColor = color("$id.viewRayColor", "View Ray Color", { cfg().viewRayColor }, { cfg().viewRayColor = it }, CATEGORIES, sub)
        val viewRayHoverColor = color("$id.viewRayHoverColor", "Hovered View Ray Color", { cfg().viewRayHoverColor }, { cfg().viewRayHoverColor = it }, CATEGORIES, sub)
        val viewRayThickness = sliderFloat("$id.viewRayThickness", "View Ray Thickness", "", { cfg().viewRayThickness }, { cfg().viewRayThickness = it }, 1f, 5f, 0.5f, CATEGORIES, sub)

        enableProp?.let { tree.put(it) }
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
        if (enableProp != null) {
            body.forEach { it.addDisplayCondition(enableProp, true) }
        }
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
