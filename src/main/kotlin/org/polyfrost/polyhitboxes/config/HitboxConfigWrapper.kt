package org.polyfrost.polyhitboxes.config

import java.lang.reflect.Field

class HitboxConfigWrapper(private val field: Field, val category: String) {
    val getOrNull: HitboxConfiguration?
        get() = try {
            this.field.get(HitBoxesConfig) as HitboxConfiguration?
        } catch (e: Exception) {
            null
        }

    val categoryOrDefault: HitboxConfiguration
        get() = try {
            val hitboxConfig = getCategory()

            if (hitboxConfig != null && hitboxConfig.global) {
                hitboxConfig
            } else {
                this.field.get(HitBoxesConfig) as HitboxConfiguration
            }
        } catch (e: Exception) {
            HitBoxesConfig.player
        }

    private fun getCategory() = when (category) {
        "Passive Entities" -> HitBoxesConfig.passive
        "Hostile Entities" -> HitBoxesConfig.hostile
        "Projectiles" -> HitBoxesConfig.projectile
        "Others" -> HitBoxesConfig.other
        else -> null
    }
}
