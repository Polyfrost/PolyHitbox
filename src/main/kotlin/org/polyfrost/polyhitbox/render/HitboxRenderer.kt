package org.polyfrost.polyhitbox.render

import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.polyfrost.polyhitbox.config.HitboxCategory
import org.polyfrost.polyhitbox.config.HitboxConfig
import org.polyfrost.polyhitbox.config.ModConfig
import kotlin.math.abs
import kotlin.math.min
//? if <26.2 {
/*import com.mojang.blaze3d.vertex.PoseStack
*///?}

/**
 * Draws entity hitboxes.
 *
 * Two backends exist because Minecraft's rendering pipeline diverges:
 *  - `>=26.2` lost the immediate `MultiBufferSource` path and draws through the world-space
 *    [net.minecraft.gizmos.Gizmos] system (stroke/fill cuboids, lines and rects).
 *  - `<26.2` uses `MultiBufferSource` + `RenderType`/`RenderTypes` geometry.
 *
 * Line styles are reimplemented geometrically since GL line stipple has no modern equivalent:
 * normal edges are lines, dashed edges are split into world-space dash segments, and proportioned
 * edges are world-space quad "tubes" whose width always tracks the thickness. Line thickness for
 * the normal/dashed styles is honoured from 1.21.11 on (lines carry a per-vertex width there) and
 * on the gizmo backend; below that lines are a fixed width.
 */
object HitboxRenderer {

    private const val NORMAL = 0
    private const val PROPORTIONED = 1
    private const val DASHED = 2

    /** World-space half-width of a proportioned line per thickness unit (matches the legacy `/200`). */
    private const val PROPORTIONED_HALF = 1.0 / 200.0

    /** World-space dash length per dash-factor step. */
    private const val DASH_STEP = 0.05

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

    // --- Shared, backend-agnostic geometry -------------------------------------------------------

    private fun boxEdges(b: AABB): List<Pair<Vec3, Vec3>> = listOf(
        Vec3(b.minX, b.minY, b.minZ) to Vec3(b.maxX, b.minY, b.minZ),
        Vec3(b.maxX, b.minY, b.minZ) to Vec3(b.maxX, b.minY, b.maxZ),
        Vec3(b.maxX, b.minY, b.maxZ) to Vec3(b.minX, b.minY, b.maxZ),
        Vec3(b.minX, b.minY, b.maxZ) to Vec3(b.minX, b.minY, b.minZ),
        Vec3(b.minX, b.maxY, b.minZ) to Vec3(b.maxX, b.maxY, b.minZ),
        Vec3(b.maxX, b.maxY, b.minZ) to Vec3(b.maxX, b.maxY, b.maxZ),
        Vec3(b.maxX, b.maxY, b.maxZ) to Vec3(b.minX, b.maxY, b.maxZ),
        Vec3(b.minX, b.maxY, b.maxZ) to Vec3(b.minX, b.maxY, b.minZ),
        Vec3(b.minX, b.minY, b.minZ) to Vec3(b.minX, b.maxY, b.minZ),
        Vec3(b.maxX, b.minY, b.minZ) to Vec3(b.maxX, b.maxY, b.minZ),
        Vec3(b.maxX, b.minY, b.maxZ) to Vec3(b.maxX, b.maxY, b.maxZ),
        Vec3(b.minX, b.minY, b.maxZ) to Vec3(b.minX, b.maxY, b.maxZ),
    )

    private fun dashSegments(from: Vec3, to: Vec3, dashFactor: Int): List<Pair<Vec3, Vec3>> {
        val total = to.subtract(from).length()
        if (total < 1.0e-6) return listOf(from to to)
        val dashLen = (dashFactor * DASH_STEP).coerceAtLeast(0.02)
        val dir = to.subtract(from).scale(1.0 / total)
        val segments = ArrayList<Pair<Vec3, Vec3>>()
        var t = 0.0
        while (t < total) {
            segments.add(from.add(dir.scale(t)) to from.add(dir.scale(min(t + dashLen, total))))
            t += dashLen * 2.0
        }
        return segments
    }

    /** Two perpendicular quads forming a world-space tube along `from`..`to`; each quad is 4 corners. */
    private fun tubeQuads(from: Vec3, to: Vec3, halfWidth: Double): List<List<Vec3>> {
        val delta = to.subtract(from)
        val len = delta.length()
        if (len < 1.0e-6) return emptyList()
        val dir = delta.scale(1.0 / len)
        val reference = if (abs(dir.y) < 0.99) Vec3(0.0, 1.0, 0.0) else Vec3(1.0, 0.0, 0.0)
        val p1 = dir.cross(reference).normalize().scale(halfWidth)
        val p2 = dir.cross(p1).normalize().scale(halfWidth)
        return listOf(
            listOf(from.add(p1), to.add(p1), to.subtract(p1), from.subtract(p1)),
            listOf(from.add(p2), to.add(p2), to.subtract(p2), from.subtract(p2)),
        )
    }

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
        val current = entity.getPosition(partialTicks)
        val box = entity.boundingBox.move(current.subtract(entity.position()))

