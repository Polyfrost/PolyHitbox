package org.polyfrost.polyhitbox.mixin;

import org.polyfrost.polyhitbox.render.HitboxRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21.11 {
@Mixin(net.minecraft.client.renderer.GameRenderer.class)
public class HitboxRenderMixin {
    // These versions draw hitboxes through the Gizmos system. A GizmoCollector is active for the
    // whole render frame, and the collected gizmos are drawn after the entity models are flushed to
    // the depth buffer, so emitting here composites the hitbox correctly against the models.
    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("HEAD"))
    private void polyhitbox$emitGizmos(net.minecraft.client.DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.emitGizmos();
    }
}
//?} elif >=1.21.10 {
/*@Mixin(net.minecraft.client.renderer.debug.DebugRenderer.class)
public class HitboxRenderMixin {
    // 1.21.10 has no Gizmos system. DebugRenderer#render runs after the entity model batches have
    // been flushed to the depth buffer, so the depth-tested hitbox composites correctly against them.
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDZ)V", at = @At("TAIL"))
    private void polyhitbox$render(com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.culling.Frustum frustum, net.minecraft.client.renderer.MultiBufferSource.BufferSource buffer, double camX, double camY, double camZ, boolean showChunkBorder, CallbackInfo ci) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        HitboxRenderer.INSTANCE.renderAfterEntities(camX, camY, camZ, partialTicks);
    }
}*/
//?} else {
/*@Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public class HitboxRenderMixin {
    @Inject(method = "renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At("TAIL"))
    private void polyhitbox$render(net.minecraft.world.entity.Entity entity, double camX, double camY, double camZ, float partialTicks, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.renderEntity(entity, poseStack, buffer, camX, camY, camZ, partialTicks);
    }
}*/
//?}
