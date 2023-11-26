package org.polyfrost.polyhitboxes

import cc.polyfrost.oneconfig.gui.OneConfigGui
import cc.polyfrost.oneconfig.libs.universal.UGraphics.GL
import cc.polyfrost.oneconfig.libs.universal.UResolution
import cc.polyfrost.oneconfig.platform.Platform
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.polyfrost.polyhitboxes.config.ModConfig
import org.polyfrost.polyhitboxes.config.HitboxProfile

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
        MinecraftForge.EVENT_BUS.register(this)
    }

    var previewHitbox: HitboxProfile? = null

    @SubscribeEvent
    fun renderPreview(event: GuiScreenEvent.DrawScreenEvent.Post) {
        val hitbox = previewHitbox ?: return
        val player = mc.thePlayer ?: return
        val unscaleMC = 1 / UResolution.scaleFactor
        val oneConfigX = UResolution.windowWidth / 2f - 640f
        val oneConfigY = UResolution.windowHeight / 2f - 400f
        val oneConfigScale = OneConfigGui.getScaleFactor()
        val mouseX = (Platform.getMousePlatform().mouseX.toFloat() - oneConfigX) / oneConfigScale
        val mouseY = (Platform.getMousePlatform().mouseY.toFloat() - oneConfigY) / oneConfigScale

        GL.pushMatrix()
        GL.scale(unscaleMC, unscaleMC, 1.0)
        GL.translate(oneConfigX, oneConfigY, 0f)
        GL.scale(oneConfigScale, oneConfigScale, 1f)
        drawEntityPointingMouse(
            hitboxConfig = hitbox,
            entity = player,
            x = 224 + 864,
            y = 72 + 640,
            scale = 100f,
            mouseX = mouseX,
            mouseY = mouseY
        )
        GL.popMatrix()

        previewHitbox = null
    }
}
