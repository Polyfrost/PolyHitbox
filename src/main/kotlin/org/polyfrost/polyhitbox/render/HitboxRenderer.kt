package org.polyfrost.polyhitbox.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import org.polyfrost.polyhitbox.config.HitboxCategory
import org.polyfrost.polyhitbox.config.HitboxConfig
import org.polyfrost.polyhitbox.config.ModConfig
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tan

object HitboxRenderer {

    private const val DASH_STEP = 0.005
    private const val MIN_DASH = 0.03

    private const val VIEW_RAY_LENGTH = 2.0

    private const val CULL_MARGIN = 0.5

    private const val EPSILON = 1.0e-12

    private const val NEAR_PLANE = 0.05

    private var camX = 0.0
    private var camY = 0.0
    private var camZ = 0.0

    private var fwdX = 0.0
    private var fwdY = 0.0
    private var fwdZ = 0.0

    private var partialTicks = 0f

    private var lastFrameKey = Double.NaN

    private var ribbonScale = 0.0
    private var lastFov = -1f
    private var lastViewportHeight = -1

    private var cullFrustum: Frustum? = null
    private var hovered: Entity? = null
    private var viewer: Player? = null
    private var selfInFirstPerson: Entity? = null
    private var vanillaToggle = false

    private var offX = 0.0
    private var offY = 0.0
    private var offZ = 0.0

    private fun active(): Boolean = ModConfig.enabled && !(ModConfig.hideInF1 && guiHidden())

    //? if >=26.2 {
    private fun guiHidden(): Boolean = Minecraft.getInstance().gui.hud.isHidden
    //?} else {
    /*private fun guiHidden(): Boolean = Minecraft.getInstance().options.hideGui
    *///?}

    //? if >=1.21.10 {
    private fun vanillaHitboxesEnabled(): Boolean =
        Minecraft.getInstance().debugEntries.isCurrentlyEnabled(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES)
    //?} else {
    /*private fun vanillaHitboxesEnabled(): Boolean =
        Minecraft.getInstance().entityRenderDispatcher.shouldRenderHitBoxes()
    *///?}

    //? if >=1.21.11 {
    private fun readCamera(camera: Camera) {
        val pos = camera.position()
        camX = pos.x
        camY = pos.y
        camZ = pos.z
        val forward = camera.forwardVector()
        fwdX = forward.x().toDouble()
        fwdY = forward.y().toDouble()
        fwdZ = forward.z().toDouble()
    }
    //?} else {
    /*private fun readCamera(camera: Camera) {
        val pos = camera.position
        camX = pos.x
        camY = pos.y
        camZ = pos.z
        val forward = camera.lookVector
        fwdX = forward.x.toDouble()
        fwdY = forward.y.toDouble()
        fwdZ = forward.z.toDouble()
    }
    *///?}

    //? if >=1.21.4 {
    private fun partialTick(): Float = Minecraft.getInstance().deltaTracker.getGameTimeDeltaPartialTick(false)
    //?} else {
    /*private fun partialTick(): Float = Minecraft.getInstance().timer.getGameTimeDeltaPartialTick(false)
    *///?}

    // The FOV actually being rendered, including the sprint/Speed modifier, the FOV Effects slider
    // and the death/lava animations, so the on-screen width holds still through those transitions.
    //? if >=26.1 {
    private fun effectiveFov(camera: Camera): Float = camera.fov
    //?} elif >=1.21.4 {
    /*private fun effectiveFov(camera: Camera): Float =
        (Minecraft.getInstance().gameRenderer as org.polyfrost.polyhitbox.mixin.FovAccessor).`polyhitbox$fov`(camera, partialTicks, true)
    *///?} else {
    /*private fun effectiveFov(camera: Camera): Float =
        (Minecraft.getInstance().gameRenderer as org.polyfrost.polyhitbox.mixin.FovAccessor).`polyhitbox$fov`(camera, partialTicks, true).toFloat()
    *///?}

    //? if >=1.21.11 {
    private fun quadsType() = net.minecraft.client.renderer.rendertype.RenderTypes.debugQuads()
    //?} else {
    /*private fun quadsType() = net.minecraft.client.renderer.rendertype.RenderType.debugQuads()
    *///?}

    private fun beginFrame(cull: Frustum?): Boolean {
        if (!active()) return false
        if (!HitboxCategory.anythingVisible()) return false
        val mc = Minecraft.getInstance()
        val level = mc.level ?: return false
        val player = mc.player ?: return false
        val camera = mc.entityRenderDispatcher.camera ?: return false
        readCamera(camera)
        partialTicks = partialTick()
        updateRibbonScale(effectiveFov(camera), mc.window.height)
        val frameKey = level.gameTime.toDouble() + partialTicks
        if (frameKey != lastFrameKey) {
            lastFrameKey = frameKey
            HitboxCategory.resolveColors()
        }
        cullFrustum = cull
        hovered = mc.crosshairPickEntity
        viewer = player
        selfInFirstPerson = if (mc.options.cameraType.isFirstPerson) mc.cameraEntity else null
        vanillaToggle = vanillaHitboxesEnabled()
        return true
    }

