package org.polyfrost.polyhitbox.config

import org.lwjgl.glfw.GLFW
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.api.config.v1.Properties
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Property.Display
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.Visualizer
import org.polyfrost.oneconfig.api.platform.v1.Platform
import org.polyfrost.oneconfig.api.ui.v1.keybind.KeybindHelper
import org.polyfrost.oneconfig.api.ui.v1.keybind.OneConfigKeybind
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier

object ModConfig : Config(
    "polyhitbox.json",
    "/assets/polyhitbox/polyhitbox.svg",
    "PolyHitbox",
    Config.Category.COMBAT,
) {
    private const val THICKNESS_MIN = 0.5f
    private const val THICKNESS_MAX = 3f
    private const val THICKNESS_STEP = 0.25f
    private const val THICKNESS_DESC = "Line width, as a multiple of vanilla's 2.5 pixel lines."

    var enabled = false

    var hideInF1 = true

    var toggled = false

    var retainToggle = true

    var toggleKeybind: OneConfigKeybind = defaultToggleKeybind()

    private fun defaultToggleKeybind(): OneConfigKeybind = KeybindHelper.builder()
        .key(GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_B)
        .action { pressed: Boolean ->
            // Rebinding through the settings UI swaps in a keybind that is not screen aware
            // so we check here instead of in the keybind itself
            if (pressed && Platform.screen().current<Any?>() == null) toggled = !toggled
            true
        }
        .build()

    private val toggleProps = ArrayList<Property<*>>()

    // Read before preload registers the tree because registering rewrites the file with only the
    // properties the tree still declares dropping any renamed or removed settings
    private var stored: Tree? = ConfigManager.active().load(id)

    init {
        preload()
        migrateSplitOverride()
        migrateLegacyStyle()
        if (!retainToggle) toggled = false
        stored = null
    }

    private fun migrateSplitOverride() {
        val saved = stored ?: return
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

    // Migrates line settings from 1.1.2 or earlier which are identified by the old lineStyle property
    private fun migrateLegacyStyle() {
        val saved = stored ?: return
        var migrated = false
        for (category in HitboxCategory.entries) {
            val key = category.name
            val legacy = (saved.getProp("${key}_lineStyle")?.get() as? Number)?.toInt() ?: continue
            val cfg = category.config
            // Old order was Normal Proportioned Dashed so Proportioned collapses onto Normal
            cfg.lineMode = if (legacy == 2) HitboxConfig.DASHED else HitboxConfig.NORMAL
            cfg.outlineThickness = legacyThickness(saved, "${key}_outlineThickness", cfg.outlineThickness)
            cfg.eyeHeightThickness = legacyThickness(saved, "${key}_eyeHeightThickness", cfg.eyeHeightThickness)
            cfg.viewRayThickness = legacyThickness(saved, "${key}_viewRayThickness", cfg.viewRayThickness)
            migrated = true
        }
        if (migrated) save()
    }

    // Old thickness was a pixel count that drew 1.4x wider than asked so convert and snap to a step
    private fun legacyThickness(saved: Tree, key: String, current: Float): Float {
        val legacy = (saved.getProp(key)?.get() as? Number)?.toFloat() ?: return current
        val multiplier = legacy * 1.4f / HitboxConfig.VANILLA_WIDTH
        return (Math.round(multiplier / THICKNESS_STEP) * THICKNESS_STEP).coerceIn(THICKNESS_MIN, THICKNESS_MAX)
    }

    override fun makeTree(): Tree {
        val tree = Tree.tree(id)
        toggleProps.clear()
        for (category in HitboxCategory.entries) {
            addCategory(tree, category)
        }
        return tree
    }

    private fun addCategory(tree: Tree, category: HitboxCategory) {
        // Separator must not be a dot because the JSON serializer treats it as a nested path
        // so the saved value never maps back onto the flat property and every field resets
        val key = category.name
        // One tab per category with a single subcategory so the list stays flat with no section header
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
            "When to draw this hitbox. \"Toggled\" is switched on and off with the toggle keybind.",
            { cfg().showCondition }, { cfg().showCondition = it },
            arrayOf("Always", "Toggled", "Hovered", "Never"), tab, sub,
        )
        val toggleKeybindProp: Property<OneConfigKeybind>? = if (isDefault) {
            keybind(
                "toggleKeybind", "Toggle Keybind",
                "Switches the hitboxes using the \"Toggled\" show condition on and off.",
                { toggleKeybind }, { toggleKeybind = it }, defaultToggleKeybind(), tab, sub,
            )
        } else {
            null
        }
        val retainToggleProp: Property<Boolean>? = if (isDefault) {
            switch(
                "retainToggle", "Retain Toggle State",
                "Remember whether the toggle is on across game restarts, instead of starting every session with it off.",
                { retainToggle }, { retainToggle = it }, tab, sub,
            )
        } else {
            null
        }
        // Stored so the toggle can survive a restart
        val toggledProp: Property<Boolean>? = if (isDefault) {
            switch("toggled", "Toggled", "", { toggled }, { toggled = it }, tab, sub)
                .also { it.addMetadata("hidden", true) }
        } else {
            null
        }
        val lineStyle = dropdown(
            "${key}_lineMode", "Line Style", "",
            { cfg().lineMode }, { cfg().lineMode = it },
            arrayOf("Normal", "Dashed"), tab, sub,
        )
        val dashFactor = sliderInt(
            "${key}_dashFactor", "Dash Factor", "",
            { cfg().dashFactor }, { cfg().dashFactor = it }, 1f, 20f, 1f, tab, sub,
        )
        val drawOverEntity = switch(
            "${key}_drawOverEntity", "Draw Over Entity",
            "Draw the hitbox on top of the entity it belongs to instead of letting the entity cover it. " +
                "Blocks and other entities in the way still cover it.",
            { cfg().drawOverEntity }, { cfg().drawOverEntity = it }, tab, sub,
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
        val outlineThickness = sliderFloat("${key}_outlineThickness", "Outline Thickness", THICKNESS_DESC, { cfg().outlineThickness }, { cfg().outlineThickness = it }, THICKNESS_MIN, THICKNESS_MAX, THICKNESS_STEP, tab, sub)

        val showEyeHeight = checkbox("${key}_showEyeHeight", "Eye Height", "", { cfg().showEyeHeight }, { cfg().showEyeHeight = it }, tab, sub)
        val eyeHeightColor = color("${key}_eyeHeightColor", "Eye Height Color", { cfg().eyeHeightColor }, { cfg().eyeHeightColor = it }, tab, sub)
        val eyeHeightHoverColor = color("${key}_eyeHeightHoverColor", "Hovered Eye Height Color", { cfg().eyeHeightHoverColor }, { cfg().eyeHeightHoverColor = it }, tab, sub)
        val eyeHeightIframeColor = color("${key}_eyeHeightIframeColor", "I-Frame Eye Height Color", { cfg().eyeHeightIframeColor }, { cfg().eyeHeightIframeColor = it }, tab, sub)
        val eyeHeightThickness = sliderFloat("${key}_eyeHeightThickness", "Eye Height Thickness", THICKNESS_DESC, { cfg().eyeHeightThickness }, { cfg().eyeHeightThickness = it }, THICKNESS_MIN, THICKNESS_MAX, THICKNESS_STEP, tab, sub)

        val showViewRay = checkbox("${key}_showViewRay", "View Ray", "", { cfg().showViewRay }, { cfg().showViewRay = it }, tab, sub)
        val viewRayColor = color("${key}_viewRayColor", "View Ray Color", { cfg().viewRayColor }, { cfg().viewRayColor = it }, tab, sub)
        val viewRayHoverColor = color("${key}_viewRayHoverColor", "Hovered View Ray Color", { cfg().viewRayHoverColor }, { cfg().viewRayHoverColor = it }, tab, sub)
        val viewRayIframeColor = color("${key}_viewRayIframeColor", "I-Frame View Ray Color", { cfg().viewRayIframeColor }, { cfg().viewRayIframeColor = it }, tab, sub)
        val viewRayThickness = sliderFloat("${key}_viewRayThickness", "View Ray Thickness", THICKNESS_DESC, { cfg().viewRayThickness }, { cfg().viewRayThickness = it }, THICKNESS_MIN, THICKNESS_MAX, THICKNESS_STEP, tab, sub)

        enableProp?.let { tree.put(it) }
        hideInF1Prop?.let { tree.put(it) }
        logicProp?.let { tree.put(it) }
        tree.put(showCondition)
        toggleKeybindProp?.let { tree.put(it) }
        retainToggleProp?.let { tree.put(it) }
        toggledProp?.let { tree.put(it) }
        visualsProp?.let { tree.put(it) }
        val visuals = listOf(
            lineStyle, dashFactor, drawOverEntity, hoverColor, iframeColor,
            showSide, sideColor, sideHoverColor, sideIframeColor,
            showOutline, outlineColor, outlineHoverColor, outlineIframeColor, outlineThickness,
            showEyeHeight, eyeHeightColor, eyeHeightHoverColor, eyeHeightIframeColor, eyeHeightThickness,
            showViewRay, viewRayColor, viewRayHoverColor, viewRayIframeColor, viewRayThickness,
        )
        visuals.forEach { tree.put(it) }

        val toggles = listOfNotNull(toggleKeybindProp, retainToggleProp)
        toggleProps.addAll(toggles)
        if (enableProp != null) {
            hideInF1Prop?.addDisplayCondition(enableProp, true)
            showCondition.addDisplayCondition(enableProp, true)
            toggles.forEach { it.addDisplayCondition(enableProp, true) }
            visuals.forEach { it.addDisplayCondition(enableProp, true) }
        }
        logicProp?.let { showCondition.addDisplayCondition(it, true) }
        visualsProp?.let { p -> visuals.forEach { it.addDisplayCondition(p, true) } }
        toggles.forEach { p -> p.addDisplayCondition(Supplier { shown(HitboxCategory.anyToggled()) }) }
        showCondition.addCallback(Predicate<Int> { revaluateToggleProps(); false })
        logicProp?.addCallback(Predicate<Boolean> { revaluateToggleProps(); false })
        dashFactor.addDisplayCondition(Supplier { shown(cfg().lineMode == HitboxConfig.DASHED) })
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

    private fun revaluateToggleProps() {
        for (prop in toggleProps) prop.revaluateDisplay()
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

    private fun keybind(
        id: String, title: String, desc: String,
        getter: () -> OneConfigKeybind, setter: (OneConfigKeybind) -> Unit,
        default: OneConfigKeybind, category: String, subcategory: String,
    ): Property<OneConfigKeybind> {
        val p = functional(id, title, desc, getter, setter, OneConfigKeybind::class.java, Visualizer.KeybindVisualizer::class.java, category, subcategory)
        p.addMetadata("default", default)
        // Keeps the bind out of Minecraft's Controls menu, which cannot represent a combo like F3+B
        p.addMetadata("oc_no_mc_mirror", true)
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
