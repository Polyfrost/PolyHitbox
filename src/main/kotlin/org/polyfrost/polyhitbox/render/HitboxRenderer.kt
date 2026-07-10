package org.polyfrost.polyhitbox.render

import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.polyfrost.polyhitbox.config.HitboxCategory
import org.polyfrost.polyhitbox.config.HitboxConfig
import org.polyfrost.polyhitbox.config.ModConfig
//? if <26.2 {
/*import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
*///?}

/**
 * Draws entity hitboxes.
 *
 * Two backends exist because Minecraft's rendering pipeline diverges:
 *  - `>=26.2` lost the immediate `MultiBufferSource` path and draws through the world-space
 *    [net.minecraft.gizmos.Gizmos] system (stroke/fill cuboids + lines).
 *  - `<26.2` uses `MultiBufferSource` + `RenderType`/`RenderTypes` geometry.
 *
 * Line-style variants (proportioned/dashed) and per-line thickness on the buffered backend are
 * simplified to a plain outline in this port; the gizmo backend honours stroke width.
 */
object HitboxRenderer {

    private fun shouldShow(config: HitboxConfig, entity: Entity, hovered: Entity?): Boolean =
        when (config.showCondition) {
            0 -> true
            1 -> vanillaHitboxesEnabled()
            2 -> entity === hovered
            else -> false
        }

    /** Whether the vanilla debug hitboxes are toggled on (F3+B); the mod overrides their rendering. */
    //? if >=1.21.10 {
    private fun vanillaHitboxesEnabled(): Boolean =
        Minecraft.getInstance().debugEntries.isCurrentlyEnabled(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES)
    //?} else {
    /*private fun vanillaHitboxesEnabled(): Boolean =
        Minecraft.getInstance().entityRenderDispatcher.shouldRenderHitBoxes()
    *///?}

    //? if >=26.2 {
    fun emitGizmos() {
        if (!ModConfig.enabled) return
        val mc = Minecraft.getInstance()
        val level = mc.level ?: return
        val partialTicks = mc.deltaTracker.getGameTimeDeltaPartialTick(false)
        val hovered = mc.crosshairPickEntity
        for (entity in level.entitiesForRendering()) {
            val config = HitboxCategory.resolve(entity)
            if (!shouldShow(config, entity, hovered)) continue
            emit(entity, config, partialTicks, entity === hovered && config.hoverColor)
        }
    }

