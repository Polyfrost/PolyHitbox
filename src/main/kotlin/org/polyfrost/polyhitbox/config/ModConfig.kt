package org.polyfrost.polyhitbox.config

import com.mojang.blaze3d.platform.InputConstants
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Config
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

    var thinWithDistance = true

    var accurateHitboxes = true

    var toggled = false

    var retainToggle = true

    var toggleKeybind: OneConfigKeybind = defaultToggleKeybind()

    private fun defaultToggleKeybind(): OneConfigKeybind = KeybindHelper.builder()
        .key(InputConstants.KEY_F3, InputConstants.KEY_B)
        .action { pressed: Boolean ->
            // Rebinding through the settings UI swaps in a keybind that is not screen aware
            // so we check here instead of in the keybind itself
            if (pressed && Platform.screen().current<Any?>() == null) toggled = !toggled
            true
        }
        .build()

    private val toggleProps = ArrayList<Property<*>>()

    init {
        preload()
        if (!retainToggle) toggled = false
    }

    override fun onInitialized(tree: Tree) {
        val legacy = tree.map.keys.filter { key -> tree.get(key) is Property<*> && HitboxCategory.entries.any { key.startsWith("${it.name}_") } }
        if (legacy.isEmpty()) return
        migrateFlatCategories(tree)
        migrateLegacyStyle(tree)
        migrateSplitOverride(tree)
        legacy.forEach { tree.remove(it) }
        save()
        revaluateDisplays(tree)
    }

    // Saves from 1.3.9 or earlier keep every category's settings at the root instead of in its accordion
    private fun migrateFlatCategories(root: Tree) {
        for (category in HitboxCategory.entries) {
            root.getChild(category.name)?.onAllProps { id, prop ->
                root.getProp(id)?.let { prop.overwrite(it, false) }
            }
        }
    }

    private fun migrateSplitOverride(root: Tree) {
        val general = HitboxCategory.DEFAULT.config
        for (category in HitboxCategory.entries) {
            if (category == HitboxCategory.DEFAULT) continue
            val key = category.name
            val legacy = root.getProp("${key}_overwriteDefault")?.get() as? Boolean
            val logic = root.getProp("${key}_overwriteLogic")?.get() as? Boolean ?: legacy
            val visuals = root.getProp("${key}_overwriteVisuals")?.get() as? Boolean ?: legacy
            if (logic == null && visuals == null) continue
            val cfg = category.config
            cfg.override = logic == true || visuals == true
            if (logic == true && visuals != true) cfg.copyVisualsFrom(general)
            if (visuals == true && logic != true) cfg.showCondition = general.showCondition
        }
    }

    // Migrates line settings from 1.1.2 or earlier which are identified by the old lineStyle property
    private fun migrateLegacyStyle(root: Tree) {
        for (category in HitboxCategory.entries) {
            val key = category.name
            val legacy = (root.getProp("${key}_lineStyle")?.get() as? Number)?.toInt() ?: continue
            val cfg = category.config
            // Old order was Normal Proportioned Dashed so Proportioned collapses onto Normal
            cfg.lineMode = if (legacy == 2) HitboxConfig.DASHED else HitboxConfig.NORMAL
            cfg.outlineThickness = legacyThickness(root, "${key}_outlineThickness", cfg.outlineThickness)
            cfg.eyeHeightThickness = legacyThickness(root, "${key}_eyeHeightThickness", cfg.eyeHeightThickness)
            cfg.viewRayThickness = legacyThickness(root, "${key}_viewRayThickness", cfg.viewRayThickness)
        }
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

    private fun addCategory(root: Tree, category: HitboxCategory) {
        // Separator must not be a dot because the JSON serializer treats it as a nested path
        // so the saved value never maps back onto the property and every field resets
        val key = category.name
        val tab = "General"
        val cfg = { category.config }
        val isDefault = category == HitboxCategory.DEFAULT
        val globalSub = "General"
        val sub = "Hitboxes"
        val tree = Tree(key, category.displayName, null, null).also {
            it.addMetadata("category", tab)
            it.addMetadata("subcategory", sub)
        }

        val enableProp: Property<Boolean>? = if (isDefault) {
            switch("enabled", "Enabled", "Enable hitbox rendering.", { enabled }, { enabled = it }, tab, globalSub)
        } else {
            null
        }
        val overrideProp: Property<Boolean>? = if (isDefault) {
            null
        } else {
            Properties.functional(
                Supplier { cfg().override }, Consumer { cfg().override = it },
                "${key}_override", "Override General",
                "Use these settings for ${category.displayName} instead of the General ones.",
                Boolean::class.javaPrimitiveType!!,
            ).also {
                it.addMetadata("category", tab)
                it.addMetadata("subcategory", sub)
            }
        }

        val hideInF1Prop: Property<Boolean>? = if (isDefault) {
            switch(
                "hideInF1", "Hide with HUD (F1)",
                "Hide hitboxes while the HUD is hidden with F1.",
                { hideInF1 }, { hideInF1 = it }, tab, globalSub,
            )
        } else {
            null
        }

        val thinWithDistanceProp: Property<Boolean>? = if (isDefault) {
            switch(
                "thinWithDistance", "Thin Lines with Distance",
                "Keep the hitbox width consistent across distances.",
                { thinWithDistance }, { thinWithDistance = it }, tab, globalSub,
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
        val showGroundedProp: Property<Boolean>? = if (category == HitboxCategory.ARROW) {
            switch(
                "${key}_showGrounded", "Show Grounded Arrows",
                "Draw hitboxes for arrows stuck in blocks.",
                { cfg().showGrounded }, { cfg().showGrounded = it }, tab, sub,
            )
        } else {
            null
        }
        val toggleKeybindProp: Property<OneConfigKeybind>? = if (isDefault) {
            keybind(
                "toggleKeybind", "Toggle Keybind",
                "Switches the hitboxes using the \"Toggled\" show condition on and off.",
                { toggleKeybind }, { toggleKeybind = it }, defaultToggleKeybind(), tab, globalSub,
            )
        } else {
            null
        }
        val retainToggleProp: Property<Boolean>? = if (isDefault) {
            switch(
                "retainToggle", "Retain Toggle State",
                "Remember whether the toggle is on across game restarts, instead of starting every session with it off.",
                { retainToggle }, { retainToggle = it }, tab, globalSub,
            )
        } else {
            null
        }
        // Stored so the toggle can survive a restart
        val toggledProp: Property<Boolean>? = if (isDefault) {
            switch("toggled", "Toggled", "", { toggled }, { toggled = it }, tab, globalSub)
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

        val sideColor = color("${key}_sideColor", "Side Color", { cfg().sideColor }, { cfg().sideColor = it }, tab, sub)
        val sideHoverColor = color("${key}_sideHoverColor", "Hovered Side Color", { cfg().sideHoverColor }, { cfg().sideHoverColor = it }, tab, sub)
        val sideIframeColor = color("${key}_sideIframeColor", "I-Frame Side Color", { cfg().sideIframeColor }, { cfg().sideIframeColor = it }, tab, sub)

        val outlineColor = color("${key}_outlineColor", "Outline Color", { cfg().outlineColor }, { cfg().outlineColor = it }, tab, sub)
        val outlineHoverColor = color("${key}_outlineHoverColor", "Hovered Outline Color", { cfg().outlineHoverColor }, { cfg().outlineHoverColor = it }, tab, sub)
        val outlineIframeColor = color("${key}_outlineIframeColor", "I-Frame Outline Color", { cfg().outlineIframeColor }, { cfg().outlineIframeColor = it }, tab, sub)
        val outlineThickness = sliderFloat("${key}_outlineThickness", "Outline Thickness", THICKNESS_DESC, { cfg().outlineThickness }, { cfg().outlineThickness = it }, THICKNESS_MIN, THICKNESS_MAX, THICKNESS_STEP, tab, sub)

        val eyeHeightColor = color("${key}_eyeHeightColor", "Eye Height Color", { cfg().eyeHeightColor }, { cfg().eyeHeightColor = it }, tab, sub)
        val eyeHeightHoverColor = color("${key}_eyeHeightHoverColor", "Hovered Eye Height Color", { cfg().eyeHeightHoverColor }, { cfg().eyeHeightHoverColor = it }, tab, sub)
        val eyeHeightIframeColor = color("${key}_eyeHeightIframeColor", "I-Frame Eye Height Color", { cfg().eyeHeightIframeColor }, { cfg().eyeHeightIframeColor = it }, tab, sub)
        val eyeHeightThickness = sliderFloat("${key}_eyeHeightThickness", "Eye Height Thickness", THICKNESS_DESC, { cfg().eyeHeightThickness }, { cfg().eyeHeightThickness = it }, THICKNESS_MIN, THICKNESS_MAX, THICKNESS_STEP, tab, sub)

        val viewRayColor = color("${key}_viewRayColor", "View Ray Color", { cfg().viewRayColor }, { cfg().viewRayColor = it }, tab, sub)
        val viewRayHoverColor = color("${key}_viewRayHoverColor", "Hovered View Ray Color", { cfg().viewRayHoverColor }, { cfg().viewRayHoverColor = it }, tab, sub)
        val viewRayIframeColor = color("${key}_viewRayIframeColor", "I-Frame View Ray Color", { cfg().viewRayIframeColor }, { cfg().viewRayIframeColor = it }, tab, sub)
        val viewRayThickness = sliderFloat("${key}_viewRayThickness", "View Ray Thickness", THICKNESS_DESC, { cfg().viewRayThickness }, { cfg().viewRayThickness = it }, THICKNESS_MIN, THICKNESS_MAX, THICKNESS_STEP, tab, sub)

        val showOutline = lineCard(
            "${key}_showOutline", "Outline", "The box around the hitbox.", { cfg().showOutline }, { cfg().showOutline = it },
            LineCard(outlineColor, outlineHoverColor, outlineIframeColor, outlineThickness, hoverColor, iframeColor), tab, sub,
        )
        val showEyeHeight = lineCard(
            "${key}_showEyeHeight", "Eye Height", "A line at the height the entity sees from.", { cfg().showEyeHeight }, { cfg().showEyeHeight = it },
            LineCard(eyeHeightColor, eyeHeightHoverColor, eyeHeightIframeColor, eyeHeightThickness, hoverColor, iframeColor), tab, sub,
        )
        val showViewRay = lineCard(
            "${key}_showViewRay", "View Ray", "A line in the direction the entity is looking.", { cfg().showViewRay }, { cfg().showViewRay = it },
            LineCard(viewRayColor, viewRayHoverColor, viewRayIframeColor, viewRayThickness, hoverColor, iframeColor), tab, sub,
        )
        val showSide = lineCard(
            "${key}_showSide", "Sides", "Fill the faces of the hitbox.", { cfg().showSide }, { cfg().showSide = it },
            LineCard(sideColor, sideHoverColor, sideIframeColor, null, hoverColor, iframeColor), tab, sub,
        )
        val cardParts = listOf(
            outlineColor, outlineHoverColor, outlineIframeColor, outlineThickness,
            eyeHeightColor, eyeHeightHoverColor, eyeHeightIframeColor, eyeHeightThickness,
            viewRayColor, viewRayHoverColor, viewRayIframeColor, viewRayThickness,
            sideColor, sideHoverColor, sideIframeColor,
        )
        cardParts.forEach { it.addMetadata("hidden", true) }


        enableProp?.let { root.put(it) }
        hideInF1Prop?.let { root.put(it) }
        thinWithDistanceProp?.let { root.put(it) }
        //? if = 1.8.9 {
        /*if (enableProp != null) {
            val accurateHitboxesProp = switch(
                "accurateHitboxes", "Accurate Hitboxes",
                "Draw the hitbox at the size attacks actually register against. ",
                { accurateHitboxes }, { accurateHitboxes = it }, tab, globalSub,
            )
            root.put(accurateHitboxesProp)
            accurateHitboxesProp.addDisplayCondition(enableProp, true)
        }
        *///?}
        toggleKeybindProp?.let { root.put(it) }
        retainToggleProp?.let { root.put(it) }
        toggledProp?.let { root.put(it) }
        root.put(tree)
        overrideProp?.let { tree.put(it) }
        tree.put(showCondition)
        val visuals = listOf(
            lineStyle, dashFactor, hoverColor, iframeColor,
            showOutline, showEyeHeight, showViewRay, showSide,
            drawOverEntity,
        )
        visuals.forEach { tree.put(it) }
        showGroundedProp?.let { tree.put(it) }
        cardParts.forEach { tree.put(it) }

        val toggles = listOfNotNull(toggleKeybindProp, retainToggleProp)
        toggleProps.addAll(toggles)
        if (enableProp != null) {
            hideInF1Prop?.addDisplayCondition(enableProp, true)
            thinWithDistanceProp?.addDisplayCondition(enableProp, true)
            showCondition.addDisplayCondition(enableProp, true)
            toggles.forEach { it.addDisplayCondition(enableProp, true) }
            visuals.forEach { it.addDisplayCondition(enableProp, true) }
        }
        overrideProp?.let { p ->
            showCondition.addDisplayCondition(p, false)
            visuals.forEach { it.addDisplayCondition(p, false) }
        }
        toggles.forEach { p -> p.addDisplayCondition(Supplier { shown(HitboxCategory.anyToggled()) }) }
        showCondition.addCallback(Predicate<Int> { revaluateToggleProps(); false })
        overrideProp?.addCallback(Predicate<Boolean> { revaluateToggleProps(); false })
        dashFactor.addDisplayCondition(Supplier { shown(cfg().lineMode == HitboxConfig.DASHED) })
        lineStyle.addCallback(Predicate<Int> { dashFactor.revaluateDisplay(); false })
    }

    private fun revaluateDisplays(tree: Tree) {
        tree.onAll { _, node ->
            when (node) {
                is Tree -> revaluateDisplays(node)
                is Property<*> -> node.revaluateDisplay()
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

    private fun lineCard(
        id: String, title: String, desc: String,
        getter: () -> Boolean, setter: (Boolean) -> Unit, card: LineCard, category: String, subcategory: String,
    ): Property<Boolean> {
        val p = functional(id, title, desc, getter, setter, Boolean::class.javaObjectType, LineCardVisualizer::class.java, category, subcategory)
        p.addMetadata(LineCard.KEY, card)
        p.addMetadata("resetWith", listOfNotNull(card.color, card.hoverColor, card.iframeColor, card.thickness))
        p.addMetadata("controlWidth", LineCardVisualizer.MAX_WIDTH.value)
        return p
    }

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
        // Every color sits in a line row, which only has room for a small square
        p.addMetadata("swatch", true)
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
