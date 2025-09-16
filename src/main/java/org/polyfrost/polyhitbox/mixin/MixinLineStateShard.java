package org.polyfrost.polyhitbox.mixin;

import org.spongepowered.asm.mixin.Mixin;

//#if MC >=1.21.5
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.OptionalDouble;
//#else
//$$ import net.minecraft.client.Minecraft;
//#endif

//#if MC >=1.21.5
@Mixin(RenderPhase.LineWidth.class)
//#else
//$$ @Mixin(Minecraft.class)
//#endif
public abstract class MixinLineStateShard {
    //#if MC >=1.21.5
    @Inject(method = "method_23553", at = @At("HEAD"), cancellable = true)
    private static void polyhitbox$overrideLineWidth(OptionalDouble optionalDouble, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "method_23554", at = @At("HEAD"), cancellable = true)
    private static void polyhitbox$overrideLineWidth2(OptionalDouble optionalDouble, CallbackInfo ci) {
        ci.cancel();
    }
    //#endif
}
