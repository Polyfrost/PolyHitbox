package org.polyfrost.polyhitbox

import net.fabricmc.api.ClientModInitializer
import org.polyfrost.polyhitbox.config.ModConfig

object PolyHitboxClient : ClientModInitializer {

    override fun onInitializeClient() {
        ModConfig.preload()
    }
}
