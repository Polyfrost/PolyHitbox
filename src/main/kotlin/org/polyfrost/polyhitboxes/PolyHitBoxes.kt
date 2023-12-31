package org.polyfrost.polyhitboxes

import cc.polyfrost.oneconfig.events.EventManager
import cc.polyfrost.oneconfig.events.event.KeyInputEvent
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe
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

    var keybindToggled = false
    private var keybindLastPressed = false

    @Mod.EventHandler
    fun onFMLInitialization(event: FMLInitializationEvent) {
        ModConfig
        EventManager.INSTANCE.register(this)
    }

    @Subscribe
    fun onKeyPressed(event: KeyInputEvent) {
        if (!ModConfig.enabled) return
        val nowPressed = ModConfig.toggleKeyBind.isActive
        if (keybindLastPressed == nowPressed) return
        keybindLastPressed = nowPressed
        if (!nowPressed) return
        keybindToggled = !keybindToggled
    }

}
