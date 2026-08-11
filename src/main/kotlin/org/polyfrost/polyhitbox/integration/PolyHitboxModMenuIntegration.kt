package org.polyfrost.polyhitbox.integration

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.fabricmc.loader.api.FabricLoader
import org.polyfrost.polyhitbox.config.ModConfig
import org.polyfrost.oneconfig.internal.ui.compose.impls.OneConfigUIScreen

class PolyHitboxModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*>? {
        // Without Mod Menu the OneConfig compat layer still collects this factory
        // and would register a duplicate entry next to the native config
        if (!FabricLoader.getInstance().isModLoaded("modmenu")) return null
        return ConfigScreenFactory { OneConfigUIScreen(ModConfig.id) }
    }
}
