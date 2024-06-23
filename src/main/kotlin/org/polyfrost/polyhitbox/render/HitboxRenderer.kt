package org.polyfrost.polyhitbox.render

import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.polyfrost.polyhitbox.config.HitboxConfig
import org.polyfrost.polyhitbox.config.ModConfig
import kotlin.math.atan2
import kotlin.math.sqrt
import net.minecraft.client.renderer.GlStateManager as GL

object HitboxRenderer {
    private const val ALTERNATING_PATTERN = 0b1010101010101010.toShort()

    data class RenderInfo(val config: HitboxConfig, val entity: Entity, val x: Double, val y: Double, val z: Double, val partialTicks: Float)

    private val renderQueue = ArrayList<RenderInfo>()

    var drawingWorld = false

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!ModConfig.enabled) return
        if (renderQueue.isEmpty()) return
        GL.pushMatrix()
        for (info in renderQueue) {
            with(info) {
                renderHitbox(config, entity, x, y, z, partialTicks)
            }
        }
        renderQueue.clear()
        GL.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GL.disableRescaleNormal()
        GL.disableBlend()
    }

    fun tryAddToQueue(config: HitboxConfig, entity: Entity, x: Double, y: Double, z: Double, partialTicks: Float) {
        if (drawingWorld) {
            renderQueue.add(RenderInfo(config, entity, x, y, z, partialTicks))
        } else {
            renderHitbox(config, entity, x, y, z, partialTicks)
        }
    }

    fun renderHitbox(
        config: HitboxConfig,
        entity: Entity,
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float,
    ) {
        GL.depthMask(false)
        GL.disableTexture2D()
        GL.disableLighting()
        GL.disableCull()
        GL.alphaFunc(516, 0.1f)
        GL.enableBlend()
        GL.tryBlendFuncSeparate(770, 771, 1, 0)
        GL.pushMatrix()
        GL.translate(x, y, z)

        if (config.lineStyle == 2) {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
            GL11.glLineStipple(config.dashFactor, ALTERNATING_PATTERN)
            GL11.glEnable(GL11.GL_LINE_STIPPLE)
            GL11.glBegin(GL11.GL_LINES)
            GL11.glEnd()
        }

        val eyeHeight = entity.eyeHeight.toDouble()
        var hitbox = entity.entityBoundingBox.offset(-entity.posX, -entity.posY, -entity.posZ)
        if (config.accurate) {
            val border = entity.collisionBorderSize.toDouble()
            hitbox = hitbox.expand(border, border, border)
        }

        val hovered = config.hoverColor && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && entity == mc.objectMouseOver.entityHit

        if (config.showSide) drawSide(config, hitbox, hovered)
        if (config.showOutline) drawBoxOutline(config, hitbox, if (hovered) config.outlineHoverColor else config.outlineColor, config.outlineThickness)
        if (config.showEyeHeight) drawEyeHeight(config, hitbox, eyeHeight, hovered)
        if (config.showViewRay) drawViewRay(config, entity, hovered, partialTicks)

        if (config.lineStyle == 2) {
            GL11.glPopAttrib()
        }

        GL.popMatrix()
        GL.enableTexture2D()
        GL.enableLighting()
        GL.enableCull()
        GL.disableBlend()
        GL.depthMask(true)

    }

    private fun drawSide(config: HitboxConfig, hitbox: AxisAlignedBB, hovered: Boolean) {
        val color = if (hovered) config.sideHoverColor else config.sideColor
        glColor(color)
        buildAndDraw(GL11.GL_TRIANGLE_STRIP) {
            pos(hitbox.maxX, hitbox.maxY, hitbox.minZ).endVertex()
            pos(hitbox.minX, hitbox.maxY, hitbox.minZ).endVertex()
            pos(hitbox.maxX, hitbox.maxY, hitbox.maxZ).endVertex()
            pos(hitbox.minX, hitbox.maxY, hitbox.maxZ).endVertex()
            pos(hitbox.maxX, hitbox.minY, hitbox.maxZ).endVertex()
            pos(hitbox.minX, hitbox.minY, hitbox.maxZ).endVertex()
            pos(hitbox.maxX, hitbox.minY, hitbox.minZ).endVertex()
            pos(hitbox.minX, hitbox.minY, hitbox.minZ).endVertex()
        }
        buildAndDraw(GL11.GL_TRIANGLE_STRIP) {
            pos(hitbox.maxX, hitbox.maxY, hitbox.maxZ).endVertex()
            pos(hitbox.maxX, hitbox.minY, hitbox.maxZ).endVertex()
            pos(hitbox.maxX, hitbox.maxY, hitbox.minZ).endVertex()
            pos(hitbox.maxX, hitbox.minY, hitbox.minZ).endVertex()
            pos(hitbox.minX, hitbox.maxY, hitbox.minZ).endVertex()
            pos(hitbox.minX, hitbox.minY, hitbox.minZ).endVertex()
            pos(hitbox.minX, hitbox.maxY, hitbox.maxZ).endVertex()
            pos(hitbox.minX, hitbox.minY, hitbox.maxZ).endVertex()
        }
        GL.color(1f, 1f, 1f, 1f)
    }

    private fun drawEyeHeight(config: HitboxConfig, hitbox: AxisAlignedBB, eyeHeight: Double, hovered: Boolean) {
        val eyeHeightBox = AxisAlignedBB(
            hitbox.minX - 0.01, eyeHeight - 0.01, hitbox.minZ - 0.01,
            hitbox.maxX + 0.01, eyeHeight + 0.01, hitbox.maxZ + 0.01,
        )
        drawBoxOutline(config, eyeHeightBox, if (hovered) config.eyeHeightHoverColor else config.eyeHeightColor, config.eyeHeightThickness)
    }

    private fun drawViewRay(profile: HitboxConfig, entity: Entity, hovered: Boolean, partialTicks: Float) {
        val color = if (hovered) profile.viewRayHoverColor else profile.viewRayColor
        val viewVector = entity.getLook(partialTicks)
        drawLine(
            profile,
            0.0, entity.eyeHeight.toDouble(), 0.0,
            viewVector.xCoord * 2.0, entity.eyeHeight + viewVector.yCoord * 2.0, viewVector.zCoord * 2.0,
            color, profile.viewRayThickness
        )
    }

    private fun glColor(oneColor: OneColor) = with(oneColor) {
        GL.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
    }

    private fun drawBoxOutline(config: HitboxConfig, box: AxisAlignedBB, color: OneColor, thickness: Float) {
        drawLine(config, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, color, thickness)
        drawLine(config, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, color, thickness)
        drawLine(config, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, color, thickness)
        drawLine(config, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, color, thickness)
        drawLine(config, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, color, thickness)
        drawLine(config, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, color, thickness)
        drawLine(config, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, color, thickness)
        drawLine(config, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, color, thickness)
        drawLine(config, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, color, thickness)
        drawLine(config, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, color, thickness)
        drawLine(config, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, color, thickness)
        drawLine(config, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, color, thickness)
    }

    private fun drawLine(config: HitboxConfig, xFrom: Double, yFrom: Double, zFrom: Double, xTo: Double, yTo: Double, zTo: Double, color: OneColor, thinkness: Float) {
        if (color.alpha != 255) {
            GL.enableAlpha()
        }
        glColor(color)
        if (config.lineStyle == 1) {
            drawProportionedLine(xFrom, yFrom, zFrom, xTo, yTo, zTo, thinkness)
        } else {
            drawGLLine(xFrom, yFrom, zFrom, xTo, yTo, zTo, thinkness)
        }
        if (color.alpha != 255) {
            GL.disableAlpha()
        }
        GL.color(1f, 1f, 1f, 1f)
    }

    private fun drawGLLine(xFrom: Double, yFrom: Double, zFrom: Double, xTo: Double, yTo: Double, zTo: Double, thinkness: Float) {
        GL11.glLineWidth(thinkness)
        buildAndDraw(GL11.GL_LINES) {
            pos(xFrom, yFrom, zFrom).endVertex()
            pos(xTo, yTo, zTo).endVertex()
        }
    }

    private fun drawProportionedLine(xFrom: Double, yFrom: Double, zFrom: Double, xTo: Double, yTo: Double, zTo: Double, thickness: Float) {
        val dx = xTo - xFrom
        val dy = yTo - yFrom
        val dz = zTo - zFrom
        val dHorizontal = sqrt(dx * dx + dz * dz)
        val distance = sqrt(dx * dx + dy * dy + dz * dz)
        val pitch = Math.toDegrees(atan2(dy, dHorizontal)).toFloat()
        val yaw = -Math.toDegrees(atan2(dz, dx)).toFloat()
        val thicknessInBlocks = thickness.toDouble() / 200.0

        GL.pushMatrix()
        GL.translate(xFrom, yFrom, zFrom)
        GL.rotate(yaw, 0f, 1f, 0f)
        GL.rotate(pitch, 0f, 0f, 1f)
        buildAndDraw(GL11.GL_TRIANGLE_STRIP) {
            pos(0.0, 0.0, -thicknessInBlocks).endVertex()
            pos(0.0, 0.0, thicknessInBlocks).endVertex()
            pos(distance, 0.0, -thicknessInBlocks).endVertex()
            pos(distance, 0.0, thicknessInBlocks).endVertex()
        }
        buildAndDraw(GL11.GL_TRIANGLE_STRIP) {
            pos(0.0, -thicknessInBlocks, 0.0).endVertex()
            pos(0.0, thicknessInBlocks, 0.0).endVertex()
            pos(distance, -thicknessInBlocks, 0.0).endVertex()
            pos(distance, thicknessInBlocks, 0.0).endVertex()
        }
        GL.popMatrix()
    }

    private fun buildAndDraw(glMode: Int, block: WorldRenderer.() -> Unit) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(glMode, DefaultVertexFormats.POSITION)
        worldRenderer.block()
        tessellator.draw()
    }
}
