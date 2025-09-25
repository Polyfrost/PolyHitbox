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
import dev.deftu.omnicore.api.client.render.vertex.OmniVertexConsumer
import dev.deftu.omnicore.api.color.OmniColor
import dev.deftu.omnicore.api.data.aabb.OmniAABB
import dev.deftu.omnicore.api.data.shape.OmniVoxelShape
import dev.deftu.omnicore.api.data.shape.OmniVoxelShapes
import dev.deftu.omnicore.api.data.vec.OmniVec3d
import dev.deftu.omnicore.api.identifierOrThrow
import dev.deftu.omnicore.api.math.OmniVector3f
import kotlin.math.abs

//#if MC <=1.16.5
//$$ import org.lwjgl.opengl.GL11
//#endif

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
    identifierOrThrow(PolyHitbox.MODID, "pipeline/box"),
    DefaultVertexFormats.POSITION_COLOR,
    DrawMode.QUADS
).applySnippet(HITBOX_SNIPPET).build()

val OUTLINE_BOX_PIPELINE: OmniRenderPipeline = OmniRenderPipelines.builderWithDefaultShader(
    identifierOrThrow(PolyHitbox.MODID, "pipeline/outline_box"),
    DefaultVertexFormats.POSITION_COLOR_NORMAL,
    DrawMode.LINES
).applySnippet(HITBOX_SNIPPET).build()

val VIEW_RAY_PIPELINE: OmniRenderPipeline = OmniRenderPipelines.builderWithDefaultShader(
    identifierOrThrow(PolyHitbox.MODID, "pipeline/view_ray"),
    DefaultVertexFormats.POSITION_COLOR_NORMAL,
    DrawMode.LINES
).applySnippet(HITBOX_SNIPPET).build()

fun renderHitbox(stack: OmniMatrixStack, hitbox: HitboxState) {
    val info = PolyHitbox.getHitboxInfo()
    when (info.showMode) {
        0 -> info.isTargeted = hitbox.isTargeted
        1 -> if (!hitbox.isTargeted) return else info.isTargeted = true
        2 -> return
    }

    // TODO/FIX
    // if (info.useDistanceBasedWidth) {
    //     info.sqrDistance = (hitbox.offsetX * hitbox.offsetX + hitbox.offsetY * hitbox.offsetY + hitbox.offsetZ * hitbox.offsetZ).toFloat()
    // }

    var entityBoundingBox = hitbox.boundingBox.offset(
        -hitbox.entityX + hitbox.offsetX,
        -hitbox.entityY + hitbox.offsetY,
        -hitbox.entityZ + hitbox.offsetZ
    )
    if (info.isAccurate) {
        val offset = hitbox.collisionSize
        entityBoundingBox = entityBoundingBox.offset(offset, offset, offset)
    }

    //#if MC <=1.16.5
    //$$ GL11.glLineStipple(info.dashFactor, 0xAAAA.toShort())
    //#endif

    // Outline
    val outline = info.outline
    if (outline.isShown) {
        //#if MC <=1.16.5
        //$$ if (outline.isDashed) {
        //$$     GL11.glEnable(GL11.GL_LINE_STIPPLE)
        //$$ }
        //#endif
        renderOutlineBox(OUTLINE_BOX_PIPELINE, stack, entityBoundingBox, outline.getColor()) {
            setLineWidth(outline.width)
        }
        //#if MC <=1.16.5
        //$$ if (outline.isDashed) {
        //$$     GL11.glDisable(GL11.GL_LINE_STIPPLE)
        //$$ }
        //#endif
    }

    // Sides
    val sides = info.sides
    if (sides.isShown) {
        OmniShapeRenderer.renderBox(BOX_PIPELINE, stack, entityBoundingBox, 0.0, 0.0, 0.0, sides.getColor())
    }

    // Eye Line
    if (hitbox.isLiving) {
        val halfWidth = hitbox.width / 2.0F
        var boundingBox = OmniAABB(
            hitbox.offsetX - halfWidth,
            hitbox.offsetY + hitbox.eyeHeight - 0.01,
            hitbox.offsetZ - halfWidth,
            hitbox.offsetX + halfWidth,
            hitbox.offsetY + hitbox.eyeHeight + 0.01,
            hitbox.offsetZ + halfWidth
        )

        val eyeline = info.eyeline
        if (eyeline.isShown) {
            if (info.isAccurate) {
                val offset = hitbox.collisionSize
                boundingBox = boundingBox.offset(offset, 0.0, offset)
            }

            //#if MC <=1.16.5
            //$$ if (eyeline.isDashed) {
            //$$     GL11.glEnable(GL11.GL_LINE_STIPPLE)
            //$$ }
            //#endif
            renderOutlineBox(OUTLINE_BOX_PIPELINE, stack, boundingBox, eyeline.getColor()) {
                setLineWidth(eyeline.width)
            }
            //#if MC <=1.16.5
            //$$ if (eyeline.isDashed) {
            //$$     GL11.glDisable(GL11.GL_LINE_STIPPLE)
            //$$ }
            //#endif
        }
    }

    // View Ray
    val viewRay = info.viewRay
    if (viewRay.isShown) {
        //#if MC <=1.16.5
        //$$ if (viewRay.isDashed) {
        //$$     GL11.glEnable(GL11.GL_LINE_STIPPLE)
        //$$ }
        //#endif
        renderViewRay(
            VIEW_RAY_PIPELINE,
            stack,
            hitbox.offsetX, hitbox.offsetY, hitbox.offsetZ,
            hitbox.lookVecX, hitbox.lookVecY, hitbox.lookVecZ,
            hitbox.eyeHeight,
            viewRay.getColor()
        ) {
            setLineWidth(viewRay.width)
        }
        //#if MC <=1.16.5
        //$$ if (viewRay.isDashed) {
        //$$     GL11.glDisable(GL11.GL_LINE_STIPPLE)
        //$$ }
        //#endif
    }
}

