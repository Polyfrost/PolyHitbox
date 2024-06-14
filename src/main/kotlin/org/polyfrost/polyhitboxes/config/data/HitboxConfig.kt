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

    @Switch(name = "Enable")
    var overwriteDefault = false

    @Dropdown(name = "Show Condition", options = ["Always", "Toggled", "Hovered", "Never"], size = 2)
    var showCondition = 1

    @Switch(name = "Accurate Hitbox")
    var accurate = false

    @Switch(name = "Proportioned Lines")
    var proportionedLines = false

    @Checkbox(name = "Sides")
    var showSide = false

    @Color(name = "Side Color")
    var sideColor = OneColor(255, 255, 255, 63)

    @Checkbox(name = "Outline")
    var showOutline = true

    @Color(name = "Outline Color")
    var outlineColor = OneColor(255, 255, 255, 255)

    @Slider(name = "Outline Thickness", min = 1f, max = 5f)
    var outlineThickness = 2f

    @Checkbox(name = "Eye Height")
    var showEyeHeight = true

    @Color(name = "Eye Height Color")
    var eyeHeightColor = OneColor(255, 0, 0, 255)

    @Slider(name = "Eye Height Thickness", min = 1f, max = 5f)
    var eyeHeightThickness = 2f

    @Checkbox(name = "View Ray")
    var showViewRay = true

    @Color(name = "View Ray Color")
    var viewRayColor = OneColor(0, 0, 255, 255)

    @Slider(name = "View Ray Thickness", min = 1f, max = 5f)
    var viewRayThickness = 2f

    override fun OptionSetup.setup() {
        for (option in optionNames) {
            if (option == ::overwriteDefault.name) continue
            if (category == HitboxCategory.DEFAULT) {
                options[0] = SpacerWidget
            } else {
                option dependOn ::overwriteDefault.name
            }
            if (option == ::showCondition.name) continue
            option.dependOn(::showCondition.name) { showCondition != 3 }
        }
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
