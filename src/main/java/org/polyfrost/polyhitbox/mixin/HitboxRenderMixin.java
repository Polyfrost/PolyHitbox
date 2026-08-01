package org.polyfrost.polyhitbox.mixin;

import org.polyfrost.polyhitbox.render.HitboxRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
@Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public class HitboxRenderMixin {
    @Inject(
        method = "submitFeatures(Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;Z)V",
        at = @At("TAIL")
    )
    private void polyhitbox$submit(net.minecraft.client.renderer.state.level.LevelRenderState levelRenderState, net.minecraft.client.renderer.SubmitNodeCollector collector, boolean renderOutline, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.submitHitboxes(levelRenderState.cameraRenderState, collector);
    }
}
//?} elif >=26.1 {
/*@Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public class HitboxRenderMixin {
    @Shadow @Final private net.minecraft.client.renderer.state.level.LevelRenderState levelRenderState;

    @Inject(method = "finalizeGizmoCollection()V", at = @At("HEAD"))
    private void polyhitbox$render(CallbackInfo ci) {
        HitboxRenderer.INSTANCE.renderHitboxes(this.levelRenderState.cameraRenderState.cullFrustum);
    }
}*/
//?} elif >=1.21.11 {
/*@Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public class HitboxRenderMixin {
    @Inject(method = "finalizeGizmoCollection()V", at = @At("HEAD"))
    private void polyhitbox$render(CallbackInfo ci) {
        HitboxRenderer.INSTANCE.renderHitboxes(null);
    }
}*/
//?} elif >=1.21.10 {
/*@Mixin(net.minecraft.client.renderer.debug.DebugRenderer.class)
public class HitboxRenderMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDZ)V", at = @At("TAIL"))
    private void polyhitbox$render(com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.culling.Frustum frustum, net.minecraft.client.renderer.MultiBufferSource.BufferSource buffer, double camX, double camY, double camZ, boolean showChunkBorder, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.renderHitboxes(frustum);
    }
}*/
//?} else {
/*@Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public class HitboxRenderMixin {
    @Inject(method = "renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At("TAIL"))
    private void polyhitbox$render(net.minecraft.world.entity.Entity entity, double camX, double camY, double camZ, float partialTicks, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.renderEntity(entity, buffer);
    }
}*/
//?}
