package org.polyfrost.polyhitbox.mixin;

import org.spongepowered.asm.mixin.Mixin;

//#if MC >=1.21.5
@Mixin(net.minecraft.client.render.RenderPhase.LineWidth.class)
//#else
//$$ @Mixin(net.minecraft.client.Minecraft.class)
//#endif
public abstract class MixinLineStateShard {
    //#if MC >=1.21.5
    @org.spongepowered.asm.mixin.injection.Inject(method = "method_23553", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private static void polyhitbox$overrideLineWidth(java.util.OptionalDouble optionalDouble, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        ci.cancel();
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "method_23554", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private static void polyhitbox$overrideLineWidth2(java.util.OptionalDouble optionalDouble, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        ci.cancel();
    }
    //#endif
}