    private fun emit(entity: Entity, config: HitboxConfig, partialTicks: Float, hover: Boolean) {
        val offset = entity.getPosition(partialTicks).subtract(entity.position())
        val box = entity.boundingBox.move(offset)

        if (config.showSide) {
            val c = if (hover) config.sideHoverColor else config.sideColor
            net.minecraft.gizmos.Gizmos.cuboid(box, net.minecraft.gizmos.GizmoStyle.fill(c.argb))
        }
        if (config.showOutline) {
            val c = if (hover) config.outlineHoverColor else config.outlineColor
            net.minecraft.gizmos.Gizmos.cuboid(box, net.minecraft.gizmos.GizmoStyle.stroke(c.argb, config.outlineThickness))
        }
        if (config.showEyeHeight) {
            val c = if (hover) config.eyeHeightHoverColor else config.eyeHeightColor
            val eyeY = box.minY + entity.eyeHeight
            val eyeBox = AABB(box.minX, eyeY - 0.01, box.minZ, box.maxX, eyeY + 0.01, box.maxZ)
            net.minecraft.gizmos.Gizmos.cuboid(eyeBox, net.minecraft.gizmos.GizmoStyle.stroke(c.argb, config.eyeHeightThickness))
        }
        if (config.showViewRay) {
            val c = if (hover) config.viewRayHoverColor else config.viewRayColor
            val eye = Vec3(entity.getPosition(partialTicks).x, box.minY + entity.eyeHeight, entity.getPosition(partialTicks).z)
            val end = eye.add(entity.getViewVector(partialTicks).scale(2.0))
            net.minecraft.gizmos.Gizmos.line(eye, end, c.argb, config.viewRayThickness)
        }
    }
    //?} else {
    /*fun renderEntity(entity: Entity, pose: PoseStack, buffer: net.minecraft.client.renderer.MultiBufferSource, camX: Double, camY: Double, camZ: Double, partialTicks: Float) {
        if (!ModConfig.enabled) return
        val hovered = Minecraft.getInstance().crosshairPickEntity
        val config = HitboxCategory.resolve(entity)
        if (!shouldShow(config, entity, hovered)) return
        draw(entity, config, pose, buffer, camX, camY, camZ, partialTicks, entity === hovered && config.hoverColor)
    }

    fun renderAll(pose: PoseStack, camX: Double, camY: Double, camZ: Double, partialTicks: Float) {
        if (!ModConfig.enabled) return
        val mc = Minecraft.getInstance()
        val level = mc.level ?: return
        val buffer = mc.renderBuffers().bufferSource()
        val hovered = mc.crosshairPickEntity
        for (entity in level.entitiesForRendering()) {
            val config = HitboxCategory.resolve(entity)
            if (!shouldShow(config, entity, hovered)) continue
            draw(entity, config, pose, buffer, camX, camY, camZ, partialTicks, entity === hovered && config.hoverColor)
        }
        buffer.endBatch(linesType())
        buffer.endBatch(quadsType())
    }

    private fun draw(entity: Entity, config: HitboxConfig, pose: PoseStack, buffer: net.minecraft.client.renderer.MultiBufferSource, camX: Double, camY: Double, camZ: Double, partialTicks: Float, hover: Boolean) {
        val x = entity.x
        val y = entity.y
        val z = entity.z
        val renderX = Mth.lerp(partialTicks.toDouble(), entity.xo, x)
        val renderY = Mth.lerp(partialTicks.toDouble(), entity.yo, y)
        val renderZ = Mth.lerp(partialTicks.toDouble(), entity.zo, z)
        pose.pushPose()
        pose.translate(renderX - camX, renderY - camY, renderZ - camZ)
        val bb = entity.boundingBox
        val box = AABB(bb.minX - x, bb.minY - y, bb.minZ - z, bb.maxX - x, bb.maxY - y, bb.maxZ - z)

        if (config.showSide) {
            val c = if (hover) config.sideHoverColor else config.sideColor
            fillBox(buffer.getBuffer(quadsType()), pose, box, c.argb)
        }
        if (config.showOutline) {
            val c = if (hover) config.outlineHoverColor else config.outlineColor
            outlineBox(buffer.getBuffer(linesType()), pose, box, c.argb)
        }
        if (config.showEyeHeight) {
            val c = if (hover) config.eyeHeightHoverColor else config.eyeHeightColor
            val eyeY = box.minY + entity.eyeHeight
            outlineBox(buffer.getBuffer(linesType()), pose, AABB(box.minX, eyeY - 0.01, box.minZ, box.maxX, eyeY + 0.01, box.maxZ), c.argb)
        }
        if (config.showViewRay) {
            val c = if (hover) config.viewRayHoverColor else config.viewRayColor
            val view = entity.getViewVector(partialTicks)
            val eyeY = box.minY + entity.eyeHeight
            line(buffer.getBuffer(linesType()), pose, 0.0, eyeY, 0.0, view.x * 2.0, eyeY + view.y * 2.0, view.z * 2.0, c.argb)
        }
        pose.popPose()
    }

    private fun outlineBox(vc: VertexConsumer, pose: PoseStack, b: AABB, argb: Int) {
        line(vc, pose, b.minX, b.minY, b.minZ, b.maxX, b.minY, b.minZ, argb)
        line(vc, pose, b.maxX, b.minY, b.minZ, b.maxX, b.minY, b.maxZ, argb)
        line(vc, pose, b.maxX, b.minY, b.maxZ, b.minX, b.minY, b.maxZ, argb)
        line(vc, pose, b.minX, b.minY, b.maxZ, b.minX, b.minY, b.minZ, argb)
        line(vc, pose, b.minX, b.maxY, b.minZ, b.maxX, b.maxY, b.minZ, argb)
        line(vc, pose, b.maxX, b.maxY, b.minZ, b.maxX, b.maxY, b.maxZ, argb)
        line(vc, pose, b.maxX, b.maxY, b.maxZ, b.minX, b.maxY, b.maxZ, argb)
        line(vc, pose, b.minX, b.maxY, b.maxZ, b.minX, b.maxY, b.minZ, argb)
        line(vc, pose, b.minX, b.minY, b.minZ, b.minX, b.maxY, b.minZ, argb)
        line(vc, pose, b.maxX, b.minY, b.minZ, b.maxX, b.maxY, b.minZ, argb)
        line(vc, pose, b.maxX, b.minY, b.maxZ, b.maxX, b.maxY, b.maxZ, argb)
        line(vc, pose, b.minX, b.minY, b.maxZ, b.minX, b.maxY, b.maxZ, argb)
    }

    private fun line(vc: VertexConsumer, pose: PoseStack, x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, argb: Int) {
        var nx = (x2 - x1).toFloat(); var ny = (y2 - y1).toFloat(); var nz = (z2 - z1).toFloat()
        val len = Mth.sqrt(nx * nx + ny * ny + nz * nz)
        if (len > 1.0e-5f) { nx /= len; ny /= len; nz /= len }
        vc.addVertex(pose.last(), x1.toFloat(), y1.toFloat(), z1.toFloat()).setColor(argb).setNormal(pose.last(), nx, ny, nz)
        vc.addVertex(pose.last(), x2.toFloat(), y2.toFloat(), z2.toFloat()).setColor(argb).setNormal(pose.last(), nx, ny, nz)
    }

    private fun fillBox(vc: VertexConsumer, pose: PoseStack, b: AABB, argb: Int) {
        quad(vc, pose, b.minX, b.minY, b.minZ, b.maxX, b.minY, b.minZ, b.maxX, b.minY, b.maxZ, b.minX, b.minY, b.maxZ, argb)
        quad(vc, pose, b.minX, b.maxY, b.minZ, b.minX, b.maxY, b.maxZ, b.maxX, b.maxY, b.maxZ, b.maxX, b.maxY, b.minZ, argb)
        quad(vc, pose, b.minX, b.minY, b.minZ, b.minX, b.maxY, b.minZ, b.maxX, b.maxY, b.minZ, b.maxX, b.minY, b.minZ, argb)
        quad(vc, pose, b.minX, b.minY, b.maxZ, b.maxX, b.minY, b.maxZ, b.maxX, b.maxY, b.maxZ, b.minX, b.maxY, b.maxZ, argb)
        quad(vc, pose, b.minX, b.minY, b.minZ, b.minX, b.minY, b.maxZ, b.minX, b.maxY, b.maxZ, b.minX, b.maxY, b.minZ, argb)
        quad(vc, pose, b.maxX, b.minY, b.minZ, b.maxX, b.maxY, b.minZ, b.maxX, b.maxY, b.maxZ, b.maxX, b.minY, b.maxZ, argb)
    }

    private fun quad(vc: VertexConsumer, pose: PoseStack, x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, x3: Double, y3: Double, z3: Double, x4: Double, y4: Double, z4: Double, argb: Int) {
        vc.addVertex(pose.last(), x1.toFloat(), y1.toFloat(), z1.toFloat()).setColor(argb)
        vc.addVertex(pose.last(), x2.toFloat(), y2.toFloat(), z2.toFloat()).setColor(argb)
        vc.addVertex(pose.last(), x3.toFloat(), y3.toFloat(), z3.toFloat()).setColor(argb)
        vc.addVertex(pose.last(), x4.toFloat(), y4.toFloat(), z4.toFloat()).setColor(argb)
    }*/
    //?}

    //? if >=1.21.11 {
    private fun linesType() = net.minecraft.client.renderer.rendertype.RenderTypes.lines()
    private fun quadsType() = net.minecraft.client.renderer.rendertype.RenderTypes.debugQuads()
    //?} else {
    /*private fun linesType() = net.minecraft.client.renderer.rendertype.RenderType.lines()
    private fun quadsType() = net.minecraft.client.renderer.rendertype.RenderType.debugQuads()
    *///?}
}
