package org.polyfrost.polyhitbox

import dev.deftu.omnicore.client.render.OmniMatrixStack
import dev.deftu.omnicore.client.render.OmniRenderState
import dev.deftu.omnicore.client.render.pipeline.DrawModes
import dev.deftu.omnicore.client.render.pipeline.OmniRenderPipeline
import dev.deftu.omnicore.client.render.pipeline.VertexFormats
import dev.deftu.omnicore.client.render.state.DepthFunction
import dev.deftu.omnicore.client.render.state.OmniManagedBlendState
import dev.deftu.omnicore.client.render.state.OmniManagedDepthState
import dev.deftu.omnicore.client.render.vertex.OmniBufferBuilder
import dev.deftu.omnicore.common.OmniBox
import dev.deftu.omnicore.common.OmniIdentifier
import dev.deftu.omnicore.common.offsetBy
import net.minecraft.util.math.Box
import org.polyfrost.polyui.color.PolyColor

//#if MC <=1.16.5
//$$ import org.lwjgl.opengl.GL11
//#endif

const val Z_FIGHTING_OFFSET = 0.01 // (to prevent z-fighting)

val BOX_PIPELINE: OmniRenderPipeline = OmniRenderPipeline.builderWithDefaultShader(
    OmniIdentifier.create(PolyHitbox.MODID, "box"),
    VertexFormats.POSITION_COLOR,
    DrawModes.QUADS
).run {
    depthState = OmniManagedDepthState.asEnabled(DepthFunction.LESS_OR_EQUAL)
    blendState = OmniManagedBlendState.NORMAL
    isCullFace = false
    build()
}

val OUTLINE_BOX_PIPELINE: OmniRenderPipeline = OmniRenderPipeline.builderWithDefaultShader(
    OmniIdentifier.create(PolyHitbox.MODID, "outline_box"),
    VertexFormats.POSITION_COLOR,
    DrawModes.LINES
).run {
    depthState = OmniManagedDepthState.asEnabled(DepthFunction.LESS_OR_EQUAL)
    blendState = OmniManagedBlendState.NORMAL
    isCullFace = false
    build()
}

val VIEW_RAY_PIPELINE: OmniRenderPipeline = OmniRenderPipeline.builderWithDefaultShader(
    OmniIdentifier.create(PolyHitbox.MODID, "view_ray"),
    VertexFormats.POSITION_COLOR,
    DrawModes.LINE_STRIP
).run {
    depthState = OmniManagedDepthState.asEnabled(DepthFunction.LESS_OR_EQUAL)
    blendState = OmniManagedBlendState.NORMAL
    isCullFace = false
    build()
}

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

    var entityBoundingBox = hitbox.boundingBox.offsetBy(-hitbox.entityX + hitbox.offsetX, -hitbox.entityY + hitbox.offsetY, -hitbox.entityZ + hitbox.offsetZ)
    if (info.isAccurate) {
        val offset = hitbox.collisionSize
        entityBoundingBox = entityBoundingBox.offsetBy(offset, offset, offset)
    }

    //#if MC <=1.16.5
    //$$ GL11.glLineStipple(info.dashFactor, 0xAAAA.toShort())
    //#endif
    OmniRenderState.disableTexture2D()
    OmniRenderState.disableLighting() // TODO: Figure out why mobs lighting is affected

    // Outline
    val outline = info.outline
    if (outline.isShown) {
        //#if MC <=1.16.5
        //$$ if (outline.isDashed) {
        //$$     GL11.glEnable(GL11.GL_LINE_STIPPLE)
        //$$ }
        //#endif
        setLineWidth(outline.width)
        renderOutlineBox(OUTLINE_BOX_PIPELINE, stack, entityBoundingBox, outline.getColor())
        //#if MC <=1.16.5
        //$$ if (outline.isDashed) {
        //$$     GL11.glDisable(GL11.GL_LINE_STIPPLE)
        //$$ }
        //#endif
    }

    // Sides
    val sides = info.sides
    if (sides.isShown) {
        renderBox(BOX_PIPELINE, stack, entityBoundingBox, sides.getColor())
    }

    // Eye Line
    if (hitbox.isLiving) {
        val halfWidth = hitbox.width / 2.0F
        var boundingBox = OmniBox.from(
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
                boundingBox = boundingBox.offsetBy(offset, 0.0, offset)
            }

            //#if MC <=1.16.5
            //$$ if (eyeline.isDashed) {
            //$$     GL11.glEnable(GL11.GL_LINE_STIPPLE)
            //$$ }
            //#endif
            setLineWidth(eyeline.width)
            renderOutlineBox(OUTLINE_BOX_PIPELINE, stack, boundingBox, eyeline.getColor())
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
        setLineWidth(viewRay.width)
        renderViewRay(
            VIEW_RAY_PIPELINE,
            stack,
            hitbox.offsetX, hitbox.offsetY, hitbox.offsetZ,
            hitbox.lookVecX, hitbox.lookVecY, hitbox.lookVecZ,
            hitbox.eyeHeight,
            viewRay.getColor()
        )
        //#if MC <=1.16.5
        //$$ if (viewRay.isDashed) {
        //$$     GL11.glDisable(GL11.GL_LINE_STIPPLE)
        //$$ }
        //#endif
    }

    setLineWidth(1.0F)
    OmniRenderState.enableLighting()
    OmniRenderState.enableTexture2D()
}