    // Thickness is a multiple of vanilla's line width, so one unit is 2.5 physical framebuffer
    // pixels of on-screen width. mc.window.height is the framebuffer height, so GUI Scale does not
    // enter into it.
    private fun updateRibbonScale(fov: Float, viewportHeight: Int) {
        if (fov == lastFov && viewportHeight == lastViewportHeight) return
        lastFov = fov
        lastViewportHeight = viewportHeight
        val focal = 1.0 / tan(Math.toRadians(fov.toDouble()) * 0.5)
        val halfViewport = viewportHeight * 0.5
        ribbonScale = if (focal > 0.0 && halfViewport > 0.0) {
            0.5 * HitboxConfig.VANILLA_WIDTH / (focal * halfViewport)
        } else {
            0.0
        }
    }

    private fun drawLevel(vc: VertexConsumer) {
        val level = Minecraft.getInstance().level ?: return
        val player = viewer ?: return
        for (entity in level.entitiesForRendering()) {
            if (entity === selfInFirstPerson || entity.isInvisible) continue
            val matched = HitboxCategory.match(entity)
            val config = HitboxCategory.visualsOf(matched)
            if (!config.showSide && !config.showOutline && !config.showEyeHeight && !config.showViewRay) continue
            when (HitboxCategory.logicOf(matched).showCondition) {
                0 -> {}
                1 -> if (!vanillaToggle) continue
                2 -> if (entity !== hovered) continue
                else -> continue
            }
            // Cull before isInvisibleTo: culling is pure math on the bounding box, while
            // isInvisibleTo walks scoreboard teams.
            if (culled(entity, config)) continue
            if (entity.isInvisibleTo(player)) continue
            drawEntity(vc, entity, config)
        }
    }

    private fun culled(entity: Entity, config: HitboxConfig): Boolean {
        val bb = entity.boundingBox
        if (bb.hasNaN()) return true
        val ex = (bb.minX + bb.maxX) * 0.5 - camX
        val ey = (bb.minY + bb.maxY) * 0.5 - camY
        val ez = (bb.minZ + bb.maxZ) * 0.5 - camZ
        if (!entity.shouldRenderAtSqrDistance(ex * ex + ey * ey + ez * ez)) return true
        val reach = CULL_MARGIN + if (config.showViewRay) VIEW_RAY_LENGTH else 0.0
        val rx = (bb.maxX - bb.minX) * 0.5
        val ry = (bb.maxY - bb.minY) * 0.5
        val rz = (bb.maxZ - bb.minZ) * 0.5
        val margin = sqrt(rx * rx + ry * ry + rz * rz) + reach
        if (ex * fwdX + ey * fwdY + ez * fwdZ < -margin) return true
        val frustum = cullFrustum
        return frustum != null && !frustum.isVisible(bb.inflate(reach))
    }

    private fun inIframes(entity: Entity): Boolean = entity is LivingEntity && entity.hurtTime > 0

    private fun pick(iframe: Boolean, hover: Boolean, iframeArgb: Int, hoverArgb: Int, baseArgb: Int): Int =
        if (iframe) iframeArgb else if (hover) hoverArgb else baseArgb

