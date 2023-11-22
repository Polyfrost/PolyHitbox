package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.annotations.Color
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.OneColor

class GlobalHitboxConfig : HitboxConfig {
    @Switch(name = "Show Hitbox", size = 2)
    override var showHitbox = false

    @Switch(name = "Accurate Hitboxes")
    override var accurate = false

    @Switch(name = "Dashed")
    override var dashedHitbox = false

    @Switch(name = "Hitbox Outline")
    override var showOutline = true

    @Color(name = "Hitbox Outline Color")
    override var outlineColor = OneColor(255, 255, 255, 255)

    @Slider(name = "Outline Thickness", min = 1f, max = 5f)
    override var outlineThickness = 2f

    @Switch(name = "Eye Height")
    override var showEyeHeight = true

    @Color(name = "Eye Height Color")
    override var eyeHeightColor = OneColor(255, 0, 0, 255)

    @Slider(name = "Eye Height Thickness", min = 1f, max = 5f)
    override var eyeHeightThickness = 2f

    @Switch(name = "Look Vector")
    override var showLookVector = true

    @Color(name = "Look Vector Color")
    override var lookVectorColor = OneColor(0, 0, 255, 255)

    @Slider(name = "Look Vector Thickness", min = 1f, max = 5f)
    override var lookVectorThickness = 2f

}
