package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Page
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.data.PageLocation
import org.polyfrost.polyhitboxes.PolyHitBoxes

object ModConfig : Config(Mod(PolyHitBoxes.NAME, ModType.UTIL_QOL), "${PolyHitBoxes.MODID}.json") {
    @Page(name = "Hitbox Tree View", location = PageLocation.BOTTOM)
    var testPage = HitboxTreePage()

    init {
        initialize()
    }
}
