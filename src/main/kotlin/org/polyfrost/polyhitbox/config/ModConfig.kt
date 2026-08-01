package org.polyfrost.polyhitbox.config

import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.api.config.v1.Properties
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Property.Display
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.Visualizer
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier

object ModConfig : Config(
    "polyhitbox.json",
    "/assets/polyhitbox/polyhitbox.svg",
    "PolyHitbox",
    Config.Category.COMBAT,
) {
    var enabled = false

    var hideInF1 = true

    init {
        preload()
        migrateSplitOverride()
    }

    private fun migrateSplitOverride() {
        val saved = ConfigManager.active().load(id) ?: return
        var migrated = false
        for (category in HitboxCategory.entries) {
            if (category == HitboxCategory.DEFAULT) continue
            val key = category.name
            if (saved.getProp("${key}_overwriteLogic") != null || saved.getProp("${key}_overwriteVisuals") != null) continue
            val legacy = saved.getProp("${key}_overwriteDefault")?.get() as? Boolean ?: continue
            category.config.overwriteLogic = legacy
            category.config.overwriteVisuals = legacy
            migrated = true
        }
        if (migrated) save()
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

        val enableProp: Property<Boolean>? = if (isDefault) {
            switch("enabled", "Enabled", "Enable hitbox rendering.", { enabled }, { enabled = it }, tab, sub)
        } else {
            null
        }
        val logicProp: Property<Boolean>? = if (isDefault) {
            null
        } else {
            switch(
                "${key}_overwriteLogic", "Override General Logic",
                "Override the General show condition for ${category.displayName}.",
                { cfg().overwriteLogic }, { cfg().overwriteLogic = it }, tab, sub,
            )
        }
        val visualsProp: Property<Boolean>? = if (isDefault) {
            null
        } else {
            switch(
                "${key}_overwriteVisuals", "Override General Visuals",
                "Override the General styling for ${category.displayName}.",
                { cfg().overwriteVisuals }, { cfg().overwriteVisuals = it }, tab, sub,
            )
        }

        val hideInF1Prop: Property<Boolean>? = if (isDefault) {
            switch(
                "hideInF1", "Hide with HUD (F1)",
                "Hide hitboxes while the HUD is hidden with F1.",
                { hideInF1 }, { hideInF1 = it }, tab, sub,
            )
        } else {
            null
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
        val iframeColor = switch(
            "${key}_iframeColor", "Different Color in I-Frames",
            "Use separate colors while the entity is in its invulnerability frames (i-frames) from a " +
                "recent hit, so you can see when it can be damaged again. Takes priority over the hover colors.",
            { cfg().iframeColor }, { cfg().iframeColor = it }, tab, sub,
        )

        val showSide = checkbox("${key}_showSide", "Sides", "", { cfg().showSide }, { cfg().showSide = it }, tab, sub)
        val sideColor = color("${key}_sideColor", "Side Color", { cfg().sideColor }, { cfg().sideColor = it }, tab, sub)
        val sideHoverColor = color("${key}_sideHoverColor", "Hovered Side Color", { cfg().sideHoverColor }, { cfg().sideHoverColor = it }, tab, sub)
        val sideIframeColor = color("${key}_sideIframeColor", "I-Frame Side Color", { cfg().sideIframeColor }, { cfg().sideIframeColor = it }, tab, sub)

        val showOutline = checkbox("${key}_showOutline", "Outline", "", { cfg().showOutline }, { cfg().showOutline = it }, tab, sub)
        val outlineColor = color("${key}_outlineColor", "Outline Color", { cfg().outlineColor }, { cfg().outlineColor = it }, tab, sub)
        val outlineHoverColor = color("${key}_outlineHoverColor", "Hovered Outline Color", { cfg().outlineHoverColor }, { cfg().outlineHoverColor = it }, tab, sub)
        val outlineIframeColor = color("${key}_outlineIframeColor", "I-Frame Outline Color", { cfg().outlineIframeColor }, { cfg().outlineIframeColor = it }, tab, sub)
        val outlineThickness = sliderFloat("${key}_outlineThickness", "Outline Thickness", "", { cfg().outlineThickness }, { cfg().outlineThickness = it }, 1f, 5f, 0.5f, tab, sub)

        val showEyeHeight = checkbox("${key}_showEyeHeight", "Eye Height", "", { cfg().showEyeHeight }, { cfg().showEyeHeight = it }, tab, sub)
        val eyeHeightColor = color("${key}_eyeHeightColor", "Eye Height Color", { cfg().eyeHeightColor }, { cfg().eyeHeightColor = it }, tab, sub)
        val eyeHeightHoverColor = color("${key}_eyeHeightHoverColor", "Hovered Eye Height Color", { cfg().eyeHeightHoverColor }, { cfg().eyeHeightHoverColor = it }, tab, sub)
        val eyeHeightIframeColor = color("${key}_eyeHeightIframeColor", "I-Frame Eye Height Color", { cfg().eyeHeightIframeColor }, { cfg().eyeHeightIframeColor = it }, tab, sub)
        val eyeHeightThickness = sliderFloat("${key}_eyeHeightThickness", "Eye Height Thickness", "", { cfg().eyeHeightThickness }, { cfg().eyeHeightThickness = it }, 1f, 5f, 0.5f, tab, sub)

        val showViewRay = checkbox("${key}_showViewRay", "View Ray", "", { cfg().showViewRay }, { cfg().showViewRay = it }, tab, sub)
        val viewRayColor = color("${key}_viewRayColor", "View Ray Color", { cfg().viewRayColor }, { cfg().viewRayColor = it }, tab, sub)
        val viewRayHoverColor = color("${key}_viewRayHoverColor", "Hovered View Ray Color", { cfg().viewRayHoverColor }, { cfg().viewRayHoverColor = it }, tab, sub)
        val viewRayIframeColor = color("${key}_viewRayIframeColor", "I-Frame View Ray Color", { cfg().viewRayIframeColor }, { cfg().viewRayIframeColor = it }, tab, sub)
        val viewRayThickness = sliderFloat("${key}_viewRayThickness", "View Ray Thickness", "", { cfg().viewRayThickness }, { cfg().viewRayThickness = it }, 1f, 5f, 0.5f, tab, sub)

        enableProp?.let { tree.put(it) }
        hideInF1Prop?.let { tree.put(it) }
        logicProp?.let { tree.put(it) }
        tree.put(showCondition)
        visualsProp?.let { tree.put(it) }
        val visuals = listOf(
            lineStyle, dashFactor, hoverColor, iframeColor,
            showSide, sideColor, sideHoverColor, sideIframeColor,
            showOutline, outlineColor, outlineHoverColor, outlineIframeColor, outlineThickness,
            showEyeHeight, eyeHeightColor, eyeHeightHoverColor, eyeHeightIframeColor, eyeHeightThickness,
            showViewRay, viewRayColor, viewRayHoverColor, viewRayIframeColor, viewRayThickness,
        )
        visuals.forEach { tree.put(it) }

        if (enableProp != null) {
            hideInF1Prop?.addDisplayCondition(enableProp, true)
            showCondition.addDisplayCondition(enableProp, true)
            visuals.forEach { it.addDisplayCondition(enableProp, true) }
        }
        logicProp?.let { showCondition.addDisplayCondition(it, true) }
        visualsProp?.let { p -> visuals.forEach { it.addDisplayCondition(p, true) } }
        dashFactor.addDisplayCondition(Supplier { shown(cfg().lineStyle == 2) })
        lineStyle.addCallback(Predicate<Int> { dashFactor.revaluateDisplay(); false })

        sideColor.addDisplayCondition(showSide, true)
        outlineColor.addDisplayCondition(showOutline, true)
        outlineThickness.addDisplayCondition(showOutline, true)
        eyeHeightColor.addDisplayCondition(showEyeHeight, true)
        eyeHeightThickness.addDisplayCondition(showEyeHeight, true)
        viewRayColor.addDisplayCondition(showViewRay, true)
        viewRayThickness.addDisplayCondition(showViewRay, true)

        for ((toggle, pairs) in listOf(
            hoverColor to listOf(
                sideHoverColor to showSide,
                outlineHoverColor to showOutline,
                eyeHeightHoverColor to showEyeHeight,
                viewRayHoverColor to showViewRay,
            ),
            iframeColor to listOf(
                sideIframeColor to showSide,
                outlineIframeColor to showOutline,
                eyeHeightIframeColor to showEyeHeight,
                viewRayIframeColor to showViewRay,
            ),
        )) {
            for ((colorProp, showProp) in pairs) {
                colorProp.addDisplayCondition(showProp, true)
                colorProp.addDisplayCondition(toggle, true)
            }
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
