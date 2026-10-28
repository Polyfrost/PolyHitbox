package org.polyfrost.polyhitbox.mixin;

import com.llamalad7.mixinextras.sugar.Local;
//? if <1.21.11 && >1.8.9
//import com.mojang.blaze3d.vertex.PoseStack;
//? if != 1.21.10
import net.minecraft.client.renderer.LevelRenderer;
//? if <1.21.11 && >1.8.9
//import net.minecraft.client.renderer.MultiBufferSource;
//? if >=26.2
import net.minecraft.client.renderer.SubmitNodeCollector;
//? if = 1.21.11 || = 1.21.10 || = 1.8.9
//import net.minecraft.client.renderer.culling.Frustum;
//? if = 1.21.10
//import net.minecraft.client.renderer.debug.DebugRenderer;
//? if >=26.1
import net.minecraft.client.renderer.state.level.LevelRenderState;
//? if <1.21.10
//import net.minecraft.world.entity.Entity;
import org.polyfrost.polyhitbox.render.HitboxRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
@Mixin(LevelRenderer.class)
public class HitboxRenderMixin {
    @Inject(
        method = "submitFeatures(Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;Z)V",
        at = @At("TAIL")
    )
    private void polyhitbox$submit(LevelRenderState levelRenderState, SubmitNodeCollector collector, boolean renderOutline, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.submitHitboxes(levelRenderState.cameraRenderState, collector);
    }
}
//?} elif >=26.1 {
/*@Mixin(LevelRenderer.class)
public class HitboxRenderMixin {
    @Shadow @Final private LevelRenderState levelRenderState;

    @Inject(method = "finalizeGizmoCollection()V", at = @At("HEAD"))
    private void polyhitbox$render(CallbackInfo ci) {
        HitboxRenderer.INSTANCE.renderHitboxes(this.levelRenderState.cameraRenderState.cullFrustum);
    }
}*/
//?} elif >=1.21.11 {
/*@Mixin(LevelRenderer.class)
public class HitboxRenderMixin {
    // the main pass lambda that renders gizmos doesn't capture the frustum
    @Unique private Frustum polyhitbox$frustum;

    @Inject(method = "addMainPass", at = @At("HEAD"))
    private void polyhitbox$captureFrustum(CallbackInfo ci, @Local(argsOnly = true) Frustum frustum) {
        this.polyhitbox$frustum = frustum;
    }

    @Inject(method = "finalizeGizmoCollection()V", at = @At("HEAD"))
    private void polyhitbox$render(CallbackInfo ci) {
        HitboxRenderer.INSTANCE.renderHitboxes(this.polyhitbox$frustum);
    }
}
*///?} elif >=1.21.10 {
/*@Mixin(DebugRenderer.class)
public class HitboxRenderMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDZ)V", at = @At("TAIL"))
    private void polyhitbox$render(PoseStack poseStack, Frustum frustum, MultiBufferSource.BufferSource buffer, double camX, double camY, double camZ, boolean showChunkBorder, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.renderHitboxes(frustum);
    }
}*/
//?} elif >1.8.9 {
/*@Mixin(LevelRenderer.class)
public class HitboxRenderMixin {
    @Inject(method = "renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At("TAIL"))
    private void polyhitbox$render(Entity entity, double camX, double camY, double camZ, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.renderEntity(entity, buffer);
    }
}*/
//?} else {
/*@Mixin(LevelRenderer.class)
public class HitboxRenderMixin {
    @Inject(method = "renderEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;F)V", at = @At("TAIL"))
    private void polyhitbox$render(Entity camera, Frustum frustum, float partialTicks, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.renderHitboxes(frustum);
    }
}*/
//?}
