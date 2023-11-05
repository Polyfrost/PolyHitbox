package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.annotations.Color
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.OneColor

open class GlobalHitboxConfig: HitboxConfig {
    @Switch(name = "Show Hitbox")
    override var showHitbox = false

    @Switch(name = "Accurate Hitboxes")
    override var accurate = false

    @Switch(name = "Dashed")
    override var dashedHitbox = false

    @Switch(name = "Hitbox Outline")
    override var showOutline = true

    @Switch(name = "Eye Height")
    override var showEyeHeight = true

    @Switch(name = "Look Vector")
    override var showLookVector = true

    @Slider(name = "Outline Thickness", min = 1f, max = 5f)
    override var outlineThickness = 2f

    @Slider(name = "Eye Height Thickness", min = 1f, max = 5f)
    override var eyeHeightThickness = 2f

    @Slider(name = "Look Vector Thickness", min = 1f, max = 5f)
    override var lookVectorThickness = 2f

    @Color(name = "Hitbox Outline Color", size = 2)
    override var outlineColor = OneColor(-1)

    @Color(name = "Eye Height Color", size = 2)
    override var eyeHeightColor = OneColor(-0x10000)

    @Color(name = "Look Vector Color", size = 2)
    override var lookVectorColor = OneColor(-0xffff01)

}
