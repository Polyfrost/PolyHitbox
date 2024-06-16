package org.polyfrost.polyhitboxes.hooks

import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.entity.Entity
import org.polyfrost.polyhitboxes.PolyHitBoxes
import org.polyfrost.polyhitboxes.config.ModConfig
import org.polyfrost.polyhitboxes.render.HitboxRenderer

fun overrideHitbox(entity: Entity, x: Double, y: Double, z: Double, partialTicks: Float): Boolean {
    if (!ModConfig.enabled) return false
    val config = ModConfig.getHitboxConfig(entity)
    val condition = when (config.showCondition) {
        0 -> true
        1 -> PolyHitBoxes.keybindToggled
        2 -> entity == mc.pointedEntity
        else -> false
    }
    if (condition) {
        HitboxRenderer.renderHitbox(config, entity, x, y, z, partialTicks)
    }
    return true
}
