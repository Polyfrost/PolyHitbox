package org.polyfrost.polyhitboxes

import cc.polyfrost.oneconfig.gui.OneConfigGui
import cc.polyfrost.oneconfig.libs.universal.UResolution
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
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
        MinecraftForge.EVENT_BUS.register(this)
    }

    var pageRendered = false

    @SubscribeEvent
    fun renderPreview(event: GuiScreenEvent.DrawScreenEvent.Post) {
        if (!pageRendered) return
        val player = mc.thePlayer ?: return
        val unscale = 1 / UResolution.scaleFactor.toFloat()
        GlStateManager.scale(unscale, unscale, 10f)
        val scaleBy = OneConfigGui.getScaleFactor() / UResolution.scaleFactor.toFloat()
        val oneConfigX = UResolution.windowWidth / 2f - 640f
        val oneConfigY = UResolution.windowHeight / 2f - 400f
        val pageX = oneConfigX + 224f / scaleBy
        val pageY = oneConfigY + 72f / scaleBy

        GlStateManager.enableDepth()
        GlStateManager.color(1f, 1f, 1f, 1f)
        renderLiving(player, pageX, pageY, scaleBy, 0)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)

        pageRendered = false
    }
}
