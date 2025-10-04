package org.polyfrost.polyhitbox

import dev.deftu.omnicore.api.client.render.DefaultVertexFormats
import dev.deftu.omnicore.api.client.render.DrawMode
import dev.deftu.omnicore.api.client.render.OmniShapeRenderer
import dev.deftu.omnicore.api.client.render.OmniTextureUnit
import dev.deftu.omnicore.api.client.render.pipeline.OmniRenderPipeline
import dev.deftu.omnicore.api.client.render.pipeline.OmniRenderPipelineSnippets
import dev.deftu.omnicore.api.client.render.pipeline.OmniRenderPipelines
import dev.deftu.omnicore.api.client.render.pipeline.RenderPassEncoder
import dev.deftu.omnicore.api.client.render.stack.OmniMatrixStack
import dev.deftu.omnicore.api.client.render.state.OmniBlendState
import dev.deftu.omnicore.api.color.OmniColor
import dev.deftu.omnicore.api.data.aabb.OmniAABB
import dev.deftu.omnicore.api.data.shape.OmniVoxelShapes
import dev.deftu.omnicore.api.data.vec.OmniVec3d
import dev.deftu.omnicore.api.identifierOrThrow
import dev.deftu.omnicore.api.math.OmniVector3f
import kotlin.math.abs

const val STIPPLE_PATTERN: Short = 0xAAAA.toShort()

val HITBOX_SNIPPET: OmniRenderPipeline.Snippet = OmniRenderPipelineSnippets.builder().run {
    setDepthTest(OmniRenderPipeline.DepthTest.LESS_OR_EQUAL)
    setBlendState(OmniBlendState.NORMAL)
    setCulling(false)
    configureLegacyEffects {
        lighting = false
        OmniTextureUnit.TEXTURE0 equals false
    }
    build()
}

val BOX_PIPELINE: OmniRenderPipeline = OmniRenderPipelines.builderWithDefaultShader(
    identifierOrThrow(PolyHitboxConstants.ID, "pipeline/box"),
    DefaultVertexFormats.POSITION_COLOR,
    DrawMode.QUADS
).applySnippet(HITBOX_SNIPPET).build()

val OUTLINE_BOX_PIPELINE: OmniRenderPipeline = OmniRenderPipelines.builderWithDefaultShader(
    identifierOrThrow(PolyHitboxConstants.ID, "pipeline/outline_box"),
    DefaultVertexFormats.POSITION_COLOR_NORMAL,
    DrawMode.LINES
).applySnippet(HITBOX_SNIPPET).build()

val OUTLINE_STIPPLE_BOX_PIPELINE: OmniRenderPipeline = OUTLINE_BOX_PIPELINE.newBuilder().configureLegacyEffects {
    lineStipple = true
}.build()

val VIEW_RAY_PIPELINE: OmniRenderPipeline = OmniRenderPipelines.builderWithDefaultShader(
    identifierOrThrow(PolyHitboxConstants.ID, "pipeline/view_ray"),
    DefaultVertexFormats.POSITION_COLOR_NORMAL,
    DrawMode.LINES
).applySnippet(HITBOX_SNIPPET).build()

val VIEW_RAY_STIPPLE_PIPELINE: OmniRenderPipeline = VIEW_RAY_PIPELINE.newBuilder().configureLegacyEffects {
    lineStipple = true
}.build()

