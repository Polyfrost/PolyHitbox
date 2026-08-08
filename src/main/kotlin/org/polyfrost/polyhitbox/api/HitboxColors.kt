package org.polyfrost.polyhitbox.api

import net.minecraft.world.entity.Entity

enum class HitboxElement {
    SIDE,
    OUTLINE,
    EYE_HEIGHT,
    VIEW_RAY,
}

fun interface HitboxColorProvider {
    fun color(entity: Entity, element: HitboxElement, argb: Int): Int
}

object HitboxColors {

    @Volatile
    private var providers: Array<HitboxColorProvider> = emptyArray()

    @JvmStatic
    fun register(provider: HitboxColorProvider) {
        synchronized(this) { providers += provider }
    }

    @JvmStatic
    fun unregister(provider: HitboxColorProvider) {
        synchronized(this) {
            providers = providers.filterNot { it === provider }.toTypedArray()
        }
    }

    internal fun snapshot(): Array<HitboxColorProvider> = providers
}
