package org.polyfrost.polyhitbox.mixin;

//? if >=1.21.11 {
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.util.debug.DebugValueAccess;
//?} elif >=1.21.10 {
/*import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.HitboxFeatureRenderer;
*///?} elif >1.8.9 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
*///?} else {
/*import net.minecraft.client.render.entity.EntityRenderDispatcher;
*///?}
//? if <1.21.10 && >=1.21.5
//import net.minecraft.client.renderer.entity.state.HitboxRenderState;
//? if <1.21.5
//import net.minecraft.world.entity.Entity;
import org.polyfrost.polyhitbox.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// PolyHitbox replaces the vanilla debug hitbox instead of layering over it
// so cancel the vanilla draw while enabled and stop the two boxes z-fighting
//? if >=1.21.11 {
@Mixin(EntityHitboxDebugRenderer.class)
public class VanillaHitboxMixin {
    @Inject(method = "emitGizmos", at = @At("HEAD"), cancellable = true)
    private void polyhitbox$suppressVanillaHitbox(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks, CallbackInfo ci) {
        if (ModConfig.INSTANCE.getEnabled()) ci.cancel();
    }
}
//?} elif >=1.21.10 {
/*@Mixin(HitboxFeatureRenderer.class)
public class VanillaHitboxMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void polyhitbox$suppressVanillaHitbox(SubmitNodeCollection submitNodes, MultiBufferSource.BufferSource buffer, CallbackInfo ci) {
        if (ModConfig.INSTANCE.getEnabled()) ci.cancel();
    }
}*/
//?} elif >=1.21.5 {
/*@Mixin(EntityRenderDispatcher.class)
public class VanillaHitboxMixin {
    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void polyhitbox$suppressVanillaHitbox(PoseStack pose, VertexConsumer vc, HitboxRenderState state, CallbackInfo ci) {
        if (ModConfig.INSTANCE.getEnabled()) ci.cancel();
    }
}*/
//?} elif >1.8.9 {
/*@Mixin(EntityRenderDispatcher.class)
public class VanillaHitboxMixin {
    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void polyhitbox$suppressVanillaHitbox(PoseStack pose, VertexConsumer vc, Entity entity, float a, float b, float c, float d, CallbackInfo ci) {
        if (ModConfig.INSTANCE.getEnabled()) ci.cancel();
    }
}*/
//?} else {
/*@Mixin(EntityRenderDispatcher.class)
public class VanillaHitboxMixin {
    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private void polyhitbox$suppressVanillaHitbox(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo ci) {
        if (ModConfig.INSTANCE.getEnabled()) ci.cancel();
    }
}*/
//?}
