package org.polyfrost.polyhitboxes.render

import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.events.EventManager
import cc.polyfrost.oneconfig.events.event.TimerUpdateEvent
import cc.polyfrost.oneconfig.gui.OneConfigGui
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe
import cc.polyfrost.oneconfig.libs.universal.UResolution
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.polyfrost.polyhitboxes.config.CategorizedHitboxConfig
import kotlin.math.atan
import net.minecraft.client.renderer.GlStateManager as GL

class HitboxPreview(
    private val config: CategorizedHitboxConfig,
    private val example: Entity?,
    description: String = "",
    category: String = "General",
    subcategory: String = "",
) : BasicOption(null, null, "Hitbox Preview", description, category, subcategory, 2) {
    private data class DrawContext(val x: Int, val y: Int, val mouseX: Float, val mouseY: Float)
    private var drawContext: DrawContext? = null

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun getHeight() = 696 - (30 + 4 * (32 + 16))

    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        drawContext = DrawContext(x, y, inputHandler.mouseX(), inputHandler.mouseY())
    }

    @SubscribeEvent
    fun renderPreview(event: GuiScreenEvent.DrawScreenEvent.Post) {
        val (oneUIX, oneUIY, mouseX, mouseY) = drawContext ?: return
        drawContext = null
        val oneConfigGui = mc.currentScreen as? OneConfigGui ?: return
        val example = example ?: mc.thePlayer ?: return
        val unscaleMC = 1 / UResolution.scaleFactor
        val oneUIScale = OneConfigGui.getScaleFactor() * oneConfigGui.animationScaleFactor
        val rawX = ((UResolution.windowWidth - 800 * oneUIScale) / 2f).toInt()
        val rawY = ((UResolution.windowHeight - 768 * oneUIScale) / 2f).toInt()

        GL.pushMatrix()
        GL.scale(unscaleMC * oneUIScale, unscaleMC * oneUIScale, 1.0)
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor(rawX, rawY, (1024 * oneUIScale).toInt(), (696 * oneUIScale).toInt())
        drawEntityPointingMouse(
            entity = example,
            x = oneUIX - 16 + 512,
            y = oneUIY + 207,
            scale = 150f,
            mouseX = mouseX,
            mouseY = mouseY
        )
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        GL.popMatrix()
    }

    private fun drawEntityPointingMouse(
        entity: Entity,
        x: Int,
        y: Int,
        scale: Float,
        mouseX: Float,
        mouseY: Float,
    ) {
        val dx = x - mouseX
        val dy = y - mouseY

        GL.enableDepth()
        GL.color(1f, 1f, 1f, 1f)
        GL.enableColorMaterial()
        GL.pushMatrix()
        GL.translate(x.toFloat(), y.toFloat(), 50f)
        GL.scale(-scale, scale, scale)
        GL.translate(0f, entity.eyeHeight, 0f)
        GL.rotate(180f, 0f, 0f, 1f)

        val tempData = if (entity is EntityLivingBase) with(entity) {
            TempData(rotationYaw, rotationYawHead, rotationPitch, renderYawOffset, prevRotationYawHead, riddenByEntity)
        } else null

        GL.rotate(135f, 0f, 1f, 0f)
        RenderHelper.enableStandardItemLighting()
        GL.rotate(-135f, 0f, 1f, 0f)

        if (entity is EntityLivingBase) {
            entity.rotationYaw = atan(dx / 40f) * 40f
            entity.rotationYawHead = entity.rotationYaw
            entity.rotationPitch = -atan(dy / 40f) * 20f
            entity.riddenByEntity = entity // cancel nametag
            entity.renderYawOffset = entity.rotationYaw
            entity.prevRotationYawHead = entity.rotationYaw
        }

        GL.rotate(-atan(dy / 40f) * 20f, 1f, 0f, 0f)
        GL.rotate(-atan(dx / 40f) * 40f, 0f, 1f, 0f)

        with(mc.renderManager) {
            playerViewX = 0f
            playerViewY = 180f
            isRenderShadow = false
            doRenderEntity(entity, 0.0, 0.0, 0.0, 0f, 0f, false)
            renderHitbox(config.profile, entity, 0.0, 0.0, 0.0, 0f)
            isRenderShadow = true
        }

        if (entity is EntityLivingBase && tempData != null) with(entity) {
            rotationYaw = tempData.yaw
            rotationYawHead = tempData.yawHead
            rotationPitch = tempData.pitch
            renderYawOffset = tempData.yawOffset
            prevRotationYawHead = tempData.prevYawHead
            riddenByEntity = tempData.riddenBy
        }

        GL.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GL.disableRescaleNormal()
        GL.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GL.disableTexture2D()
        GL.setActiveTexture(OpenGlHelper.defaultTexUnit)
    }
}

private data class TempData(
    val yaw: Float,
    val yawHead: Float,
    val pitch: Float,
    val yawOffset: Float,
    val prevYawHead: Float,
    val riddenBy: Entity?,
)