    private fun drawEntity(vc: VertexConsumer, entity: Entity, config: HitboxConfig) {
        val delta = partialTicks.toDouble()
        val px = Mth.lerp(delta, entity.xo, entity.x)
        val py = Mth.lerp(delta, entity.yo, entity.y)
        val pz = Mth.lerp(delta, entity.zo, entity.z)
        val bb = entity.boundingBox
        val dx = px - entity.x - camX
        val dy = py - entity.y - camY
        val dz = pz - entity.z - camZ
        val minX = bb.minX + dx
        val minY = bb.minY + dy
        val minZ = bb.minZ + dz
        val maxX = bb.maxX + dx
        val maxY = bb.maxY + dy
        val maxZ = bb.maxZ + dz

        val hover = entity === hovered && config.hoverColor
        val iframe = config.iframeColor && inIframes(entity)

        if (config.showSide) {
            val c = pick(iframe, hover, config.sideIframeArgb, config.sideHoverArgb, config.sideArgb)
            fillBox(vc, minX, minY, minZ, maxX, maxY, maxZ, c)
        }
        if (config.showOutline) {
            val c = pick(iframe, hover, config.outlineIframeArgb, config.outlineHoverArgb, config.outlineArgb)
            styledBox(vc, config, minX, minY, minZ, maxX, maxY, maxZ, c, config.outlineThickness)
        }
        if (config.showEyeHeight) {
            val c = pick(iframe, hover, config.eyeHeightIframeArgb, config.eyeHeightHoverArgb, config.eyeHeightArgb)
            val eyeY = minY + entity.eyeHeight
            styledBox(vc, config, minX, eyeY - 0.01, minZ, maxX, eyeY + 0.01, maxZ, c, config.eyeHeightThickness)
        }
        if (config.showViewRay) {
            val c = pick(iframe, hover, config.viewRayIframeArgb, config.viewRayHoverArgb, config.viewRayArgb)
            val eyeY = minY + entity.eyeHeight
            val view = entity.getViewVector(partialTicks)
            val ax = px - camX
            val az = pz - camZ
            styledEdge(
                vc, config,
                ax, eyeY, az,
                ax + view.x * VIEW_RAY_LENGTH, eyeY + view.y * VIEW_RAY_LENGTH, az + view.z * VIEW_RAY_LENGTH,
                c, config.viewRayThickness,
            )
        }
    }

    private fun styledBox(
        vc: VertexConsumer, config: HitboxConfig,
        minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double,
        argb: Int, thickness: Float,
    ) {
        styledEdge(vc, config, minX, minY, minZ, maxX, minY, minZ, argb, thickness)
        styledEdge(vc, config, maxX, minY, minZ, maxX, minY, maxZ, argb, thickness)
        styledEdge(vc, config, maxX, minY, maxZ, minX, minY, maxZ, argb, thickness)
        styledEdge(vc, config, minX, minY, maxZ, minX, minY, minZ, argb, thickness)
        styledEdge(vc, config, minX, maxY, minZ, maxX, maxY, minZ, argb, thickness)
        styledEdge(vc, config, maxX, maxY, minZ, maxX, maxY, maxZ, argb, thickness)
        styledEdge(vc, config, maxX, maxY, maxZ, minX, maxY, maxZ, argb, thickness)
        styledEdge(vc, config, minX, maxY, maxZ, minX, maxY, minZ, argb, thickness)
        styledEdge(vc, config, minX, minY, minZ, minX, maxY, minZ, argb, thickness)
        styledEdge(vc, config, maxX, minY, minZ, maxX, maxY, minZ, argb, thickness)
        styledEdge(vc, config, maxX, minY, maxZ, maxX, maxY, maxZ, argb, thickness)
        styledEdge(vc, config, minX, minY, maxZ, minX, maxY, maxZ, argb, thickness)
    }

    private fun styledEdge(
        vc: VertexConsumer, config: HitboxConfig,
        ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double,
        argb: Int, thickness: Float,
    ) {
        when (config.lineMode) {
            HitboxConfig.DASHED -> {
                val dx = bx - ax
                val dy = by - ay
                val dz = bz - az
                val total = sqrt(dx * dx + dy * dy + dz * dz)
                if (total < 1.0e-6) return
                val dashLength = max(config.dashFactor * DASH_STEP, MIN_DASH)
                val ux = dx / total
                val uy = dy / total
                val uz = dz / total
                var start = 0.0
                while (start < total) {
                    val end = min(start + dashLength, total)
                    ribbon(
                        vc,
                        ax + ux * start, ay + uy * start, az + uz * start,
                        ax + ux * end, ay + uy * end, az + uz * end,
                        thickness, argb,
                    )
                    start += dashLength * 2.0
                }
            }

            else -> ribbon(vc, ax, ay, az, bx, by, bz, thickness, argb)
        }
    }

    private fun ribbon(
        vc: VertexConsumer,
        startX: Double, startY: Double, startZ: Double, endX: Double, endY: Double, endZ: Double,
        thickness: Float, argb: Int,
    ) {
        val startDepth = startX * fwdX + startY * fwdY + startZ * fwdZ
        val endDepth = endX * fwdX + endY * fwdY + endZ * fwdZ
        if (startDepth < NEAR_PLANE && endDepth < NEAR_PLANE) return
        var ax = startX
        var ay = startY
        var az = startZ
        var bx = endX
        var by = endY
        var bz = endZ
        if (startDepth < NEAR_PLANE) {
            val t = (NEAR_PLANE - startDepth) / (endDepth - startDepth)
            ax = startX + (endX - startX) * t
            ay = startY + (endY - startY) * t
            az = startZ + (endZ - startZ) * t
        } else if (endDepth < NEAR_PLANE) {
            val t = (NEAR_PLANE - endDepth) / (startDepth - endDepth)
            bx = endX + (startX - endX) * t
            by = endY + (startY - endY) * t
            bz = endZ + (startZ - endZ) * t
        }

        val dx = bx - ax
        val dy = by - ay
        val dz = bz - az
        if (dx * dx + dy * dy + dz * dz < EPSILON) return

        billboardOffset(dx, dy, dz, ax, ay, az, thickness)
        val oax = offX
        val oay = offY
        val oaz = offZ
        billboardOffset(dx, dy, dz, bx, by, bz, thickness)
        quad(
            vc,
            ax + oax, ay + oay, az + oaz,
            bx + offX, by + offY, bz + offZ,
            bx - offX, by - offY, bz - offZ,
            ax - oax, ay - oay, az - oaz,
            argb,
        )
    }

