package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.annotations.Button
import cc.polyfrost.oneconfig.config.annotations.Color
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.OneColor

open class HitboxConfiguration {
    @JvmField
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

    @JvmField
    @Switch(name = "Accurate Hitboxes", subcategory = "General Options")
    var accurate = false

    @JvmField
    @Switch(name = "Dashed", subcategory = "General Options")
    var dashedHitbox = false

    @JvmField
    @Switch(name = "Hitbox Outline", subcategory = "General Options")
    var showOutline = true

    @JvmField
    @Switch(name = "Eye Height", subcategory = "General Options")
    var showEyeHeight = true

    @JvmField
    @Switch(name = "Look Vector", subcategory = "General Options")
    var showLookVector = true

    @JvmField
    @Slider(name = "Outline Thickness", subcategory = "General Options", min = 1f, max = 5f)
    var outlineThickness = 2f

    @JvmField
    @Slider(name = "Eye Height Thickness", subcategory = "General Options", min = 1f, max = 5f)
    var eyeHeightThickness = 2f

    @JvmField
    @Slider(name = "Look Vector Thickness", subcategory = "General Options", min = 1f, max = 5f)
    var lookVectorThickness = 2f

    @JvmField
    @Color(name = "Hitbox Outline Color", subcategory = "Color Options", size = 2)
    var outlineColor = OneColor(-1)

    @JvmField
    @Color(name = "Eye Height Color", subcategory = "Color Options", size = 2)
    var eyeHeightColor = OneColor(-0x10000)

    @JvmField
    @Color(name = "Look Vector Color", subcategory = "Color Options", size = 2)
    var lookVectorColor = OneColor(-0xffff01)
}
