package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.annotations.Button
import cc.polyfrost.oneconfig.config.annotations.Color
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.OneColor

open class HitboxConfiguration {
    @Switch(name = "Show Hitbox", subcategory = "General Options")
    var showHitbox = false

    @Button(name = "Reset", text = "Reset", subcategory = "General Options")
    fun reset() {
        accurate = false
        dashedHitbox = false
        showOutline = true
        showEyeHeight = true
        showLookVector = true
        outlineThickness = 2f
        eyeHeightThickness = 2f
        lookVectorThickness = 2f
        outlineColor = OneColor(-1)
        eyeHeightColor = OneColor(-0x10000)
        lookVectorColor = OneColor(-0xffff01)
    }

    @Switch(name = "Accurate Hitboxes", subcategory = "General Options")
    var accurate = false

    @Switch(name = "Dashed", subcategory = "General Options")
    var dashedHitbox = false

    @Switch(name = "Hitbox Outline", subcategory = "General Options")
    var showOutline = true

    @Switch(name = "Eye Height", subcategory = "General Options")
    var showEyeHeight = true

    @Switch(name = "Look Vector", subcategory = "General Options")
    var showLookVector = true

    @Slider(name = "Outline Thickness", subcategory = "General Options", min = 1f, max = 5f)
    var outlineThickness = 2f

    @Slider(name = "Eye Height Thickness", subcategory = "General Options", min = 1f, max = 5f)
    var eyeHeightThickness = 2f

    @Slider(name = "Look Vector Thickness", subcategory = "General Options", min = 1f, max = 5f)
    var lookVectorThickness = 2f

    @Color(name = "Hitbox Outline Color", subcategory = "Color Options", size = 2)
    var outlineColor = OneColor(-1)

    @Color(name = "Eye Height Color", subcategory = "Color Options", size = 2)
    var eyeHeightColor = OneColor(-0x10000)

    @Color(name = "Look Vector Color", subcategory = "Color Options", size = 2)
    var lookVectorColor = OneColor(-0xffff01)

}
