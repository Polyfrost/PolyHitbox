package org.polyfrost.polyhitbox

import cc.polyfrost.oneconfig.events.EventManager
import cc.polyfrost.oneconfig.events.event.KeyInputEvent
import cc.polyfrost.oneconfig.events.event.Stage
import cc.polyfrost.oneconfig.events.event.TickEvent
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.polyfrost.polyhitbox.config.ModConfig

@Mod(
    modid = PolyHitbox.MODID,
    name = PolyHitbox.NAME,
    version = PolyHitbox.VERSION,
    modLanguageAdapter = "cc.polyfrost.oneconfig.utils.KotlinLanguageAdapter"
)
object PolyHitbox {
    const val MODID = "@ID@"
    const val NAME = "@NAME@"
    const val VERSION = "@VER@"

    @Mod.EventHandler
    fun onFMLInitialization(event: FMLInitializationEvent) {
        ModConfig
        EventManager.INSTANCE.register(this)
    }

    private var lastEnabled = false

    @Subscribe
    fun onTick(event: TickEvent) {
        if (event.stage != Stage.END) return

        if (ModConfig.enabled) {
            mc.renderManager.isDebugBoundingBox = true
        }
        if (lastEnabled == ModConfig.enabled) return
        if (lastEnabled) {
            mc.renderManager.isDebugBoundingBox = false
        }
        lastEnabled = ModConfig.enabled
    }

    var keybindToggled = false
    private var keybindLastPressed = false

    @Subscribe
    fun onKeyPressed(event: KeyInputEvent) {
        if (!ModConfig.enabled) return

        val nowPressed = ModConfig.toggleKeyBind.isActive
        if (keybindLastPressed == nowPressed) return
        keybindLastPressed = nowPressed
        if (!nowPressed) return
        keybindToggled = !keybindToggled
        ModConfig.toggleState = keybindToggled
        ModConfig.save()
    }
}
