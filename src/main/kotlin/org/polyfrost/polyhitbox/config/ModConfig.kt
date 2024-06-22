package org.polyfrost.polyhitbox.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.CustomOption
import cc.polyfrost.oneconfig.config.annotations.KeyBind
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.core.OneKeyBind
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.config.elements.OptionPage
import cc.polyfrost.oneconfig.events.EventManager
import cc.polyfrost.oneconfig.libs.universal.UKeyboard
import org.polyfrost.polyhitbox.PolyHitbox
import java.lang.reflect.Field

object ModConfig : Config(Mod("Hitbox", ModType.UTIL_QOL, "/${PolyHitbox.MODID}.svg"), "${PolyHitbox.MODID}.json") {
    @KeyBind(name = "Toggle Keybind")
    var toggleKeyBind = OneKeyBind(UKeyboard.KEY_F3, UKeyboard.KEY_B)

    @Switch(name = "Retain Toggle State", description = "Retains the toggle state of hitboxes between sessions.")
    var retainToggleState = true

    var toggleState = false

    @CustomOption
    var configs = HashMap<HitboxCategory, HitboxConfig>()

    init {
        initialize()
        EventManager.INSTANCE.register(this)
    }

    override fun getCustomOption(
        field: Field,
        annotation: CustomOption,
        page: OptionPage,
        mod: Mod,
        migrate: Boolean
    ): BasicOption? {
        for (category in HitboxCategory.entries) { // retain order
            category.config = configs.computeIfAbsent(category) { HitboxConfig() }
            ConfigUtils.getSubCategory(page, category.displayName, "").options.addAll(
                category.config.getOptions(
                    category
                )
            )
        }
        return null
    }

    @Transient
    val sortedByPriority: List<HitboxCategory> =
        (HitboxCategory.entries - HitboxCategory.DEFAULT).sortedBy { it.priority }
}
