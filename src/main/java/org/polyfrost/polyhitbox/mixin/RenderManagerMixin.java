package org.polyfrost.polyhitbox.mixin;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.polyfrost.polyhitbox.config.ModConfig;
import org.polyfrost.polyhitbox.hooks.MixinHooksKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderManager.class)
public abstract class RenderManagerMixin {
    @Shadow public abstract void setDebugBoundingBox(boolean debugBoundingBoxIn);

    @Inject(method = "renderDebugBoundingBox", at = @At("HEAD"), cancellable = true)
    public void polyHitbox$injectHitbox(Entity entity, double x, double y, double z, float yaw, float partialTicks, CallbackInfo callbackInfo) {
        MixinHooksKt.overrideHitbox(entity, x, y, z, partialTicks);
        callbackInfo.cancel();
    }

    @Inject(method = "doRenderEntity", at = @At("HEAD"))
    public void polyHitbox$injectIsDebugBoundingBox(Entity entity, double x, double y, double z, float entityYaw, float partialTicks, boolean p_147939_10_, CallbackInfoReturnable<Boolean> cir) {
        setDebugBoundingBox(MixinHooksKt.shouldRenderHitbox(ModConfig.INSTANCE.getHitboxConfig(entity), entity));
    }
}
