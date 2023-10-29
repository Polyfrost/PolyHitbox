package org.polyfrost.hitboxes.mixin;

import cc.polyfrost.oneconfig.config.core.OneColor;
import org.polyfrost.hitboxes.HitBoxes;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import org.polyfrost.hitboxes.config.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import static org.lwjgl.opengl.GL11.*;

@Mixin(RenderManager.class)
public class RenderManagerMixin {

    /**
     * Completely overrides the original hitbox rendering method.
     */
    @Inject(method = "doRenderEntity", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V",
            shift = At.Shift.AFTER
    ))
    public void overrideHitboxRendering(Entity entityIn, double x, double y, double z, float entityYaw, float partialTicks, boolean p_147939_10_, CallbackInfoReturnable<Boolean> cir) {

        HitBoxesConfig config = HitBoxes.INSTANCE.config;

        if (!config.enabled) return;

        HitboxConfiguration e = config.getEntityType(entityIn);

        if (!e.showHitbox || entityIn.isInvisible())
            return;

        OneColor outlineColor = e.outlineColor;
        OneColor eyeHeightColor = e.eyeHeightColor;
        OneColor lookVectorColor = e.lookVectorColor;

        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        float f = entityIn.width / 2.0f;
        AxisAlignedBB axisalignedbb = entityIn.getEntityBoundingBox();
        GL11.glEnable(GL_BLEND);

        if (e.showOutline) {
            GL11.glLineWidth(e.outlineThickness);

            if (e.dashedHitbox) {
                GL11.glPushAttrib(8192);
                GL11.glLineStipple(10, (short) 0xAAAA);
                GL11.glEnable(GL_LINE_STIPPLE);
                GL11.glBegin(GL_LINES);
                GL11.glEnd();
            }

            AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(axisalignedbb.minX - entityIn.posX + x, axisalignedbb.minY - entityIn.posY + y, axisalignedbb.minZ - entityIn.posZ + z, axisalignedbb.maxX - entityIn.posX + x, axisalignedbb.maxY - entityIn.posY + y, axisalignedbb.maxZ - entityIn.posZ + z);
            axisalignedbb1 = e.accurate ? axisalignedbb1.expand(entityIn.getCollisionBorderSize(), entityIn.getCollisionBorderSize(), entityIn.getCollisionBorderSize()) : axisalignedbb1;
            RenderGlobal.drawOutlinedBoundingBox(axisalignedbb1, outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), outlineColor.getAlpha());

            if (e.dashedHitbox) {
                GL11.glPopAttrib();
            }
        }

        GL11.glDisable(GL_BLEND);

        if (e.showEyeHeight) {
            GL11.glLineWidth(e.eyeHeightThickness);
            RenderGlobal.drawOutlinedBoundingBox(new AxisAlignedBB(x - (double)f, y + (double)entityIn.getEyeHeight() - (double)0.01f, z - (double)f, x + (double)f, y + (double)entityIn.getEyeHeight() + (double)0.01f, z + (double)f),
                    eyeHeightColor.getRed(), eyeHeightColor.getGreen(), eyeHeightColor.getBlue(), eyeHeightColor.getAlpha());
        }

        if (e.showLookVector) {
            GL11.glLineWidth(e.lookVectorThickness);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            Vec3 vec3 = entityIn.getLook(partialTicks);
            worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(x, y + (double)entityIn.getEyeHeight(), z).color(lookVectorColor.getRed(), lookVectorColor.getGreen(), lookVectorColor.getBlue(), lookVectorColor.getAlpha()).endVertex();
            worldrenderer.pos(x + vec3.xCoord * 2.0, y + (double)entityIn.getEyeHeight() + vec3.yCoord * 2.0, z + vec3.zCoord * 2.0).color(lookVectorColor.getRed(), lookVectorColor.getGreen(), lookVectorColor.getBlue(), lookVectorColor.getAlpha()).endVertex();
            tessellator.draw();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);

    }

    /**
     * Cancel the Vanilla rendering.
     */
    @Inject(method = "renderDebugBoundingBox", at = @At(value = "HEAD"), cancellable = true)
    public void cancelVanillaHitboxes(CallbackInfo ci) {
        // A true toggle of the mod, when it's disabled it can go back to the Vanilla behavior.
        if (HitBoxes.INSTANCE.config.enabled) {
            ci.cancel();
        }
    }

}
