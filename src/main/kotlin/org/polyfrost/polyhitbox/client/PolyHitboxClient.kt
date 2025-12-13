package org.polyfrost.polyhitbox.client

import dev.deftu.omnicore.api.client.client
import org.polyfrost.oneconfig.api.config.v1.ConfigManager

object PolyHitboxClient {
    private val hitboxInfo = HitboxInfo("hitbox.yaml")

    private var hitboxesEnabled: Boolean
        get() {
            //#if MC < 1.14
            //$$ return client.renderManager.isDebugBoundingBox
            //#else
            return client.entityRenderDispatcher.shouldRenderHitboxes()
            //#endif
        }
        set(value) {
            //#if MC < 1.14
            //$$ client.renderManager.isDebugBoundingBox = value
            //#else
            client.entityRenderDispatcher.setRenderHitboxes(value)
            //#endif
        }

    fun initialize() {
        hitboxInfo.tree.title = "PolyHitbox"
        hitboxInfo.tree = ConfigManager.active().register(hitboxInfo.tree).tree
        var enabled = false
        if (hitboxInfo.showMode != 2) {
            enabled = true
        }

        //#if FABRIC && MC > 1.14
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STARTED.register { tick ->
            hitboxesEnabled = enabled
        }
        //#else
        //hitboxesEnabled = enabled
        //#endif
    }

    fun getHitboxInfo(): HitboxInfo {
        return hitboxInfo
    }
}
