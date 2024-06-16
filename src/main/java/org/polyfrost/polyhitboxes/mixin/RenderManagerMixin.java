package org.polyfrost.polyhitboxes.mixin;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.polyfrost.polyhitboxes.hooks.MixinHooksKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderManager.class)
public abstract class RenderManagerMixin {
    @Inject(method = "doRenderEntity", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;debugBoundingBox:Z"))
    public void polyHitbox$preRenderHitbox(CallbackInfoReturnable<Boolean> cir) {
        MixinHooksKt.preRenderHitbox();
    }

    @Inject(method = "doRenderEntity", at = @At("TAIL"))
    public void polyHitbox$postRenderHitbox(CallbackInfoReturnable<Boolean> cir) {
        MixinHooksKt.postRenderHitbox();
    }

    @Inject(method = "renderDebugBoundingBox", at = @At("HEAD"), cancellable = true)
    public void polyHitbox$injectHitbox(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo callbackInfo) {
        if (MixinHooksKt.overrideHitbox(entity, x, y, z, partialTicks)) {
            callbackInfo.cancel();
        }
    }
}
