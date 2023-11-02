package org.polyfrost.polyhitboxes.hooks

import cc.polyfrost.oneconfig.config.core.OneColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import org.polyfrost.polyhitboxes.config.ConfigMap
import org.polyfrost.polyhitboxes.config.HitBoxesConfig
import org.polyfrost.polyhitboxes.config.HitBoxesConfigV2
import org.polyfrost.polyhitboxes.config.HitboxConfiguration
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

fun injectHitbox(entity: Entity, x: Double, y: Double, z: Double, partialTicks: Float, callbackInfo: CallbackInfo) {
    if (!HitBoxesConfigV2.enabled) return
    renderHitbox(entity, x, y, z, partialTicks)
    callbackInfo.cancel()
}

private fun renderHitbox(entityIn: Entity, x: Double, y: Double, z: Double, partialTicks: Float) {
    val hitboxConfig = ConfigMap.getEntityType(entityIn)
    if (!hitboxConfig.showHitbox) return

    GlStateManager.depthMask(false)
    GlStateManager.disableTexture2D()
    GlStateManager.disableLighting()
    GlStateManager.disableCull()
    GlStateManager.disableBlend()

    if (hitboxConfig.showOutline) {
        drawOutline(hitboxConfig, entityIn, x, y, z)
    }
    if (hitboxConfig.showEyeHeight) {
        drawEyeHeight(hitboxConfig, entityIn, x, y, z)
    }
    if (hitboxConfig.showLookVector) {
        drawLookVector(hitboxConfig, entityIn, x, y, z, partialTicks)
    }

    GlStateManager.enableTexture2D()
    GlStateManager.enableLighting()
    GlStateManager.enableCull()
    GlStateManager.disableBlend()
    GlStateManager.depthMask(true)
}

private const val ALTERNATING_PATTERN = 0b1010101010101010.toShort()

private fun drawOutline(hitboxConfig: HitboxConfiguration, entity: Entity, x: Double, y: Double, z: Double) {
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glLineWidth(hitboxConfig.outlineThickness)

    if (hitboxConfig.dashedHitbox) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GL11.glLineStipple(10, ALTERNATING_PATTERN)
        GL11.glEnable(GL11.GL_LINE_STIPPLE)
        GL11.glBegin(GL11.GL_LINES)
        GL11.glEnd()
    }

    val box = entity.entityBoundingBox
    var expandedBox = AxisAlignedBB(box.minX - entity.posX + x, box.minY - entity.posY + y, box.minZ - entity.posZ + z, box.maxX - entity.posX + x, box.maxY - entity.posY + y, box.maxZ - entity.posZ + z)

    if (hitboxConfig.accurate) {
        expandedBox = expandedBox.expand(entity.collisionBorderSize.toDouble(), entity.collisionBorderSize.toDouble(), entity.collisionBorderSize.toDouble())
    }

    drawOutlinedBoundingBox(expandedBox, hitboxConfig.outlineColor)

    if (hitboxConfig.dashedHitbox) {
        GL11.glPopAttrib()
    }

    GL11.glDisable(GL11.GL_BLEND)
}

private fun drawEyeHeight(hitboxConfig: HitboxConfiguration, entity: Entity, x: Double, y: Double, z: Double) {
    GL11.glLineWidth(hitboxConfig.eyeHeightThickness)

    val halfWidth = entity.width / 2.0f

    val eyeHeightBox = AxisAlignedBB(
        x - halfWidth.toDouble(),
        y + entity.eyeHeight.toDouble() - 0.01,
        z - halfWidth.toDouble(),
        x + halfWidth.toDouble(),
        y + entity.eyeHeight.toDouble() + 0.01,
        z + halfWidth.toDouble()
    )

    drawOutlinedBoundingBox(eyeHeightBox, hitboxConfig.eyeHeightColor)
}


private fun drawLookVector(hitboxConfig: HitboxConfiguration, entity: Entity, x: Double, y: Double, z: Double, partialTicks: Float) {
    val color = hitboxConfig.lookVectorColor
    GL11.glLineWidth(hitboxConfig.lookVectorThickness)
    GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

    val tessellator = Tessellator.getInstance()
    with(tessellator.worldRenderer) {
        val lookVector = entity.getLook(partialTicks)
        begin(3, DefaultVertexFormats.POSITION)
        pos(x, y + entity.eyeHeight.toDouble(), z).endVertex()
        pos(x + lookVector.xCoord * 2.0, y + entity.eyeHeight.toDouble() + lookVector.yCoord * 2.0, z + lookVector.zCoord * 2.0).endVertex()
    }
    tessellator.draw()

    GlStateManager.color(1f, 1f, 1f, 1f)
}


private fun drawOutlinedBoundingBox(boundingBox: AxisAlignedBB, color: OneColor) =
    RenderGlobal.drawOutlinedBoundingBox(boundingBox, color.red, color.green, color.blue, color.alpha)