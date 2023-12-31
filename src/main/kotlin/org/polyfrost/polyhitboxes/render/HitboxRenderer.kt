package org.polyfrost.polyhitboxes.render

import cc.polyfrost.oneconfig.config.core.OneColor
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import org.polyfrost.polyhitboxes.config.data.HitboxConfig
import net.minecraft.client.renderer.GlStateManager as GL

object HitboxRenderer {
    private const val ALTERNATING_PATTERN = 0b1010101010101010.toShort()

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
        GL.enableBlend()
        GL.pushMatrix()
        GL.translate(x, y, z)

        if (config.dashedLines) {
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

        if (config.showSide) drawSide(config, hitbox)
        if (config.showOutline) drawOutline(config, hitbox)
        if (config.showEyeHeight) drawEyeHeight(config, hitbox, eyeHeight)
        if (config.showViewRay) drawViewRay(config, entity, partialTicks)

        if (config.dashedLines) {
            GL11.glPopAttrib()
        }

        GL.popMatrix()
        GL.enableTexture2D()
        GL.enableLighting()
        GL.enableCull()
        GL.disableBlend()
        GL.depthMask(true)
    }

    private fun drawSide(config: HitboxConfig, hitbox: AxisAlignedBB) {
        glColor(config.sideColor)
        startBuilding(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION) {
            pos(hitbox.maxX, hitbox.maxY, hitbox.minZ).endVertex()
            pos(hitbox.minX, hitbox.maxY, hitbox.minZ).endVertex()
            pos(hitbox.maxX, hitbox.maxY, hitbox.maxZ).endVertex()
            pos(hitbox.minX, hitbox.maxY, hitbox.maxZ).endVertex()
            pos(hitbox.maxX, hitbox.minY, hitbox.maxZ).endVertex()
            pos(hitbox.minX, hitbox.minY, hitbox.maxZ).endVertex()
            pos(hitbox.maxX, hitbox.minY, hitbox.minZ).endVertex()
            pos(hitbox.minX, hitbox.minY, hitbox.minZ).endVertex()
        }
        startBuilding(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION) {
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

    private fun drawOutline(config: HitboxConfig, hitbox: AxisAlignedBB) {
        GL11.glLineWidth(config.outlineThickness.toFloat())
        drawOutlinedBoundingBox(hitbox, config.outlineColor)
    }

    private fun drawEyeHeight(config: HitboxConfig, hitbox: AxisAlignedBB, eyeHeight: Double) {
        val eyeHeightBox = AxisAlignedBB(
            hitbox.minX, eyeHeight - 0.01, hitbox.minZ,
            hitbox.maxX, eyeHeight + 0.01, hitbox.maxZ
        )
        GL11.glLineWidth(config.eyeHeightThickness.toFloat())
        drawOutlinedBoundingBox(eyeHeightBox, config.eyeHeightColor)
    }

    private fun drawViewRay(profile: HitboxConfig, entity: Entity, partialTicks: Float) {
        val color = profile.viewRayColor
        val viewVector = entity.getLook(partialTicks)

        GL11.glLineWidth(profile.viewRayThickness.toFloat())
        glColor(color)
        startBuilding(GL11.GL_LINES, DefaultVertexFormats.POSITION) {
            pos(0.0, entity.eyeHeight.toDouble(), 0.0).endVertex()
            pos(viewVector.xCoord * 2.0, entity.eyeHeight + viewVector.yCoord * 2.0, viewVector.zCoord * 2.0).endVertex()
        }
        GL.color(1f, 1f, 1f, 1f)
    }

    private fun glColor(oneColor: OneColor) = with(oneColor) {
        GL.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
    }

    private fun drawOutlinedBoundingBox(boundingBox: AxisAlignedBB, color: OneColor) =
        RenderGlobal.drawOutlinedBoundingBox(boundingBox, color.red, color.green, color.blue, color.alpha)

    private fun startBuilding(glMode: Int, format: VertexFormat, block: WorldRenderer.() -> Unit) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(glMode, format)
        worldRenderer.block()
        tessellator.draw()
    }
}