private fun renderViewRay(
    pipeline: OmniRenderPipeline,
    stack: OmniMatrixStack,
    offsetX: Double, offsetY: Double, offsetZ: Double,
    lookVecX: Double, lookVecY: Double, lookVecZ: Double,
    eyeHeight: Float,
    color: OmniColor,
    builder: RenderPassEncoder.() -> Unit = {},
) {
    val dir = OmniVector3f(lookVecX.toFloat(), lookVecY.toFloat(), lookVecZ.toFloat()).normalized()
    val up = if (abs(dir.y) > 0.99) OmniVector3f.UNIT_X else OmniVector3f.UNIT_Y
    val normal = dir.cross(up).normalized()
    pipeline.createBufferBuilder().run {
        vertex(stack, offsetX, offsetY + eyeHeight, offsetZ)
            .color(color)
            .normal(stack, normal.x, normal.y, normal.z)
            .next()
        vertex(stack, offsetX + lookVecX * 2.0, offsetY + eyeHeight + lookVecY * 2.0, offsetZ + lookVecZ * 2.0)
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
    renderOutlineExt(
        stack,
        buffer,
        OmniVoxelShapes.cuboid(boundingBox).simplify(),
        0.0, 0.0, 0.0,
        color
    )
    buffer.buildOrNull()?.drawAndClose(pipeline, builder)
}

fun renderOutlineExt(
    matrices: OmniMatrixStack,
    vertexConsumer: OmniVertexConsumer,
    shape: OmniVoxelShape,
    x: Double,
    y: Double,
    z: Double,
    color: OmniColor,
) {
    shape.forEachEdge { box ->
        val startX = box.minX;
        val startY = box.minY;
        val startZ = box.minZ
        val endX = box.maxX;
        val endY = box.maxY;
        val endZ = box.maxZ

        val normal = OmniVector3f(
            (endX - startX).toFloat(),
            (endY - startY).toFloat(),
            (endZ - startZ).toFloat()
        ).normalized()

        vertexConsumer
            .vertex(matrices, startX + x, startY + y, startZ + z)
            .color(color)
            .normal(matrices, normal.x, normal.y, normal.z)
            .next()
        vertexConsumer
            .vertex(matrices, endX + x, endY + y, endZ + z)
            .color(color)
            .normal(matrices, normal.x, normal.y, normal.z)
            .next()
    }
}