private fun renderViewRay(
    pipeline: OmniRenderPipeline,
    stack: OmniMatrixStack,
    offsetX: Double, offsetY: Double, offsetZ: Double,
    lookVecX: Double, lookVecY: Double, lookVecZ: Double,
    eyeHeight: Double,
    color: PolyColor,
) {
    //#if MC >=1.21.5
    val buffer = dev.deftu.omnicore.client.OmniClient.getInstance().bufferBuilders.entityVertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.LINES)
    val pose = stack.toVanillaStack().peek()
    buffer
        .vertex(pose, offsetX.toFloat(), (offsetY + eyeHeight).toFloat(), offsetZ.toFloat())
        .normal(pose, 0.0F, 0.0F, 0.0F)
        .color(color.argb)
    buffer
        .vertex(pose, (offsetX + lookVecX * 2.0).toFloat(), (offsetY + eyeHeight + lookVecY * 2.0).toFloat(), (offsetZ + lookVecZ * 2.0).toFloat())
        .normal(pose, 0.0F, 0.0F, 0.0F)
        .color(color.argb)
    dev.deftu.omnicore.client.OmniClient.getInstance().bufferBuilders.entityVertexConsumers.draw()
    //#else
    //$$ OmniBufferBuilder.create(DrawModes.LINES, VertexFormats.POSITION_COLOR).run {
    //$$     vertex(stack, offsetX, offsetY + eyeHeight, offsetZ)
    //$$         .color(color.argb)
    //$$         .next()
    //$$     vertex(stack, offsetX + lookVecX * 2.0, offsetY + eyeHeight + lookVecY * 2.0, offsetZ + lookVecZ * 2.0)
    //$$         .color(color.argb)
    //$$         .next()
    //$$     build()?.drawWithCleanup(pipeline) {}
    //$$ }
    //#endif
}

private fun renderBox(
    pipeline: OmniRenderPipeline,
    stack: OmniMatrixStack,
    boundingBox: Box,
    color: PolyColor,
) {
    OmniBufferBuilder.create(DrawModes.QUADS, VertexFormats.POSITION_COLOR).run {
        // back
        vertex(stack, boundingBox.minX, boundingBox.minY, boundingBox.maxZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
            .color(color.argb)
            .next()
        // front
        vertex(stack, boundingBox.maxX, boundingBox.minY, boundingBox.minZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.minX, boundingBox.minY, boundingBox.minZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
            .color(color.argb)
            .next()
        // left
        vertex(stack, boundingBox.minX, boundingBox.minY, boundingBox.minZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.minX, boundingBox.minY, boundingBox.maxZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
            .color(color.argb)
            .next()
        // right
        vertex(stack, boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.maxX, boundingBox.minY, boundingBox.minZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
            .color(color.argb)
            .next()
        // top
        vertex(stack, boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
            .color(color.argb)
            .next()
        // bottom
        vertex(stack, boundingBox.minX, boundingBox.minY + Z_FIGHTING_OFFSET, boundingBox.minZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.maxX, boundingBox.minY + Z_FIGHTING_OFFSET, boundingBox.minZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.maxX, boundingBox.minY + Z_FIGHTING_OFFSET, boundingBox.maxZ)
            .color(color.argb)
            .next()
        vertex(stack, boundingBox.minX, boundingBox.minY + Z_FIGHTING_OFFSET, boundingBox.maxZ)
            .color(color.argb)
            .next()
        build()?.drawWithCleanup(pipeline) {}
    }
}

private fun renderOutlineBox(
    pipeline: OmniRenderPipeline,
    stack: OmniMatrixStack,
    boundingBox: Box,
    color: PolyColor,
) {
    //#if MC >=1.16.5
    val buffer = dev.deftu.omnicore.client.OmniClient.getInstance().bufferBuilders.entityVertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.LINES)
    net.minecraft.client.render.VertexRendering.drawOutline(
        stack.toVanillaStack(),
        buffer,
        net.minecraft.util.shape.VoxelShapes.cuboid(boundingBox).simplify(),
        0.0, 0.0, 0.0,
        color.argb
    )
    dev.deftu.omnicore.client.OmniClient.getInstance().bufferBuilders.entityVertexConsumers.draw()
    //#else
    //$$ stack.runWithGlobalState {
    //$$     pipeline.bind()
    //$$     net.minecraft.client.renderer.RenderGlobal.drawOutlinedBoundingBox(boundingBox, color.r, color.g, color.b, color.a)
    //$$     pipeline.unbind()
    //$$ }
    //#endif
}

private fun setLineWidth(width: Float) {
    //#if MC >=1.16.5
    com.mojang.blaze3d.systems.RenderSystem.lineWidth(width)
    //#else
    //$$ GL11.glLineWidth(width)
    //#endif
}