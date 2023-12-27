package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.elements.OptionSubcategory
import cc.polyfrost.oneconfig.utils.InputHandler
import net.minecraft.entity.Entity
import org.polyfrost.polyhitboxes.render.HitboxPreview

class CategorizedHitboxConfig(
    val name: String,
    val condition: (Entity) -> Boolean,
    val priority: Int,
    exampleEntity: Entity?,
) {
    private val options = OptionSubcategory("", "")
    var profile = HitboxProfile()
    private val hitboxPreview = HitboxPreview(this, exampleEntity)

    fun load(profileLoaded: HitboxProfile?) {
        profileLoaded?.let {
            profile = it
        }
        options.options = ConfigUtils.getClassOptions(profile)
        options.options.add(hitboxPreview)
    }

    fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) = options.draw(vg, x, y, inputHandler)
}