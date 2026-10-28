package org.polyfrost.polyhitbox.mixin;

//? if = 1.8.9
//import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
//? if >1.8.9
import net.minecraft.client.KeyboardHandler;
//? if >=1.21.11 || = 1.8.9
import net.minecraft.client.Minecraft;
//? if >=1.21.10
import net.minecraft.client.input.KeyEvent;
//? if = 1.8.9
//import net.minecraft.client.render.entity.EntityRenderDispatcher;
//? if <1.21.11 && >1.8.9
//import org.lwjgl.glfw.GLFW;
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
@Mixin(KeyboardHandler.class)
public class VanillaHitboxKeyMixin {
    @Inject(method = "handleDebugKeys(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void polyhitbox$suppressVanillaToggle(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.INSTANCE.getEnabled() && Minecraft.getInstance().options.keyDebugShowHitboxes.matches(event)) {
            cir.setReturnValue(true);
        }
    }
}
//?} elif >=1.21.10 {
/*@Mixin(KeyboardHandler.class)
public class VanillaHitboxKeyMixin {
    @Inject(method = "handleDebugKeys(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void polyhitbox$suppressVanillaToggle(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.INSTANCE.getEnabled() && event.key() == GLFW.GLFW_KEY_B) {
            cir.setReturnValue(true);
        }
    }
}*/
//?} elif >1.8.9 {
/*@Mixin(KeyboardHandler.class)
public class VanillaHitboxKeyMixin {
    @Inject(method = "handleDebugKeys(I)Z", at = @At("HEAD"), cancellable = true)
    private void polyhitbox$suppressVanillaToggle(int key, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.INSTANCE.getEnabled() && key == GLFW.GLFW_KEY_B) {
            cir.setReturnValue(true);
        }
    }
}*/
//?} else {
/*@Mixin(Minecraft.class)
public class VanillaHitboxKeyMixin {
    @WrapWithCondition(
        method = "tick()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;setRenderHitboxes(Z)V")
    )
    private boolean polyhitbox$suppressVanillaToggle(EntityRenderDispatcher dispatcher, boolean renderHitboxes) {
        return !ModConfig.INSTANCE.getEnabled();
    }
}*/
//?}
