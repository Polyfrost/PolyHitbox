package org.polyfrost.polyhitboxes.config.tree

import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.annotations.Color
import cc.polyfrost.oneconfig.config.annotations.Checkbox
import cc.polyfrost.oneconfig.config.annotations.Number
import cc.polyfrost.oneconfig.config.core.OneColor

open class HitboxProfile {
    @Switch(name = "Inherit", size = 1)
    var inherit = true

    @Switch(name = "Show Hitbox", size = 1)
    var showHitbox = false

    @Switch(name = "Accurate Hitboxes")
    var accurate = false

    @Switch(name = "Dashed Lines")
    var dashedLines = false

    @Checkbox(name = "Hitbox Outline")
    var showOutline = true

    @Color(name = "Hitbox Outline Color")
    var outlineColor = OneColor(255, 255, 255, 255)

    @Number(name = "Outline Thickness", min = 1f, max = 5f)
    var outlineThickness = 2f

    @Checkbox(name = "Eye Height")
    var showEyeHeight = true

    @Color(name = "Eye Height Color")
    var eyeHeightColor = OneColor(255, 0, 0, 255)

    @Number(name = "Eye Height Thickness", min = 1f, max = 5f)
    var eyeHeightThickness = 2f

    @Checkbox(name = "Look Vector")
    var showLookVector = true

    @Color(name = "Look Vector Color")
    var lookVectorColor = OneColor(0, 0, 255, 255)

    @Number(name = "Look Vector Thickness", min = 1f, max = 5f)
    var lookVectorThickness = 2f
}
