package org.polyfrost.polyhitbox.mixin;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.polyfrost.polyhitbox.config.ModConfig;
import org.polyfrost.polyhitbox.hooks.MixinHooksKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderManager.class)
public abstract class RenderManagerMixin {

    @Inject(method = "renderDebugBoundingBox", at = @At("HEAD"), cancellable = true)
    public void polyHitbox$injectHitbox(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo callbackInfo) {
        if (MixinHooksKt.overrideHitbox(entity, x, y, z, partialTicks)) {
            callbackInfo.cancel();
        }
    }

    @Redirect(method = "doRenderEntity", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;debugBoundingBox:Z"))
    private boolean redirectBoundingBox(RenderManager instance) {
        return ModConfig.INSTANCE.enabled || instance.isDebugBoundingBox();
    }
}
