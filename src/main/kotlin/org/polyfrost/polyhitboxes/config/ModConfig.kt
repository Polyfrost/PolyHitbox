package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.CustomOption
import cc.polyfrost.oneconfig.config.annotations.Page
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.data.PageLocation
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.config.elements.OptionPage
import net.minecraft.entity.Entity
import org.polyfrost.polyhitboxes.PolyHitBoxes
import org.polyfrost.polyhitboxes.config.tree.HitboxTreePage
import java.lang.reflect.Field

object ModConfig : Config(Mod(PolyHitBoxes.NAME, ModType.UTIL_QOL), "${PolyHitBoxes.MODID}.json") {
    @CustomOption(id = "global")
    var global = GlobalHitboxConfig()

    @CustomOption(id = "filtered")
    var filtered = FilteredHitboxList()

    @Page(name = "Hitbox Tree View", location = PageLocation.BOTTOM)
    var testPage = HitboxTreePage()

    init {
        initialize()
    }

    fun getEntityHitbox(entity: Entity): HitboxConfig =
        filtered.firstOrNull { hitbox ->
            hitbox.passFilter(entity)
        } ?: global

    override fun getCustomOption(field: Field, annotation: CustomOption, page: OptionPage, mod: Mod, migrate: Boolean): BasicOption? {
        when (annotation.id) {
            "global" -> generateOptionList(global, page, mod, false)
            "filtered" -> return filtered.addOptionTo(this, page, category = "Advanced", subcategory = "Profiles")
        }
        return null
    }
}
