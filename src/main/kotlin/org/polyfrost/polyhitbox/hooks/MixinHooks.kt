package org.polyfrost.polyhitbox.hooks

import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.entity.Entity
import org.polyfrost.polyhitbox.PolyHitbox
import org.polyfrost.polyhitbox.config.ModConfig
import org.polyfrost.polyhitbox.render.HitboxRenderer

fun overrideHitbox(entity: Entity, x: Double, y: Double, z: Double, partialTicks: Float): Boolean {
    val config = ModConfig.getHitboxConfig(entity)
    if (!ModConfig.enabled) return false
    val condition = when (config.showCondition) {
        0 -> true
        1 -> if (ModConfig.retainToggleState) ModConfig.toggleState else PolyHitbox.keybindToggled
        2 -> entity == mc.pointedEntity
        else -> false
    }
    if (condition) {
        HitboxRenderer.renderHitbox(config, entity, x, y, z, partialTicks)
    }
    return condition
}