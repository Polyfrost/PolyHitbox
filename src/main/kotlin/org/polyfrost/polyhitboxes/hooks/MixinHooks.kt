package org.polyfrost.polyhitboxes.hooks

import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.entity.Entity
import org.polyfrost.polyhitboxes.config.ModConfig
import org.polyfrost.polyhitboxes.config.data.HitboxCategory
import org.polyfrost.polyhitboxes.render.HitboxRenderer

private var isHitboxToggled: Boolean? = null

fun preRenderHitbox() {
    if (!ModConfig.enabled) return
    isHitboxToggled = mc.renderManager.isDebugBoundingBox
    mc.renderManager.isDebugBoundingBox = true
}

fun overrideHitbox(entity: Entity, x: Double, y: Double, z: Double, partialTicks: Float): Boolean {
    val hitboxToggled = isHitboxToggled ?: return false
    val config = HitboxCategory.getHitboxConfig(entity)
    val condition = when (config.showCondition) {
        0 -> true
        1 -> hitboxToggled
        2 -> entity == mc.pointedEntity
        else -> false
    }
    if (condition) {
        HitboxRenderer.renderHitbox(config, entity, x, y, z, partialTicks)
    }
    return true
}

fun postRenderHitbox() {
    val hitboxToggled = isHitboxToggled ?: return
    mc.renderManager.isDebugBoundingBox = hitboxToggled
    isHitboxToggled = null
}