        if (config.showSide) {
            val c = if (hover) config.sideHoverColor else config.sideColor
            net.minecraft.gizmos.Gizmos.cuboid(box, net.minecraft.gizmos.GizmoStyle.fill(c.argb))
        }
        if (config.showOutline) {
            val c = if (hover) config.outlineHoverColor else config.outlineColor
            gizmoBox(config, box, c.argb, config.outlineThickness)
        }
        if (config.showEyeHeight) {
            val c = if (hover) config.eyeHeightHoverColor else config.eyeHeightColor
            val eyeY = box.minY + entity.eyeHeight
            gizmoBox(config, AABB(box.minX, eyeY - 0.01, box.minZ, box.maxX, eyeY + 0.01, box.maxZ), c.argb, config.eyeHeightThickness)
        }
        if (config.showViewRay) {
            val c = if (hover) config.viewRayHoverColor else config.viewRayColor
            val eye = Vec3(current.x, box.minY + entity.eyeHeight, current.z)
            gizmoEdge(config, eye, eye.add(entity.getViewVector(partialTicks).scale(2.0)), c.argb, config.viewRayThickness)
        }
    }

    private fun gizmoBox(config: HitboxConfig, box: AABB, argb: Int, thickness: Float) {
        if (config.lineStyle == NORMAL) {
            net.minecraft.gizmos.Gizmos.cuboid(box, net.minecraft.gizmos.GizmoStyle.stroke(argb, thickness))
        } else {
            for ((from, to) in boxEdges(box)) gizmoEdge(config, from, to, argb, thickness)
        }
    }

    private fun gizmoEdge(config: HitboxConfig, from: Vec3, to: Vec3, argb: Int, thickness: Float) {
        when (config.lineStyle) {
            PROPORTIONED -> for (quad in tubeQuads(from, to, thickness * PROPORTIONED_HALF)) {
                net.minecraft.gizmos.Gizmos.rect(quad[0], quad[1], quad[2], quad[3], net.minecraft.gizmos.GizmoStyle.fill(argb))
            }
            DASHED -> for ((a, b) in dashSegments(from, to, config.dashFactor)) {
                net.minecraft.gizmos.Gizmos.line(a, b, argb, thickness)
            }
            else -> net.minecraft.gizmos.Gizmos.line(from, to, argb, thickness)
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
            fillBox(buffer, pose, box, c.argb)
        }
        if (config.showOutline) {
            val c = if (hover) config.outlineHoverColor else config.outlineColor
            styledBox(config, buffer, pose, box, c.argb, config.outlineThickness)
        }
        if (config.showEyeHeight) {
            val c = if (hover) config.eyeHeightHoverColor else config.eyeHeightColor
            val eyeY = box.minY + entity.eyeHeight
            styledBox(config, buffer, pose, AABB(box.minX, eyeY - 0.01, box.minZ, box.maxX, eyeY + 0.01, box.maxZ), c.argb, config.eyeHeightThickness)
        }
        if (config.showViewRay) {
            val c = if (hover) config.viewRayHoverColor else config.viewRayColor
            val eyeY = box.minY + entity.eyeHeight
            val view = entity.getViewVector(partialTicks)
            styledEdge(config, buffer, pose, Vec3(0.0, eyeY, 0.0), Vec3(view.x * 2.0, eyeY + view.y * 2.0, view.z * 2.0), c.argb, config.viewRayThickness)
        }
        pose.popPose()
    }

    private fun styledBox(config: HitboxConfig, buffer: net.minecraft.client.renderer.MultiBufferSource, pose: PoseStack, box: AABB, argb: Int, thickness: Float) {
        for ((from, to) in boxEdges(box)) styledEdge(config, buffer, pose, from, to, argb, thickness)
    }

    private fun styledEdge(config: HitboxConfig, buffer: net.minecraft.client.renderer.MultiBufferSource, pose: PoseStack, from: Vec3, to: Vec3, argb: Int, thickness: Float) {
        when (config.lineStyle) {
            PROPORTIONED -> for (quad in tubeQuads(from, to, thickness * PROPORTIONED_HALF)) fillQuad(buffer, pose, quad, argb)
            DASHED -> for ((a, b) in dashSegments(from, to, config.dashFactor)) line(buffer, pose, a, b, argb, thickness)
            else -> line(buffer, pose, from, to, argb, thickness)
        }
    }

    // MultiBufferSource.BufferSource can only build one shared layer at a time and ends the previous
    // when the render type changes, so the consumer is fetched per primitive rather than cached.
    private fun line(buffer: net.minecraft.client.renderer.MultiBufferSource, pose: PoseStack, from: Vec3, to: Vec3, argb: Int, thickness: Float) {
        val vc = buffer.getBuffer(linesType())
        val delta = to.subtract(from)
        val len = delta.length()
        val n = if (len > 1.0e-6) delta.scale(1.0 / len) else delta
        lineWidth(vc.addVertex(pose.last(), from.x.toFloat(), from.y.toFloat(), from.z.toFloat()).setColor(argb).setNormal(pose.last(), n.x.toFloat(), n.y.toFloat(), n.z.toFloat()), thickness)
        lineWidth(vc.addVertex(pose.last(), to.x.toFloat(), to.y.toFloat(), to.z.toFloat()).setColor(argb).setNormal(pose.last(), n.x.toFloat(), n.y.toFloat(), n.z.toFloat()), thickness)
    }

    private fun fillBox(buffer: net.minecraft.client.renderer.MultiBufferSource, pose: PoseStack, b: AABB, argb: Int) {
        fillQuad(buffer, pose, listOf(Vec3(b.minX, b.minY, b.minZ), Vec3(b.maxX, b.minY, b.minZ), Vec3(b.maxX, b.minY, b.maxZ), Vec3(b.minX, b.minY, b.maxZ)), argb)
        fillQuad(buffer, pose, listOf(Vec3(b.minX, b.maxY, b.minZ), Vec3(b.minX, b.maxY, b.maxZ), Vec3(b.maxX, b.maxY, b.maxZ), Vec3(b.maxX, b.maxY, b.minZ)), argb)
        fillQuad(buffer, pose, listOf(Vec3(b.minX, b.minY, b.minZ), Vec3(b.minX, b.maxY, b.minZ), Vec3(b.maxX, b.maxY, b.minZ), Vec3(b.maxX, b.minY, b.minZ)), argb)
        fillQuad(buffer, pose, listOf(Vec3(b.minX, b.minY, b.maxZ), Vec3(b.maxX, b.minY, b.maxZ), Vec3(b.maxX, b.maxY, b.maxZ), Vec3(b.minX, b.maxY, b.maxZ)), argb)
        fillQuad(buffer, pose, listOf(Vec3(b.minX, b.minY, b.minZ), Vec3(b.minX, b.minY, b.maxZ), Vec3(b.minX, b.maxY, b.maxZ), Vec3(b.minX, b.maxY, b.minZ)), argb)
        fillQuad(buffer, pose, listOf(Vec3(b.maxX, b.minY, b.minZ), Vec3(b.maxX, b.maxY, b.minZ), Vec3(b.maxX, b.maxY, b.maxZ), Vec3(b.maxX, b.minY, b.maxZ)), argb)
    }

    private fun fillQuad(buffer: net.minecraft.client.renderer.MultiBufferSource, pose: PoseStack, corners: List<Vec3>, argb: Int) {
        val vc = buffer.getBuffer(quadsType())
        for (c in corners) vc.addVertex(pose.last(), c.x.toFloat(), c.y.toFloat(), c.z.toFloat()).setColor(argb)
    }*/
    //?}

    // The lines() vertex format gained a mandatory per-vertex LineWidth element in 1.21.11; older
    // versions have no such element, so setting it is a no-op there.
    //? if >=1.21.11 {
    private fun linesType() = net.minecraft.client.renderer.rendertype.RenderTypes.lines()
    private fun quadsType() = net.minecraft.client.renderer.rendertype.RenderTypes.debugQuads()
    private fun lineWidth(vertex: com.mojang.blaze3d.vertex.VertexConsumer, thickness: Float): com.mojang.blaze3d.vertex.VertexConsumer = vertex.setLineWidth(thickness)
    //?} else {
    /*private fun linesType() = net.minecraft.client.renderer.rendertype.RenderType.lines()
    private fun quadsType() = net.minecraft.client.renderer.rendertype.RenderType.debugQuads()
    private fun lineWidth(vertex: com.mojang.blaze3d.vertex.VertexConsumer, thickness: Float): com.mojang.blaze3d.vertex.VertexConsumer = vertex
    *///?}
}
