package org.polyfrost.polyhitbox.hooks

import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.entity.Entity
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import org.polyfrost.polyhitbox.PolyHitbox
import org.polyfrost.polyhitbox.config.ModConfig
import org.polyfrost.polyhitbox.render.HitboxRenderer

fun overrideHitbox(entity: Entity, x: Double, y: Double, z: Double, partialTicks: Float): Boolean {
    if (entity !is EntityHook) return false
    val config = entity.`polyHitbox$getHitboxConfig`()
    if (!ModConfig.enabled) return false
    val condition = when (config.showCondition) {
        0 -> true
        1 -> if (ModConfig.retainToggleState) ModConfig.toggleState else PolyHitbox.keybindToggled
        2 -> mc.objectMouseOver.typeOfHit == MovingObjectType.ENTITY && entity == mc.objectMouseOver.entityHit
        else -> false
    }
    if (condition) {
        HitboxRenderer.tryAddToQueue(config, entity, x, y, z, partialTicks)
    }
    return true
}