    private fun billboardOffset(
        dx: Double, dy: Double, dz: Double,
        vx: Double, vy: Double, vz: Double,
        thickness: Float,
    ) {
        // Offsetting perpendicular to the view axis, by a magnitude taken from the depth the
        // perspective divide will use, keeps the on-screen width constant across the whole frame.
        var cx = dy * fwdZ - dz * fwdY
        var cy = dz * fwdX - dx * fwdZ
        var cz = dx * fwdY - dy * fwdX
        var lengthSq = cx * cx + cy * cy + cz * cz
        if (lengthSq < EPSILON) {
            // The edge points down the view axis; fall back to a radial perpendicular.
            cx = dy * vz - dz * vy
            cy = dz * vx - dx * vz
            cz = dx * vy - dy * vx
            lengthSq = cx * cx + cy * cy + cz * cz
            if (lengthSq < EPSILON) {
                offX = 0.0
                offY = 0.0
                offZ = 0.0
                return
            }
        }
        val depth = max(vx * fwdX + vy * fwdY + vz * fwdZ, NEAR_PLANE)
        val scale = thickness * ribbonScale * depth / sqrt(lengthSq)
        offX = cx * scale
        offY = cy * scale
        offZ = cz * scale
    }

    private fun fillBox(
        vc: VertexConsumer,
        minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double,
        argb: Int,
    ) {
        quad(vc, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, argb)
        quad(vc, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, argb)
        quad(vc, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, argb)
        quad(vc, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, argb)
        quad(vc, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, argb)
        quad(vc, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, argb)
    }

    private fun quad(
        vc: VertexConsumer,
        x1: Double, y1: Double, z1: Double,
        x2: Double, y2: Double, z2: Double,
        x3: Double, y3: Double, z3: Double,
        x4: Double, y4: Double, z4: Double,
        argb: Int,
    ) {
        vc.addVertex(x1.toFloat(), y1.toFloat(), z1.toFloat()).setColor(argb)
        vc.addVertex(x2.toFloat(), y2.toFloat(), z2.toFloat()).setColor(argb)
        vc.addVertex(x3.toFloat(), y3.toFloat(), z3.toFloat()).setColor(argb)
        vc.addVertex(x4.toFloat(), y4.toFloat(), z4.toFloat()).setColor(argb)
    }

    //? if >=26.2 {
    private val identityPose = PoseStack()

    private val geometry = net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer { _, buffer -> drawLevel(buffer) }

    fun submitHitboxes(camera: net.minecraft.client.renderer.state.level.CameraRenderState, collector: net.minecraft.client.renderer.SubmitNodeCollector) {
        if (!beginFrame(camera.cullFrustum)) return
        collector.submitCustomGeometry(identityPose, quadsType(), geometry)
    }
    //?} elif >=1.21.10 {
    /*
    fun renderHitboxes(cull: Frustum?) {
        if (!beginFrame(cull)) return
        val type = quadsType()
        val buffer = Minecraft.getInstance().renderBuffers().bufferSource()
        drawLevel(buffer.getBuffer(type))
        buffer.endBatch(type)
    }
    *///?} else {
    /*
    fun renderEntity(entity: Entity, buffer: net.minecraft.client.renderer.MultiBufferSource) {
        if (!beginFrame(null)) return
        val player = viewer ?: return
        if (entity === selfInFirstPerson || entity.isInvisible) return
        val matched = HitboxCategory.match(entity)
        val config = HitboxCategory.visualsOf(matched)
        if (!config.showSide && !config.showOutline && !config.showEyeHeight && !config.showViewRay) return
        when (HitboxCategory.logicOf(matched).showCondition) {
            0 -> {}
            1 -> if (!vanillaToggle) return
            2 -> if (entity !== hovered) return
            else -> return
        }
        if (entity.isInvisibleTo(player)) return
        drawEntity(buffer.getBuffer(quadsType()), entity, config)
    }
    *///?}
}
