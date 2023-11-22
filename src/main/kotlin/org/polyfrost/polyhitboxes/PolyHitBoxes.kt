package org.polyfrost.polyhitboxes

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.polyfrost.polyhitboxes.config.ModConfig

@Mod(
    modid = PolyHitBoxes.MODID,
    name = PolyHitBoxes.NAME,
    version = PolyHitBoxes.VERSION,
    modLanguageAdapter = "cc.polyfrost.oneconfig.utils.KotlinLanguageAdapter"
)
object PolyHitBoxes {
    const val MODID = "@ID@"
    const val NAME = "@NAME@"
    const val VERSION = "@VER@"

    @Mod.EventHandler
    fun onFMLInitialization(event: FMLInitializationEvent) {
        ModConfig // todo: tree entity selector
    }
}
