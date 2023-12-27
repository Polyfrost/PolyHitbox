package org.polyfrost.polyhitboxes.mixin;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.polyfrost.polyhitboxes.render.PlayerRendererKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderManager.class)
public abstract class RenderManagerMixin {
    @Inject(method = "renderDebugBoundingBox", at = @At(value = "HEAD"), cancellable = true)
    public void injectHitbox(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo callbackInfo) {
        if (PlayerRendererKt.overrideHitbox(entity, x, y, z, partialTicks))
            callbackInfo.cancel();
    }
}
