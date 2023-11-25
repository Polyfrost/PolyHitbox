package org.polyfrost.polyhitboxes

import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.EntityLivingBase
import org.polyfrost.polyhitboxes.hooks.renderHitbox

fun renderLiving(entity: EntityLivingBase, x: Float, y: Float, scale: Float, rotation: Int) {
    GlStateManager.enableColorMaterial()
    GlStateManager.pushMatrix()
    GlStateManager.translate(x + (40.0 * scale), y + (107.0 * scale), 50.0)
    GlStateManager.scale(-(scale * 50), scale * 50, scale * 50)
    GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f)

    val tempRYO = entity.renderYawOffset
    val tempRY = entity.rotationYaw
    val tempRP = entity.rotationPitch
    val tempPRYH = entity.prevRotationYawHead
    val tempRYN = entity.rotationYawHead

    GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f)
    RenderHelper.enableStandardItemLighting()
    GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f)
    val actualRotation = 360F - rotation

    entity.renderYawOffset = actualRotation
    entity.rotationYaw = actualRotation
    entity.rotationYawHead = entity.rotationYaw
    entity.prevRotationYawHead = entity.rotationYaw
    entity.riddenByEntity
//    GlStateManager.translate(0.0f, 0.0f, 0.0f)
    val renderManager = mc.renderManager
    renderManager.setPlayerViewY(180.0f)
    renderManager.isRenderShadow = false
    renderManager.doRenderEntity(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, true)
    renderHitbox(entity, 0.0, 0.0, 0.0, 1.0f)
    renderManager.isRenderShadow = true

    entity.renderYawOffset = tempRYO
    entity.rotationYaw = tempRY
    entity.rotationPitch = tempRP
    entity.prevRotationYawHead = tempPRYH
    entity.rotationYawHead = tempRYN

    GlStateManager.popMatrix()
}