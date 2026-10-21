@file:OptIn(ExperimentalOneConfigUiApi::class)

package org.polyfrost.polyhitbox.integration

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screens.Screen
import org.polyfrost.polyhitbox.config.ModConfig
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.OneConfigUI
import org.polyfrost.oneconfig.api.ui.v1.page.OneConfigPages

class PolyHitboxModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*>? {
        // Without Mod Menu the OneConfig compat layer still collects this factory
        // and would register a duplicate entry next to the native config
        if (!FabricLoader.getInstance().isModLoaded("modmenu")) return null
        return ConfigScreenFactory { OneConfigUI.createScreen(OneConfigPages.modConfig(ModConfig.id)) as Screen? }
    }
}
