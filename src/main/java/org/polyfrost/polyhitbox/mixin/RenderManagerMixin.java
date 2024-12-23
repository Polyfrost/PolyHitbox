package org.polyfrost.polyhitbox.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;
import org.polyfrost.polyhitbox.HitboxInfo;
import org.polyfrost.polyhitbox.PolyHitbox;
import org.polyfrost.polyui.color.PolyColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderManager.class)
public abstract class RenderManagerMixin {

    @Inject(method = "renderDebugBoundingBox", at = @At("HEAD"), cancellable = true)
    public void checkAndSetup(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo ci, @Share("info") LocalRef<HitboxInfo> infoRef) {
        HitboxInfo info = PolyHitbox.INSTANCE.getHitboxInfo(entity);
        switch (info.getShowMode()) {
            case NEVER:
                ci.cancel();
                return;
            case HOVERED:
                if (entity != Minecraft.getMinecraft().pointedEntity) {
                    ci.cancel();
                    return;
                }
                info.setTargetted(true);
                break;
            case ALWAYS:
                info.setTargetted(entity == Minecraft.getMinecraft().pointedEntity);
                break;
        }
        if (info.isDashed()) {
            GL11.glLineStipple(info.getDashFactor(), (short) 0b1010101010101010);
            GL11.glEnable(GL11.GL_LINE_STIPPLE);
        }
        infoRef.set(info);
    }

    @Inject(method = "renderDebugBoundingBox", at = @At("TAIL"))
    private void restoreState(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo ci) {
        GL11.glLineWidth(1f);
        GL11.glDisable(GL11.GL_LINE_STIPPLE);
    }

    @Inject(method = "renderDebugBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;getInstance()Lnet/minecraft/client/renderer/Tessellator;"), cancellable = true)
    private void renderViewRayCheck(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo ci, @Share("info") LocalRef<HitboxInfo> infoRef) {
        if (!infoRef.get().getShowViewRay()) {
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GL11.glLineWidth(1f);
            GL11.glDisable(GL11.GL_LINE_STIPPLE);
            ci.cancel();
        }
    }

    @Redirect(method = "renderDebugBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;drawOutlinedBoundingBox(Lnet/minecraft/util/AxisAlignedBB;IIII)V", ordinal = 0))
    private void renderHitboxModifyColor(AxisAlignedBB boundingBox, int red, int green, int blue, int alpha, Entity entity, @Share("info") LocalRef<HitboxInfo> infoRef) {
        HitboxInfo info = infoRef.get();
        if (!info.getShowOutline()) return;
        if (info.getAccurate()) {
            double offset = entity.getCollisionBorderSize();
            boundingBox = boundingBox.expand(offset, offset, offset);
        }
        PolyColor color = info.getOutlineColor(info.isTargetted());
        GL11.glLineWidth(info.getOutlineWidth());
        RenderGlobal.drawOutlinedBoundingBox(boundingBox, color.red(), color.green(), color.blue(), color.alpha());
    }

    @Redirect(method = "renderDebugBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;drawOutlinedBoundingBox(Lnet/minecraft/util/AxisAlignedBB;IIII)V", ordinal = 1))
    private void renderEyelineModifyColor(AxisAlignedBB boundingBox, int red, int green, int blue, int alpha, Entity entity, @Share("info") LocalRef<HitboxInfo> infoRef) {
        HitboxInfo info = infoRef.get();
        if (!info.getShowEyeline()) return;
        if (info.getAccurate()) {
            double offset = entity.getCollisionBorderSize();
            boundingBox = boundingBox.expand(offset, 0.0, offset);
        }
        PolyColor color = info.getEyelineColor(info.isTargetted());
        GL11.glLineWidth(info.getEyelineWidth());
        RenderGlobal.drawOutlinedBoundingBox(boundingBox, color.red(), color.green(), color.blue(), color.alpha());
    }

    @Inject(method = "renderDebugBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;begin(ILnet/minecraft/client/renderer/vertex/VertexFormat;)V"))
    private void renderViewRayModifyWidth(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo ci, @Share("info") LocalRef<HitboxInfo> infoRef) {
        GL11.glLineWidth(infoRef.get().getViewRayWidth());
    }

    @Redirect(method = "renderDebugBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;color(IIII)Lnet/minecraft/client/renderer/WorldRenderer;"))
    private WorldRenderer renderViewRayModifyColor(WorldRenderer worldRenderer, int red, int green, int blue, int alpha, Entity entity, @Share("info") LocalRef<HitboxInfo> infoRef) {
        HitboxInfo info = infoRef.get();
        PolyColor color = info.getViewRayColor(info.isTargetted());
        worldRenderer.color(color.red(), color.green(), color.blue(), color.alpha());
        return worldRenderer;
    }

}
