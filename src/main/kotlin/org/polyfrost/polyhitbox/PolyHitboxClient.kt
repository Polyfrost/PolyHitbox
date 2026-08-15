package org.polyfrost.polyhitbox

import net.fabricmc.api.ClientModInitializer
import org.polyfrost.oneconfig.api.ui.v1.keybind.KeybindManager
import org.polyfrost.polyhitbox.config.ModConfig

object PolyHitboxClient : ClientModInitializer {

    override fun onInitializeClient() {
        ModConfig.preload()
        KeybindManager.register(ModConfig.toggleKeybind)
    }
}
