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
//? if <1.21.11 {
/*import com.mojang.blaze3d.vertex.PoseStack
*///?}

/**
 * Draws entity hitboxes.
 *
 * Two backends exist because Minecraft's rendering pipeline diverges:
 *  - `>=1.21.11` draws through the world-space [net.minecraft.gizmos.Gizmos] system (stroke/fill
 *    cuboids, lines and rects). Gizmos are collected during the frame and drawn after the entity
 *    models are flushed to the depth buffer, so the hitbox composites correctly against them; this
 *    is exactly where vanilla renders its own hitboxes on these versions.
 *  - `<1.21.11` uses `MultiBufferSource` + `RenderType` geometry.
 *
 * Line styles are reimplemented geometrically since GL line stipple has no modern equivalent. To
 * stay consistent with the 26.2 gizmo backend (the closest match to the legacy look), normal and
 * dashed edges are camera-facing ribbons of constant screen-space width, while proportioned edges
 * are world-space quad "tubes" whose world width scales with camera distance, so they read as
 * thinner up close and thicker far away. Both backends share the same geometry;
 * the buffered backend billboards ribbons on the CPU, the gizmo backend uses screen-space gizmo
 * lines. Thickness therefore behaves the same on every version.
 */
object HitboxRenderer {

    private const val NORMAL = 0
    private const val PROPORTIONED = 1
    private const val DASHED = 2

    /**
     * World-space half-width of a proportioned line per thickness unit per block of camera distance
     * (matches the legacy `/200` at one block).
     */
    private const val PROPORTIONED_HALF = 1.0 / 200.0

    /** Screen-space ribbon width calibration, tuned to match the 26.2 gizmo backend. */
    private const val WIDTH_SCALE = 1.4f

    /** World-space dash length per dash-factor step. */
    private const val DASH_STEP = 0.005
    private const val MIN_DASH = 0.03

    private fun shouldShow(config: HitboxConfig, entity: Entity, hovered: Entity?): Boolean {
        if (isFirstPersonSelf(entity)) return false
        return when (config.showCondition) {
            0 -> true
            1 -> vanillaHitboxesEnabled()
            2 -> entity === hovered
            else -> false
        }
    }

