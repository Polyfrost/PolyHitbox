package org.polyfrost.polyhitbox.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.polyfrost.polyhitbox.HitboxInfo;
import org.polyfrost.polyhitbox.PolyHitbox;
import org.polyfrost.polyui.color.PolyColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL11.*;

@Mixin(RenderManager.class)
public abstract class RenderManagerMixin {

    @Inject(method = "renderDebugBoundingBox", at = @At("HEAD"), cancellable = true)
    public void checkAndSetup(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo ci, @Share("info") LocalRef<HitboxInfo> infoRef) {
        HitboxInfo info = PolyHitbox.INSTANCE.getHitboxInfo(entity);
        switch (info.getShowMode()) {
            case 2: // NEVER
                ci.cancel();
                return;
            case 1: // ONLY TARGETED
                if (entity != Minecraft.getMinecraft().pointedEntity) {
                    ci.cancel();
                    return;
                } else info.setTargeted(true);
                break;
            case 0: // ALWAYS
                info.setTargeted(entity == Minecraft.getMinecraft().pointedEntity);
                break;
        }
        if (info.getUseDistanceBasedWidth()) info.setSqrDistance((float) (x * x + y * y + z * z));
        glLineStipple(info.getDashFactor(), (short) 0b1010101010101010);
        infoRef.set(info);
    }

    @Inject(method = "renderDebugBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;getInstance()Lnet/minecraft/client/renderer/Tessellator;"), cancellable = true)
    private void renderViewRayCheck(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo ci, @Share("info") LocalRef<HitboxInfo> infoRef) {
        if (!infoRef.get().getViewRay().isShown()) {
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            restoreState(entity, x, y, z, yaw, partialTicks, ci);
            ci.cancel();
        }
    }

    @Redirect(method = "renderDebugBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;drawOutlinedBoundingBox(Lnet/minecraft/util/AxisAlignedBB;IIII)V", ordinal = 0))
    private void renderHitboxModifyColorAndSides(AxisAlignedBB bb, int r, int g, int b, int a, Entity entity, @Share("info") LocalRef<HitboxInfo> infoRef) {
        HitboxInfo info = infoRef.get();
        if (info.isAccurate()) {
            double offset = entity.getCollisionBorderSize();
            bb = bb.expand(offset, offset, offset);
        }
        HitboxInfo.ElementInfo outline = info.getOutline();
        if (outline.isShown()) {
            PolyColor color = outline.getColor();
            if (outline.isDashed()) glEnable(GL_LINE_STIPPLE);
            glLineWidth(outline.getWidth());
            RenderGlobal.drawOutlinedBoundingBox(bb, color.red(), color.green(), color.blue(), color.alpha());
            glDisable(GL_LINE_STIPPLE);
        }
        HitboxInfo.ElementInfo sides = info.getSides();
        if (sides.isShown()) {
            PolyColor color = sides.getColor();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer wr = tessellator.getWorldRenderer();
            GlStateManager.color(color.red() / 255f, color.green() / 255f, color.blue() / 255f, color.getAlpha());
            GlStateManager.enableBlend();
            wr.begin(GL_QUADS, DefaultVertexFormats.POSITION);
            // back
            wr.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
            wr.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
            wr.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
            wr.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
            // front
            wr.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
            wr.pos(bb.minX, bb.minY, bb.minZ).endVertex();
            wr.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
            // left
            wr.pos(bb.minX, bb.minY, bb.minZ).endVertex();
            wr.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
            wr.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
            wr.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
            // right
            wr.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
            wr.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
            // top
            wr.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
            wr.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
            wr.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
            wr.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
            // bottom (+0.01 to prevent z-fighting)
            wr.pos(bb.minX, bb.minY + 0.01, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.minY + 0.01, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.minY + 0.01, bb.maxZ).endVertex();
            wr.pos(bb.minX, bb.minY + 0.01, bb.maxZ).endVertex();
            tessellator.draw();
            GlStateManager.color(1f, 1f, 1f, 1f);
            GlStateManager.disableBlend();
        }
    }

    @Redirect(method = "renderDebugBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;drawOutlinedBoundingBox(Lnet/minecraft/util/AxisAlignedBB;IIII)V", ordinal = 1))
    private void renderEyelineModifyColor(AxisAlignedBB boundingBox, int red, int green, int blue, int alpha, Entity entity, @Share("info") LocalRef<HitboxInfo> infoRef) {
        HitboxInfo info = infoRef.get();
        HitboxInfo.ElementInfo eyeline = info.getEyeline();
        if (!eyeline.isShown()) return;
        if (info.isAccurate()) {
            double offset = entity.getCollisionBorderSize();
            boundingBox = boundingBox.expand(offset, 0.0, offset);
        }
        PolyColor color = eyeline.getColor();
        if (eyeline.isDashed()) glEnable(GL_LINE_STIPPLE);
        glLineWidth(eyeline.getWidth());
        RenderGlobal.drawOutlinedBoundingBox(boundingBox, color.red(), color.green(), color.blue(), color.alpha());
        glDisable(GL_LINE_STIPPLE);
    }

    @Inject(method = "renderDebugBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;begin(ILnet/minecraft/client/renderer/vertex/VertexFormat;)V"))
    private void renderViewRayModifyWidth(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo ci, @Share("info") LocalRef<HitboxInfo> infoRef) {
        HitboxInfo.ElementInfo viewRay = infoRef.get().getViewRay();
        if (viewRay.isDashed()) glEnable(GL_LINE_STIPPLE);
        glLineWidth(viewRay.getWidth());
    }

    @Redirect(method = "renderDebugBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;color(IIII)Lnet/minecraft/client/renderer/WorldRenderer;"))
    private WorldRenderer renderViewRayModifyColor(WorldRenderer worldRenderer, int red, int green, int blue, int alpha, Entity entity, @Share("info") LocalRef<HitboxInfo> infoRef) {
        PolyColor color = infoRef.get().getViewRay().getColor();
        worldRenderer.color(color.red(), color.green(), color.blue(), color.alpha());
        return worldRenderer;
    }

    @Inject(method = "renderDebugBoundingBox", at = @At(value = "RETURN"))
    private void restoreState(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo ci) {
        glLineWidth(1f);
        glDisable(GL_LINE_STIPPLE);
    }

}
