package org.polyfrost.polyhitbox.api

import net.minecraft.world.entity.Entity
import org.slf4j.LoggerFactory

enum class HitboxElement {
    SIDE,
    OUTLINE,
    EYE_HEIGHT,
    VIEW_RAY,
}

enum class HitboxCondition {
    HOVERED,
    IFRAME,
}

interface HitboxColorContext {
    val entity: Entity
    val element: HitboxElement

    fun has(condition: HitboxCondition): Boolean
}

fun interface HitboxColorProvider {
    fun color(context: HitboxColorContext, argb: Int): Int
}

object HitboxColors {

    private val logger = LoggerFactory.getLogger("PolyHitbox")

    @Volatile
    private var providers: Array<HitboxColorProvider> = emptyArray()

    @JvmStatic
    fun register(provider: HitboxColorProvider) {
        synchronized(this) {
            if (providers.none { it === provider }) providers += provider
        }
    }

    @JvmStatic
    fun unregister(provider: HitboxColorProvider) {
        synchronized(this) {
            providers = providers.filterNot { it === provider }.toTypedArray()
        }
    }

    internal fun snapshot(): Array<HitboxColorProvider> = providers

    internal fun disable(provider: HitboxColorProvider, error: Throwable) {
        unregister(provider)
        logger.error("Unregistered hitbox color provider {} because it threw.", provider.javaClass.name, error)
    }
}
