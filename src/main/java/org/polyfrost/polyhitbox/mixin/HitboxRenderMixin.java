package org.polyfrost.polyhitbox.mixin;

import org.polyfrost.polyhitbox.render.HitboxRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
@Mixin(net.minecraft.client.renderer.GameRenderer.class)
public class HitboxRenderMixin {
    // 26.2 removed MultiBufferSource; hitboxes are drawn through the Gizmos system, which is only
    // valid while a render-thread GizmoCollector is active — that window is GameRenderer#render.
    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("HEAD"))
    private void polyhitbox$emitGizmos(net.minecraft.client.DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.emitGizmos();
    }
}
//?} elif >=26.1 {
/*@Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public class HitboxRenderMixin {
    @Inject(method = "submitEntities", at = @At("TAIL"))
    private void polyhitbox$render(com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.state.level.LevelRenderState levelRenderState, net.minecraft.client.renderer.SubmitNodeCollector output, CallbackInfo ci) {
        net.minecraft.world.phys.Vec3 cam = levelRenderState.cameraRenderState.pos;
        float partialTicks = net.minecraft.client.Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        HitboxRenderer.INSTANCE.renderAll(poseStack, cam.x, cam.y, cam.z, partialTicks);
    }
}*/
//?} elif >=1.21.10 {
/*@Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public class HitboxRenderMixin {
    @Inject(method = "submitEntities", at = @At("TAIL"))
    private void polyhitbox$render(com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.state.LevelRenderState levelRenderState, net.minecraft.client.renderer.SubmitNodeCollector output, CallbackInfo ci) {
        net.minecraft.world.phys.Vec3 cam = levelRenderState.cameraRenderState.pos;
        float partialTicks = net.minecraft.client.Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        HitboxRenderer.INSTANCE.renderAll(poseStack, cam.x, cam.y, cam.z, partialTicks);
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
