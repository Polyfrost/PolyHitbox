package org.polyfrost.polyhitboxes.config.data

import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.core.OneColor
import org.polyfrost.polyhitboxes.config.gui.SpacerWidget
import org.polyfrost.polyhitboxes.config.gui.OptionSetup
import org.polyfrost.polyhitboxes.config.gui.SetupOptions
import org.polyfrost.polyhitboxes.render.HitboxPreview

class HitboxConfig : SetupOptions {
    @Transient
    var category: HitboxCategory? = null

    @Switch(name = "Overwrite Default")
    var overwriteDefault = false

    @Dropdown(name = "Show Condition", options = ["Always", "Toggled", "Hovered", "Never"], size = 2)
    var showCondition = 1

    @Switch(name = "Accurate Hitbox")
    var accurate = false

    @Switch(name = "Dashed Lines")
    var dashedLines = false

    @Slider(name = "Dash Factor", min = 1f, max = 20f, step = 1)
    var dashFactor = 10

    @Checkbox(name = "Sides")
    var showSide = false

    @Color(name = "Side Color")
    var sideColor = OneColor(255, 255, 255, 63)

    @Checkbox(name = "Outline")
    var showOutline = true

    @Color(name = "Outline Color")
    var outlineColor = OneColor(255, 255, 255, 255)

    @Slider(name = "Outline Thickness", min = 1f, max = 5f, step = 1)
    var outlineThickness = 2

    @Checkbox(name = "Eye Height")
    var showEyeHeight = true

    @Color(name = "Eye Height Color")
    var eyeHeightColor = OneColor(255, 0, 0, 255)

    @Slider(name = "Eye Height Thickness", min = 1f, max = 5f, step = 1)
    var eyeHeightThickness = 2

    @Checkbox(name = "View Ray")
    var showViewRay = true

    @Color(name = "View Ray Color")
    var viewRayColor = OneColor(0, 0, 255, 255)

    @Slider(name = "View Ray Thickness", min = 1f, max = 5f, step = 1)
    var viewRayThickness = 2

    override fun OptionSetup.setup() {
        for (option in optionNames) {
            if (option == ::overwriteDefault.name) continue
            if (category != HitboxCategory.DEFAULT) {
                option dependOn ::overwriteDefault.name
            } else {
                options[0] = SpacerWidget
            }
            if (option == ::showCondition.name) continue
            option.dependOn(::showCondition.name) { showCondition != 3 }
        }
        ::dashFactor.name dependOn ::dashedLines.name
        ::sideColor.name dependOn ::showSide.name
        ::outlineColor.name dependOn ::showOutline.name
        ::outlineThickness.name dependOn ::showOutline.name
        ::eyeHeightColor.name dependOn ::showEyeHeight.name
        ::eyeHeightThickness.name dependOn ::showEyeHeight.name
        ::viewRayColor.name dependOn ::showViewRay.name
        ::viewRayThickness.name dependOn ::showViewRay.name

        options.add(0, SpacerWidget)
        val category = category ?: return
        options.add(HitboxPreview(category))
    }
}