fun renderHitbox(
    stack: OmniMatrixStack,
    offset: OmniVec3d,
    entityPos: OmniVec3d,
    lookVec: OmniVec3d,
    eyeHeight: Float,
    isLiving: Boolean,
    isTargeted: Boolean,
    width: Double,
    collisionSize: Double,
    boundingBox: OmniAABB,
) {
    val info = PolyHitbox.getHitboxInfo()
    when (info.showMode) {
        0 -> info.isTargeted = isTargeted
        1 -> if (!isTargeted) return else info.isTargeted = true
        2 -> return
    }

    if (info.useDistanceBasedWidth) {
        info.sqrDistance = (offset.x * offset.x + offset.y * offset.y + offset.z * offset.z).toFloat()
    }

    var entityBoundingBox =
        boundingBox.offset(-entityPos.x + offset.x, -entityPos.y + offset.y, -entityPos.z + offset.z)
    if (info.isAccurate) {
        entityBoundingBox = entityBoundingBox.offset(collisionSize)
    }

    // Outline
    val outline = info.outline
    if (outline.isShown) {
        val pipeline = if (outline.isDashed) OUTLINE_STIPPLE_BOX_PIPELINE else OUTLINE_BOX_PIPELINE
        renderOutlineBox(pipeline, stack, entityBoundingBox, outline.getColor()) {
            setLineWidth(outline.width)
            if (outline.isDashed) {
                setLineStipple(info.dashFactor, STIPPLE_PATTERN)
            }
        }
    }

    // Sides
    val sides = info.sides
    if (sides.isShown) {
        OmniShapeRenderer.FILLED_BOX.render(BOX_PIPELINE, stack, entityBoundingBox, sides.getColor())
    }

    // Eye Line
    if (isLiving) {
        val halfWidth = width / 2.0F
        var boundingBox = OmniAABB(
            offset.x - halfWidth,
            offset.y + eyeHeight - 0.01,
            offset.z - halfWidth,
            offset.x + halfWidth,
            offset.y + eyeHeight + 0.01,
            offset.z + halfWidth
        )

        val eyeline = info.eyeline
        if (eyeline.isShown) {
            if (info.isAccurate) {
                boundingBox = boundingBox.offset(collisionSize, 0.0, collisionSize)
            }

            val pipeline = if (eyeline.isDashed) OUTLINE_STIPPLE_BOX_PIPELINE else OUTLINE_BOX_PIPELINE
            renderOutlineBox(pipeline, stack, boundingBox, eyeline.getColor()) {
                setLineWidth(eyeline.width)
                if (eyeline.isDashed) {
                    setLineStipple(info.dashFactor, STIPPLE_PATTERN)
                }
            }
        }
    }

    // View Ray
    val viewRay = info.viewRay
    if (viewRay.isShown) {
        val pipeline = if (viewRay.isDashed) VIEW_RAY_STIPPLE_PIPELINE else VIEW_RAY_PIPELINE
        renderViewRay(pipeline, stack, offset, lookVec, eyeHeight, viewRay.getColor()) {
            setLineWidth(viewRay.width)
            if (viewRay.isDashed) {
                setLineStipple(info.dashFactor, STIPPLE_PATTERN)
            }
        }
    }
}

private fun renderViewRay(
    pipeline: OmniRenderPipeline,
    stack: OmniMatrixStack,
    offset: OmniVec3d,
    lookVec: OmniVec3d,
    eyeHeight: Float,
    color: OmniColor,
    builder: RenderPassEncoder.() -> Unit = {},
) {
    val dir = OmniVector3f(lookVec.x.toFloat(), lookVec.y.toFloat(), lookVec.z.toFloat()).normalized()
    val up = if (abs(dir.y) > 0.99) OmniVector3f.UNIT_X else OmniVector3f.UNIT_Y
    val normal = dir.cross(up).normalized()
    pipeline.createBufferBuilder().run {
        vertex(stack, offset.x, offset.y + eyeHeight, offset.z)
            .color(color)
            .normal(stack, normal.x, normal.y, normal.z)
            .next()
        vertex(stack, offset.x + lookVec.x * 2.0, offset.y + eyeHeight + lookVec.y * 2.0, offset.z + lookVec.z * 2.0)
            .color(color)
            .normal(stack, normal.x, normal.y, normal.z)
            .next()
        buildOrNull()?.drawAndClose(pipeline, builder)
    }
}

private fun renderOutlineBox(
    pipeline: OmniRenderPipeline,
    stack: OmniMatrixStack,
    boundingBox: OmniAABB,
    color: OmniColor,
    builder: RenderPassEncoder.() -> Unit = {},
) {
    val buffer = pipeline.createBufferBuilder()
    OmniShapeRenderer.SHAPE_OUTLINE.render(buffer, stack, OmniVoxelShapes.cuboid(boundingBox).simplify(), color)
    buffer.buildOrNull()?.drawAndClose(pipeline, builder)
}