package org.polyfrost.polyhitbox.mixin;

import org.polyfrost.polyhitbox.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The vanilla hitbox state is left exactly as the player set it while PolyHitbox is enabled
// so the vanilla bind must not flip it either, and PolyHitbox brings its own toggle instead
// Returning true reports the key as a handled debug action, which is what stops releasing the
// debug modifier from opening the debug overlay
//? if >=1.21.11 {
@Mixin(net.minecraft.client.KeyboardHandler.class)
public class VanillaHitboxKeyMixin {
    @Inject(method = "handleDebugKeys(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void polyhitbox$suppressVanillaToggle(net.minecraft.client.input.KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.INSTANCE.getEnabled() && net.minecraft.client.Minecraft.getInstance().options.keyDebugShowHitboxes.matches(event)) {
            cir.setReturnValue(true);
        }
    }
}
//?} elif >=1.21.10 {
/*@Mixin(net.minecraft.client.KeyboardHandler.class)
public class VanillaHitboxKeyMixin {
    @Inject(method = "handleDebugKeys(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void polyhitbox$suppressVanillaToggle(net.minecraft.client.input.KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.INSTANCE.getEnabled() && event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_B) {
            cir.setReturnValue(true);
        }
    }
}*/
//?} else {
/*@Mixin(net.minecraft.client.KeyboardHandler.class)
public class VanillaHitboxKeyMixin {
    @Inject(method = "handleDebugKeys(I)Z", at = @At("HEAD"), cancellable = true)
    private void polyhitbox$suppressVanillaToggle(int key, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.INSTANCE.getEnabled() && key == org.lwjgl.glfw.GLFW.GLFW_KEY_B) {
            cir.setReturnValue(true);
        }
    }
}*/
//?}
