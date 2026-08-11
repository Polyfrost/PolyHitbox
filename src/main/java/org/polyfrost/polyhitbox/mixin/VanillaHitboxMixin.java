package org.polyfrost.polyhitbox.mixin;

import org.polyfrost.polyhitbox.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// PolyHitbox replaces the vanilla debug hitbox instead of layering over it
// so cancel the vanilla draw while enabled and stop the two boxes z-fighting
//? if >=1.21.11 {
@Mixin(net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer.class)
public class VanillaHitboxMixin {
    @Inject(method = "emitGizmos", at = @At("HEAD"), cancellable = true)
    private void polyhitbox$suppressVanillaHitbox(double camX, double camY, double camZ, net.minecraft.util.debug.DebugValueAccess debugValues, net.minecraft.client.renderer.culling.Frustum frustum, float partialTicks, CallbackInfo ci) {
        if (ModConfig.INSTANCE.getEnabled()) ci.cancel();
    }
}
//?} elif >=1.21.10 {
/*@Mixin(net.minecraft.client.renderer.feature.HitboxFeatureRenderer.class)
public class VanillaHitboxMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void polyhitbox$suppressVanillaHitbox(net.minecraft.client.renderer.SubmitNodeCollection submitNodes, net.minecraft.client.renderer.MultiBufferSource.BufferSource buffer, CallbackInfo ci) {
        if (ModConfig.INSTANCE.getEnabled()) ci.cancel();
    }
}*/
//?} elif >=1.21.5 {
/*@Mixin(net.minecraft.client.renderer.entity.EntityRenderDispatcher.class)
public class VanillaHitboxMixin {
    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void polyhitbox$suppressVanillaHitbox(com.mojang.blaze3d.vertex.PoseStack pose, com.mojang.blaze3d.vertex.VertexConsumer vc, net.minecraft.client.renderer.entity.state.HitboxRenderState state, CallbackInfo ci) {
        if (ModConfig.INSTANCE.getEnabled()) ci.cancel();
    }
}*/
//?} else {
/*@Mixin(net.minecraft.client.renderer.entity.EntityRenderDispatcher.class)
public class VanillaHitboxMixin {
    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void polyhitbox$suppressVanillaHitbox(com.mojang.blaze3d.vertex.PoseStack pose, com.mojang.blaze3d.vertex.VertexConsumer vc, net.minecraft.world.entity.Entity entity, float a, float b, float c, float d, CallbackInfo ci) {
        if (ModConfig.INSTANCE.getEnabled()) ci.cancel();
    }
}*/
//?}
