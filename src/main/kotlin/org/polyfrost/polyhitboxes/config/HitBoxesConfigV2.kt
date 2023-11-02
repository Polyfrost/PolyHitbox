package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.CustomOption
import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.config.elements.OptionPage
import cc.polyfrost.oneconfig.gui.elements.config.ConfigPageButton
import org.polyfrost.polyhitboxes.PolyHitBoxes
import java.lang.reflect.Field

object HitBoxesConfigV2 :
    Config(Mod(PolyHitBoxes.NAME, ModType.UTIL_QOL), "${PolyHitBoxes.MODID}.json"),
    MutableMap<String, HitboxConfiguration> by ConfigMap {

    @CustomOption
    private val dummy: Boolean = true

    init {
        initialize()
        setupConditions()
    }

    private fun setupConditions() {
        for ((key, value) in optionNames) {
            if (key.contains("showHitbox")) continue
            if (key.contains("global")) continue
            val parent = value.parent as? HitboxConfiguration ?: continue
            val page = key.split(".").getOrNull(0) ?: continue
            value.addDependency("$page.showHitbox") { parent.showHitbox }
        }
    }

    override fun getCustomOption(field: Field, annotation: CustomOption, page: OptionPage, mod: Mod, migrate: Boolean): BasicOption? {
        for ((key, value) in ConfigMap.innerMap) {
            val subcategory = ConfigUtils.getSubCategory(page, value.category, value.subcategory)
            val newPage = OptionPage(key, mod)
            generateOptionList(value.hitboxConfig, newPage, mod, migrate)
            val button = ConfigPageButton(null, null, key, "", value.category, value.subcategory, newPage)
            subcategory.topButtons.add(button)
        }
        return null
    }
}
