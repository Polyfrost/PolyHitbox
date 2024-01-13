package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.CustomOption
import cc.polyfrost.oneconfig.config.annotations.KeyBind
import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.core.OneKeyBind
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.config.elements.OptionPage
import cc.polyfrost.oneconfig.libs.universal.UKeyboard
import org.polyfrost.polyhitboxes.PolyHitBoxes
import org.polyfrost.polyhitboxes.config.data.HitboxCategory
import org.polyfrost.polyhitboxes.config.data.HitboxConfig
import org.polyfrost.polyhitboxes.config.gui.HitboxEditor
import java.lang.reflect.Field

object ModConfig : Config(Mod("Hitbox", ModType.UTIL_QOL, "/${PolyHitBoxes.MODID}.svg"), "${PolyHitBoxes.MODID}.json") {
    @KeyBind(name = "Toggle Keybind", size = 2)
    var toggleKeyBind = OneKeyBind(UKeyboard.KEY_F3, UKeyboard.KEY_B)

    @CustomOption
    var configs = HashMap<HitboxCategory, HitboxConfig>()

    init {
        initialize()
    }

    override fun load() {
        super.load()
        HitboxCategory.loadConfig(configs)
    }

    override fun save() {
        configs = HitboxCategory.saveConfig()
        super.save()
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
