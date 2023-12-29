package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.CustomOption
import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.config.elements.OptionPage
import org.polyfrost.polyhitboxes.PolyHitBoxes
import org.polyfrost.polyhitboxes.config.data.ConfigAdapter
import org.polyfrost.polyhitboxes.config.gui.HitboxEditor
import java.lang.reflect.Field

object ModConfig : Config(Mod(PolyHitBoxes.NAME, ModType.UTIL_QOL), "${PolyHitBoxes.MODID}.json") {
    @CustomOption
    var configs = ConfigAdapter()

    init {
        initialize()
    }

    override fun getCustomOption(
        field: Field,
        annotation: CustomOption,
        page: OptionPage,
        mod: Mod,
        migrate: Boolean,
    ): BasicOption {
        val option = HitboxEditor()
        ConfigUtils.getSubCategory(page, "General", "").options.add(option)
        return option
    }
}
