package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.core.OneColor

class HitboxConfig {
    @Switch(name = "Overwrite Default")
    var overwriteDefault = false

    @Dropdown(name = "Show Condition", options = ["Always", "Toggled", "Hovered", "Never"], size = 2)
    var showCondition = 1

    @Switch(name = "Accurate Hitbox")
    var accurate = false

    @Switch(name = "Dashed Lines")
    var dashedLines = false

    @Checkbox(name = "Hitbox Outline")
    var showOutline = true

    @Color(name = "Outline Color")
    var outlineColor = OneColor(255, 255, 255, 255)

    @Slider(name = "Outline Thickness", min = 1f, max = 5f)
    var outlineThickness = 2

    @Checkbox(name = "Eye Height")
    var showEyeHeight = true

    @Color(name = "Eye Height Color")
    var eyeHeightColor = OneColor(255, 0, 0, 255)

    @Slider(name = "Eye Height Thickness", min = 1f, max = 5f)
    var eyeHeightThickness = 2

    @Checkbox(name = "View Ray")
    var showViewRay = true

    @Color(name = "View Ray Color")
    var viewRayColor = OneColor(0, 0, 255, 255)

    @Slider(name = "View Ray Thickness", min = 1f, max = 5f)
    var viewRayThickness = 2

}
