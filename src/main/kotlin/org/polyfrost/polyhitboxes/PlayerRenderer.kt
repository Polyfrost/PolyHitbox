package org.polyfrost.polyhitboxes

import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import org.polyfrost.polyhitboxes.config.tree.HitboxProfile
import kotlin.math.atan
import net.minecraft.client.renderer.GlStateManager as GL

fun drawEntityPointingMouse(hitboxConfig: HitboxProfile, entity: EntityLivingBase, x: Int, y: Int, scale: Float, mouseX: Float, mouseY: Float) {
    val dx = x - mouseX
    val dy = y - entity.eyeHeight * scale - mouseY

    GL.enableDepth()
    GL.color(1f, 1f, 1f, 1f)
    GL.enableColorMaterial()
    GL.pushMatrix()
    GL.translate(x.toFloat(), y.toFloat(), 50f)
    GL.scale(-scale, scale, scale)
    GL.rotate(180f, 0f, 0f, 1f)

    val tempRYO = entity.renderYawOffset
    val tempRY = entity.rotationYaw
    val tempRP = entity.rotationPitch
    val tempPRYH = entity.prevRotationYawHead
    val tempRYN = entity.rotationYawHead
    val tempRBE = entity.riddenByEntity

    GL.rotate(135f, 0f, 1f, 0f)
    RenderHelper.enableStandardItemLighting()
    GL.rotate(-135f, 0f, 1f, 0f)

    entity.rotationYaw = atan(dx / 40f) * 40f
    entity.renderYawOffset = entity.rotationYaw
    entity.rotationYawHead = entity.rotationYaw
    entity.prevRotationYawHead = entity.rotationYaw
    entity.rotationPitch = -atan(dy / 40f) * 20f
    entity.riddenByEntity = entity // cancel nametag

    GL.rotate(entity.rotationPitch, 1f, 0f, 0f)
    GL.rotate(entity.rotationYaw, 0f, 1f, 0f)

    val renderManager = mc.renderManager
    renderManager.setPlayerViewY(180.0f)
    renderManager.isRenderShadow = false
    renderManager.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0f, 1f)
    renderManager.isRenderShadow = true
    renderHitbox(hitboxConfig, entity, 0.0, 0.0, 0.0, 1f)

    entity.renderYawOffset = tempRYO
    entity.rotationYaw = tempRY
    entity.rotationPitch = tempRP
    entity.prevRotationYawHead = tempPRYH
    entity.rotationYawHead = tempRYN
    entity.riddenByEntity = tempRBE

    GL.popMatrix()
    RenderHelper.disableStandardItemLighting()
    GL.disableRescaleNormal()
    GL.setActiveTexture(OpenGlHelper.lightmapTexUnit)
    GL.disableTexture2D()
    GL.setActiveTexture(OpenGlHelper.defaultTexUnit)
}

fun renderHitbox(hitboxConfig: HitboxProfile, entityIn: Entity, x: Double, y: Double, z: Double, partialTicks: Float) {
    if (!hitboxConfig.showHitbox) return

    GL.depthMask(false)
    GL.disableTexture2D()
    GL.disableLighting()
    GL.disableCull()
    GL.disableBlend()
    if (hitboxConfig.showOutline) {
        drawOutline(hitboxConfig, entityIn, x, y, z)
    }
    if (hitboxConfig.showEyeHeight) {
        drawEyeHeight(hitboxConfig, entityIn, x, y, z)
    }
    if (hitboxConfig.showLookVector) {
        drawLookVector(hitboxConfig, entityIn, x, y, z, partialTicks)
    }
    GL.enableTexture2D()
    GL.enableLighting()
    GL.enableCull()
    GL.disableBlend()
    GL.depthMask(true)
}

private const val ALTERNATING_PATTERN = 0b1010101010101010.toShort()

private fun drawOutline(hitboxConfig: HitboxProfile, entity: Entity, x: Double, y: Double, z: Double) {
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glLineWidth(hitboxConfig.outlineThickness)

    if (hitboxConfig.dashedLines) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GL11.glLineStipple(10, ALTERNATING_PATTERN)
        GL11.glEnable(GL11.GL_LINE_STIPPLE)
        GL11.glBegin(GL11.GL_LINES)
        GL11.glEnd()
    }

    val box = entity.entityBoundingBox
    var expandedBox = AxisAlignedBB(
        box.minX - entity.posX + x,
        box.minY - entity.posY + y,
        box.minZ - entity.posZ + z,
        box.maxX - entity.posX + x,
        box.maxY - entity.posY + y,
        box.maxZ - entity.posZ + z,
    )

    if (hitboxConfig.accurate) {
        val collisionBorderSize = entity.collisionBorderSize.toDouble()
        expandedBox = expandedBox.expand(collisionBorderSize, collisionBorderSize, collisionBorderSize)
    }

    drawOutlinedBoundingBox(expandedBox, hitboxConfig.outlineColor)

    if (hitboxConfig.dashedLines) {
        GL11.glPopAttrib()
    }

    GL11.glDisable(GL11.GL_BLEND)
}

private fun drawEyeHeight(hitboxConfig: HitboxProfile, entity: Entity, x: Double, y: Double, z: Double) {
    GL11.glLineWidth(hitboxConfig.eyeHeightThickness)

    val halfWidth = entity.width / 2.0
    val eyeHeight = entity.eyeHeight

    val eyeHeightBox = AxisAlignedBB(
        x - halfWidth,
        y + eyeHeight - 0.01,
        z - halfWidth,
        x + halfWidth,
        y + eyeHeight + 0.01,
        z + halfWidth,
    )

    drawOutlinedBoundingBox(eyeHeightBox, hitboxConfig.eyeHeightColor)
}

private fun drawLookVector(hitboxConfig: HitboxProfile, entity: Entity, x: Double, y: Double, z: Double, partialTicks: Float) {
    val color = hitboxConfig.lookVectorColor
    GL11.glLineWidth(hitboxConfig.lookVectorThickness)
    GL.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
    val lookVector = entity.getLook(partialTicks)

    val tessellator = Tessellator.getInstance()
    val worldRenderer = tessellator.worldRenderer
    worldRenderer.begin(3, DefaultVertexFormats.POSITION)
    worldRenderer.pos(x, y + entity.eyeHeight, z).endVertex()
    worldRenderer.pos(x + lookVector.xCoord * 2.0, y + entity.eyeHeight + lookVector.yCoord * 2.0, z + lookVector.zCoord * 2.0).endVertex()
    tessellator.draw()

    GL.color(1f, 1f, 1f, 1f)
}

private fun drawOutlinedBoundingBox(boundingBox: AxisAlignedBB, color: OneColor) =
    RenderGlobal.drawOutlinedBoundingBox(boundingBox, color.red, color.green, color.blue, color.alpha)