    /**
     * In first-person view the camera sits inside the entity it's attached to (normally the local
     * player), so drawing its hitbox would smear geometry across the whole screen. Skip it; in
     * third-person the body is visible, so the hitbox stays.
     */
    private fun isFirstPersonSelf(entity: Entity): Boolean {
        val mc = Minecraft.getInstance()
        return mc.options.cameraType.isFirstPerson && entity === mc.cameraEntity
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
        val dashLen = (dashFactor * DASH_STEP).coerceAtLeast(MIN_DASH)
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

    //? if >=1.21.11 {
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
            PROPORTIONED -> {
                val cam = Minecraft.getInstance().entityRenderDispatcher.camera!!.position()
                val half = thickness * PROPORTIONED_HALF * from.add(to).scale(0.5).distanceTo(cam)
                for (quad in tubeQuads(from, to, half)) {
                    net.minecraft.gizmos.Gizmos.rect(quad[0], quad[1], quad[2], quad[3], net.minecraft.gizmos.GizmoStyle.fill(argb))
                }
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

    /**
     * Submit-pipeline backend (1.21.10). Called from the tail of `DebugRenderer.render`, which runs
     * after the entity model batches have been flushed to the depth buffer, so the depth-tested
     * hitbox composites correctly against them. The pose is left identity: geometry is emitted in
     * camera-relative world space and the billboard uses camera distance, so it does not depend on
     * the pose carrying the view matrix.
     */
    fun renderAfterEntities(camX: Double, camY: Double, camZ: Double, partialTicks: Float) {
        if (!ModConfig.enabled) return
        val mc = Minecraft.getInstance()
        val level = mc.level ?: return
        val pose = PoseStack()
        val buffer = mc.renderBuffers().bufferSource()
        val hovered = mc.crosshairPickEntity
        for (entity in level.entitiesForRendering()) {
            val config = HitboxCategory.resolve(entity)
            if (!shouldShow(config, entity, hovered)) continue
            draw(entity, config, pose, buffer, camX, camY, camZ, partialTicks, entity === hovered && config.hoverColor)
        }
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
            PROPORTIONED -> {
                val mid = from.add(to).scale(0.5)
                val v = org.joml.Vector3f(mid.x.toFloat(), mid.y.toFloat(), mid.z.toFloat())
                pose.last().pose().transformPosition(v)
                val half = thickness * PROPORTIONED_HALF * v.length().toDouble()
                for (quad in tubeQuads(from, to, half)) fillQuad(buffer, pose, quad, argb)
            }
            DASHED -> for ((a, b) in dashSegments(from, to, config.dashFactor)) screenLine(buffer, pose, a, b, thickness, argb)
            else -> screenLine(buffer, pose, from, to, thickness, argb)
        }
    }

    /**
     * Draws an edge as a camera-facing ribbon of constant screen-space width, matching the legacy
     * glLineWidth look and the 26.2 gizmo backend. The endpoints are pushed to view space, offset
     * perpendicular to the line by a depth-scaled amount so the width stays constant in pixels, then
     * the offset is rotated back into model space for emission through the pose.
     */
    private fun screenLine(buffer: net.minecraft.client.renderer.MultiBufferSource, pose: PoseStack, from: Vec3, to: Vec3, thickness: Float, argb: Int) {
        val m = pose.last().pose()
        val va = org.joml.Vector3f(from.x.toFloat(), from.y.toFloat(), from.z.toFloat()); m.transformPosition(va)
        val vb = org.joml.Vector3f(to.x.toFloat(), to.y.toFloat(), to.z.toFloat()); m.transformPosition(vb)
        val dir = org.joml.Vector3f(vb).sub(va)
        if (dir.lengthSquared() < 1.0e-9f) return
        dir.normalize()
        val toModel = org.joml.Matrix3f(m).transpose()
        val vpH = Minecraft.getInstance().window.height.toFloat()
        // The projection matrix isn't readable on 1.21.8+, so derive its focal scale from the
        // vertical FOV: the perspective m11 term is 1/tan(fov/2).
        val focal = (1.0 / kotlin.math.tan(Math.toRadians(Minecraft.getInstance().options.fov().get().toDouble()) * 0.5)).toFloat()
        val offA = billboardOffset(va, dir, thickness, focal, vpH, toModel)
        val offB = billboardOffset(vb, dir, thickness, focal, vpH, toModel)
        fillQuad(buffer, pose, listOf(from.add(offA), to.add(offB), to.subtract(offB), from.subtract(offA)), argb)
    }

    private fun billboardOffset(v: org.joml.Vector3f, dir: org.joml.Vector3f, thickness: Float, focal: Float, vpH: Float, toModel: org.joml.Matrix3f): Vec3 {
        // cross(lineDir, viewRay) is perpendicular to both the line and the view ray, so it faces the
        // camera and lies exactly in the plane tangent to the view ray (perp is orthogonal to v).
        val perp = org.joml.Vector3f(dir).cross(v)
        if (perp.lengthSquared() < 1.0e-9f) return Vec3.ZERO
        perp.normalize()
        // Distance from the camera (rotation-invariant, so it works whether the pose is the view
        // matrix or identity). Since perp is tangent to the view ray it projects at the clean rate
        // focal/dist, so a world offset that spans a fixed pixel width is halfPx * dist / (focal *
        // vpH/2) with no projected-length division — bounded even when the line is viewed end-on.
        val dist = v.length().coerceAtLeast(0.05f)
        val mag = thickness * 0.5f * WIDTH_SCALE * dist / (focal * vpH * 0.5f)
        perp.mul(mag)
        toModel.transform(perp)
        return Vec3(perp.x().toDouble(), perp.y().toDouble(), perp.z().toDouble())
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
    }

    private fun quadsType() = net.minecraft.client.renderer.rendertype.RenderType.debugQuads()*/
    //?}
}
